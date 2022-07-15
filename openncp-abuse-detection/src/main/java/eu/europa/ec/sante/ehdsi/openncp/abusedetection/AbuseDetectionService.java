package eu.europa.ec.sante.ehdsi.openncp.abusedetection;

import epsos.ccd.gnomon.auditmanager.AuditTrailUtils;
import epsos.ccd.gnomon.auditmanager.EventType;
import epsos.ccd.gnomon.auditmanager.IHEEventType;
import epsos.ccd.gnomon.utils.Utils;
import eu.epsos.util.EvidenceUtils;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import net.RFC3881.ActiveParticipantType;
import net.RFC3881.AuditMessage;
import net.RFC3881.CodedValueType;
import net.RFC3881.ParticipantObjectIdentificationType;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.util.Constants;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbuseDetectionService implements Job {

    private static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("LOGGER_CLINICAL");
    private static final Logger LOGGER = LoggerFactory.getLogger(AbuseDetectionService.class);

    private static List<AbuseEvent> abuseList = new ArrayList<>();

    private static long lastFileAnalyzed = -1;

    public AbuseDetectionService() {
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOGGER.info("AbuseDetectionService Job is running......");

        var scheduler = jobExecutionContext.getScheduler();

        try {
            LOGGER.info("AbuseDetectionService Job paused");
            //scheduler.pauseJob(new JobKey(AbuseDetectionHelper.NAME_OF_JOB, AbuseDetectionHelper.NAME_OF_GROUP));
            scheduler.pauseJob(jobExecutionContext.getJobDetail().getKey());

            if (StringUtils.equals(ConfigurationManagerFactory.getConfigurationManager().getProperty("WRITE_TEST_AUDITS"), "true")) {
                String path = Utils.getProperty("TEST_AUDITS_PATH");

                try (Stream<Path> paths = Files.walk(Paths.get(path))) {
                    List<Path> files = paths
                            .filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString().endsWith(".xml"))
                            .filter(p -> p.toFile().lastModified() > lastFileAnalyzed)
                            .sorted(Comparator.comparingLong(p -> p.toFile().lastModified()))
                            .collect(Collectors.toList());

                    if(files.size() > 0) {
                        files.forEach(p -> {
                            try {
                                if (readAuditFile(p)) {
                                    lastFileAnalyzed = p.toFile().lastModified();
                                }
                            } catch (JAXBException e) {
                                LOGGER.error(e.getMessage());
                                throw new RuntimeException(e);
                            }
                        });
                        abuseList = checkAnomalies(abuseList);
                        LOGGER.info("AbuseDetectionService: end of checking data");
                    }
                } catch (Exception e) {
                    LOGGER.debug(e.getMessage());
                }
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                scheduler.resumeJob(jobExecutionContext.getJobDetail().getKey());
                LOGGER.info("AbuseDetectionService Job resumed");
            } catch (SchedulerException e) {
                LOGGER.debug(e.getMessage());
            }
        }
    }

    private boolean readAuditFile(Path p) throws JAXBException {
        Document document = null;
        try {
            String filename = p.toString();

            BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
            LocalDateTime fdt = new LocalDateTime(attr.creationTime().toInstant().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            LocalDateTime now = new LocalDateTime();
            Period diff = new Period(fdt, now);
            int val = Math.max(3600, Integer.parseInt(Constants.ABUSE_ALL_REQUEST_REFERENCE_REQUEST_PERIOD));
            if(diff.toStandardSeconds().getSeconds() > val) {
                return false; // do not process file
            }

            boolean newFileToProcess = abuseList.stream().noneMatch(f -> StringUtils.equals(f.getFilename(), filename));


//            IntStream.range(0, abuseList.size())
//                    .filter(i -> Objects.nonNull(abuseList.get(i)))
//                    .filter(i -> filename.equals(abuseList.get(i).getFilename()))
//                    .findFirst()
//                    .orElse(-1);

            if(newFileToProcess) {
                document = EvidenceUtils.readMessage(p.toString());
                if(StringUtils.equals(document.getDocumentElement().getLocalName(), "AuditMessage")) {
                    AuditMessage au = AuditTrailUtils.convertXMLToAuditObject(p.toFile());

                    LocalDateTime dt = new LocalDateTime(au.getEventIdentification().getEventDateTime()
                            .toGregorianCalendar()
                            .toZonedDateTime()
                            .toLocalDateTime()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli());

                    boolean evtPresent = false;
                    AbuseTransactionType transactionType = AbuseTransactionType.TRANSACTION_UNKNOWN;
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            EventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS.getCode()))) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCPD_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.PATIENT_SERVICE_LIST.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            EventType.PATIENT_SERVICE_LIST.getCode())).findAny().isPresent() &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            Constants.PS_CLASSCODE)).findAny().isPresent()) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.PATIENT_SERVICE_RETRIEVE.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            EventType.PATIENT_SERVICE_RETRIEVE.getCode())).findAny().isPresent() &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            Constants.PS_CLASSCODE)).findAny().isPresent()) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.PATIENT_SERVICE_LIST.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            EventType.ORDER_SERVICE_LIST.getCode())).findAny().isPresent() &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            Constants.EP_CLASSCODE)).findAny().isPresent()) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.PATIENT_SERVICE_RETRIEVE.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            EventType.ORDER_SERVICE_RETRIEVE.getCode())).findAny().isPresent() &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            Constants.EP_CLASSCODE)).findAny().isPresent()) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.DISPENSATION_SERVICE_INITIALIZE.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            EventType.DISPENSATION_SERVICE_DISCARD.getCode())).findAny().isPresent() &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            Constants.EDD_CLASSCODE)).findAny().isPresent()) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XDR_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.ORCD_SERVICE_LIST.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            EventType.ORCD_SERVICE_LIST.getCode())).findAny().isPresent()) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.ORCD_SERVICE_RETRIEVE.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .filter(c -> StringUtils.equals(c.getCode(),
                                            EventType.ORCD_SERVICE_RETRIEVE.getCode())).findAny().isPresent()) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }

                    if (evtPresent == true) {
                        LOGGER.info("audit found: " +
                                "event time [" + dt.toString() + "] " +
                                "event id code [" + au.getEventIdentification().getEventID().getCode() + "] " +
                                "event id display name [" + au.getEventIdentification().getEventID().getDisplayName() + "] " +
                                "event id code system name [" + au.getEventIdentification().getEventID().getCodeSystemName() + "] " +
                                "event id codes [" + getTypeCodes(au.getEventIdentification().getEventTypeCode()) + "] " +
                                "active participants [" + getActiveParticipants(au.getActiveParticipant()) + "] "
                        );

                        String joined_poc = au.getActiveParticipant().stream()
                                .filter(a -> a.isUserIsRequestor())
                                .map(ActiveParticipantType::getUserID)
                                .collect(Collectors.joining("-"));

                        String participant = au.getParticipantObjectIdentification().stream()
                                .filter(a -> a.getParticipantObjectTypeCode() == 1 && a.getParticipantObjectTypeCodeRole() == 1)
                                .map(ParticipantObjectIdentificationType::getParticipantObjectID)
                                .collect(Collectors.joining());

                        /*
                        int index = IntStream.range(0, abuseList.size())
                                .filter(i -> Objects.nonNull(abuseList.get(i)))
                                .filter(i -> au.getEventIdentification().getEventID().getCode().equals(abuseList.get(i).getRequestType().getCode()) &&
                                        joined_poc.equals(abuseList.get(i).getPointOfCare()) &&
                                        participant.equals(abuseList.get(i).getPatientId()))
                                .findFirst()
                                .orElse(-1);

                        if (index != -1) {
                            AbuseEvent ae = abuseList.get(index);
                            ae.setRequestNum(ae.getRequestNum() + 1);
                            ae.setLastRequestDateTime(dt);
                            ae.addFile(filename);
                        } else {
                            abuseList.add(new AbuseEvent(au.getEventIdentification().getEventID(),
                                    joined_poc,
                                    participant,
                                    1,
                                    dt,
                                    false,
                                    filename));
                        }
                        */
                        abuseList.add(
                                new AbuseEvent(au.getEventIdentification().getEventID(),
                                    joined_poc,
                                    participant,
                                    dt,
                                    filename,
                                    transactionType)
                                );
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            LOGGER.debug(e.getMessage());
            throw new RuntimeException(e);
        } catch (SAXException e) {
            LOGGER.debug(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.debug(e.getMessage());
            throw new RuntimeException(e);
        }

        return true;
    }

    private List<AbuseEvent> checkAnomalies(List<AbuseEvent> list) {
        int areqr = Integer.parseInt(Constants.ABUSE_ALL_REQUEST_REFERENCE_REQUEST_PERIOD);
        int upatr = Integer.parseInt(Constants.ABUSE_UNIQUE_PATIENT_REFERENCE_REQUEST_PERIOD);
        int upocr = Integer.parseInt(Constants.ABUSE_UNIQUE_POC_REFERENCE_REQUEST_PERIOD);

        int areq_threshold = Integer.parseInt(Constants.ABUSE_ALL_REQUEST_THRESHOLD);
        int upat_threshold = Integer.parseInt(Constants.ABUSE_UNIQUE_PATIENT_REQUEST_THRESHOLD);
        int upoc_threshold = Integer.parseInt(Constants.ABUSE_UNIQUE_POC_REQUEST_THRESHOLD);

        if(areqr <= 0 && upatr <= 0 && upocr <= 0) { // no check
            return list;
        }

        List<AbuseEvent> sortedAllList = list.stream()
                .sorted(Comparator.comparing(AbuseEvent::getRequestDateTime))
                .collect(Collectors.toList());
        if(areqr > 0 && sortedAllList.size() > 1) { // Analyze ALL requests
            LocalDateTime begin = sortedAllList.get(0).getRequestDateTime();
            LocalDateTime end = sortedAllList.get(sortedAllList.size() - 1).getRequestDateTime();
            Period diff = new Period(begin, end); // time elapsed between first and last request
            if(diff.toStandardSeconds().getSeconds() < areqr) { // we are inside the interval for detecting
                if(sortedAllList.size() > areq_threshold) {
                    LOGGER.error("WARNING_SEC_UNEXPECTED_NUMBER_OF_REQUESTS : [Total requests: " + sortedAllList.size() + " inside an interval of " + diff.toStandardSeconds().getSeconds() + " seconds]");
                }
            }
        }


        List<AbuseEvent> distinctPatientIds = list.stream()
                .filter( distinctByKey(p -> p.getPatientId()) )
                .collect( Collectors.toList() );
        if(upatr > 0 && sortedAllList.size() > 1) { // Analyze unique Patient requests

            if(distinctPatientIds.size() > 0) {
                distinctPatientIds.forEach(pat -> {

                    List<AbuseEvent> sortedXcpdList = list.stream()
                            .filter(p -> p.getTransactionType().equals(AbuseTransactionType.XCPD_SERVICE_REQUEST))
                            .filter(p -> p.getPatientId().equals(pat.getPatientId()))
                            .sorted(Comparator.comparing(AbuseEvent::getPatientId))
                            .sorted(Comparator.comparing(AbuseEvent::getRequestDateTime))
                            .collect(Collectors.toList());

                    LocalDateTime begin = sortedXcpdList.get(0).getRequestDateTime();
                    LocalDateTime end = sortedXcpdList.get(sortedXcpdList.size() - 1).getRequestDateTime();
                    Period diff = new Period(begin, end); // time elapsed between first and last request
                    if(diff.toStandardSeconds().getSeconds() < upatr) { // we are inside the interval for detecting
                        if(sortedXcpdList.size() > upat_threshold) {
                            LOGGER.error("WARNING_SEC_UNEXPECTED_NUMBER_OF_REQUESTS_FOR_UNIQUE_PATIENT : [Total requests: " + sortedXcpdList.size() + " inside an interval of " + diff.toStandardSeconds().getSeconds() + "seconds ]");
                        }
                    }
                });
            }
        }

        List<AbuseEvent> sortedPocList = list.stream()
                .filter(p -> p.getTransactionType().equals(AbuseTransactionType.XCA_SERVICE_REQUEST))
                .sorted(Comparator.comparing(AbuseEvent::getRequestDateTime))
                .collect(Collectors.toList());
        if(upocr > 0 && sortedPocList.size() > 1) { // analyze unique POC requests
            LocalDateTime begin = sortedPocList.get(0).getRequestDateTime();
            LocalDateTime end = sortedPocList.get(sortedPocList.size() - 1).getRequestDateTime();
            Period diff = new Period(begin, end); // time elapsed between first and last request
            if(diff.toStandardSeconds().getSeconds() < upocr) { // we are inside the interval for detecting
                if(sortedPocList.size() > upoc_threshold) {
                    LOGGER.error("WARNING_SEC_UNEXPECTED_NUMBER_OF_REQUESTS_FOR_UNIQUE_POINT_OF_CARE : [Total requests: " + sortedPocList.size() + " inside an interval of " + diff.toStandardSeconds().getSeconds() + " seconds]");
                }
            }
        }

        // TODO: strip from table file older than ABUSE_ALL_REQUEST_REFERENCE_REQUEST_PERIOD
        //

        LocalDateTime now = new LocalDateTime();
        return list.stream()
                .sorted(Comparator.comparing(AbuseEvent::getRequestDateTime))
                .filter(p -> Period.fieldDifference(p.getRequestDateTime(), now).toStandardSeconds().getSeconds() <= areqr)
                .collect(Collectors.toList());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private String getActiveParticipants(List<AuditMessage.ActiveParticipant> activeParticipant) {
        String val = "";
        for(AuditMessage.ActiveParticipant p : activeParticipant) {
            val += "ActiveParticipant " + p.getUserID() + " - " + p.isUserIsRequestor() + " ";
        }
        return StringUtils.trim(val);
    }

    private String getTypeCodes(List<CodedValueType> eventTypeCode) {
        String val = "";
        for(CodedValueType t : eventTypeCode) {
            val += "EventTypeCode " + t.getCode() + " - " + t.getDisplayName() + " ";
        }
        return StringUtils.trim(val);
    }

}
