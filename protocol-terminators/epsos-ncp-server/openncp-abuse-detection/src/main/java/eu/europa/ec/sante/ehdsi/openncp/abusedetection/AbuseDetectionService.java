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
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
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

    private static List<AbuseEvent> abuseList = new ArrayList<>();
    private static long lastFileAnalyzed = -1;
    private final Logger logger = LoggerFactory.getLogger(AbuseDetectionService.class);

    public AbuseDetectionService() {
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        logger.info("AbuseDetectionService Job is running......");
        var scheduler = jobExecutionContext.getScheduler();

        try {
            logger.info("AbuseDetectionService Job paused");
            scheduler.pauseJob(jobExecutionContext.getJobDetail().getKey());

            //  TODO: The ATNA secured repository must be use for the anomalies detection and not the audit generated for testing
            if (StringUtils.equals(ConfigurationManagerFactory.getConfigurationManager().getProperty("WRITE_TEST_AUDITS"), "true")) {
                String path = Utils.getProperty("TEST_AUDITS_PATH");

                try (Stream<Path> paths = Files.walk(Paths.get(path))) {
                    List<Path> files = paths
                            .filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString().endsWith(".xml"))
                            .filter(p -> p.toFile().lastModified() > lastFileAnalyzed)
                            .sorted(Comparator.comparingLong(p -> p.toFile().lastModified()))
                            .collect(Collectors.toList());

                    if (!files.isEmpty()) {
                        files.forEach(p -> {
                            try {
                                if (readAuditFile(p)) {
                                    lastFileAnalyzed = p.toFile().lastModified();
                                }
                            } catch (JAXBException e) {
                                throw new AbuseDetectionException(e);
                            }
                        });
                        abuseList = checkAnomalies(abuseList);
                        logger.info("AbuseDetectionService: end of checking data");
                    }
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
            }
        } catch (SchedulerException e) {
            throw new AbuseDetectionException(e);
        } finally {
            try {
                scheduler.resumeJob(jobExecutionContext.getJobDetail().getKey());
                logger.info("AbuseDetectionService Job resumed");
            } catch (SchedulerException e) {
                logger.debug(e.getMessage());
            }
        }
    }

    private boolean readAuditFile(Path p) throws JAXBException {

        Document document;
        try {
            String filename = p.toString();

            BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
            LocalDateTime fdt = new LocalDateTime(attr.creationTime().toInstant().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            LocalDateTime now = new LocalDateTime();
            Period diff = new Period(fdt, now);
            int val = Math.max(3600, Integer.parseInt(Constants.ABUSE_ALL_REQUEST_REFERENCE_REQUEST_PERIOD));
            if (diff.toStandardSeconds().getSeconds() > val) {
                return false; // do not process file
            }

            boolean newFileToProcess = abuseList.stream().noneMatch(f -> StringUtils.equals(f.getFilename(), filename));

            if (newFileToProcess) {
                document = EvidenceUtils.readMessage(p.toString());
                if (StringUtils.equals(document.getDocumentElement().getLocalName(), "AuditMessage")) {
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
                    if (StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
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
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            EventType.PATIENT_SERVICE_LIST.getCode())) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            Constants.PS_CLASSCODE))) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.PATIENT_SERVICE_RETRIEVE.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            EventType.PATIENT_SERVICE_RETRIEVE.getCode())) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            Constants.PS_CLASSCODE))) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.PATIENT_SERVICE_LIST.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            EventType.ORDER_SERVICE_LIST.getCode())) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            Constants.EP_CLASSCODE))) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.PATIENT_SERVICE_RETRIEVE.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            EventType.ORDER_SERVICE_RETRIEVE.getCode())) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            Constants.EP_CLASSCODE))) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.DISPENSATION_SERVICE_INITIALIZE.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            EventType.DISPENSATION_SERVICE_DISCARD.getCode())) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            Constants.EDD_CLASSCODE))) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XDR_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.ORCD_SERVICE_LIST.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            EventType.ORCD_SERVICE_LIST.getCode()))) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }
                    if (!evtPresent && StringUtils.equals(au.getEventIdentification().getEventID().getCode(),
                            IHEEventType.ORCD_SERVICE_RETRIEVE.getCode()) &&
                            au.getEventIdentification().getEventTypeCode()
                                    .stream()
                                    .anyMatch(c -> StringUtils.equals(c.getCode(),
                                            EventType.ORCD_SERVICE_RETRIEVE.getCode()))) {
                        evtPresent = true;
                        transactionType = AbuseTransactionType.XCA_SERVICE_REQUEST;
                    }

                    if (evtPresent) {
                        logger.info("Audit found: event time ['{}'}'] event id code ['{}'}'] event id display name ['{}'}'] " +
                                        "event id code system name ['{}'}'] event id codes ['{}'] active participants ['{}'}'] ",
                                dt, au.getEventIdentification().getEventID().getCode(), au.getEventIdentification().getEventID().getDisplayName(),
                                au.getEventIdentification().getEventID().getCodeSystemName(),
                                getTypeCodes(au.getEventIdentification().getEventTypeCode()), getActiveParticipants(au.getActiveParticipant()));

                        String joined_poc = au.getActiveParticipant().stream()
                                .filter(ActiveParticipantType::isUserIsRequestor)
                                .map(ActiveParticipantType::getUserID)
                                .collect(Collectors.joining("-"));

                        String simple_poc = au.getActiveParticipant().stream()
                                .filter(auid -> auid.getAlternativeUserID() != null)
                                .filter(ActiveParticipantType::isUserIsRequestor)
                                .map(ActiveParticipantType::getUserID)
                                .collect(Collectors.joining());

                        String participant = au.getParticipantObjectIdentification().stream()
                                .filter(a -> a.getParticipantObjectTypeCode() == 1 && a.getParticipantObjectTypeCodeRole() == 1)
                                .map(ParticipantObjectIdentificationType::getParticipantObjectID)
                                .collect(Collectors.joining());

                        abuseList.add(
                                new AbuseEvent(au.getEventIdentification().getEventID(),
                                        simple_poc,
                                        participant,
                                        dt,
                                        filename,
                                        transactionType)
                        );
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AbuseDetectionException(e);
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

        if (areqr <= 0 && upatr <= 0 && upocr <= 0) { // no check
            return list;
        }

        LocalDateTime now = new LocalDateTime();

        List<AbuseEvent> sortedAllList = list.stream()
                .sorted(Comparator.comparing(AbuseEvent::getRequestDateTime))
                .collect(Collectors.toList());
        if (areqr > 0 && sortedAllList.size() > areq_threshold) { // Analyze ALL requests
            for (int i = 0; i < sortedAllList.size(); i++) {
                int begin;
                int end;

                begin = i;
                end = begin + Math.min(begin + areq_threshold, sortedAllList.size() - 1 - begin);
                LocalDateTime t1 = sortedAllList.get(begin).getRequestDateTime();
                LocalDateTime t2 = sortedAllList.get(end).getRequestDateTime();
                Period diff = new Period(t1, t2); // time elapsed between first and last request
                if (diff.toStandardSeconds().getSeconds() < areqr) { // we are inside the interval for detecting
                    int totreq = end - begin + 1;
                    if (totreq > areq_threshold) {
                        logger.error("WARNING_SEC_UNEXPECTED_NUMBER_OF_REQUESTS : [Total requests: '{}' exceeding " +
                                        "threshold of: '{}' requests inside an interval of '{}' seconds] - begin event : ['{}'] end event: ['{}']",
                                totreq, areq_threshold, diff.toStandardSeconds().getSeconds(), sortedAllList.get(begin), sortedAllList.get(end));
                    }
                }
            }
        }

        List<AbuseEvent> distinctPointOfCareIds = list.stream()
                .filter(distinctByKey(AbuseEvent::getPointOfCare))
                .collect(Collectors.toList());
        if (upocr > 0 && sortedAllList.size() > upoc_threshold) { // analyze unique POC requests
            if (!distinctPointOfCareIds.isEmpty()) {
                distinctPointOfCareIds.forEach(poc -> {

                    List<AbuseEvent> sortedPocList = list.stream()
                            .filter(p -> p.getPointOfCare().equals(poc.getPointOfCare()))
                            .sorted(Comparator.comparing(AbuseEvent::getPointOfCare))
                            .sorted(Comparator.comparing(AbuseEvent::getRequestDateTime))
                            .collect(Collectors.toList());

                    for (int i = 0; i < sortedPocList.size(); i++) {
                        int begin;
                        int end;

                        begin = i;
                        end = begin + Math.min(begin + upoc_threshold, sortedPocList.size() - 1 - begin);
                        LocalDateTime t1 = sortedPocList.get(begin).getRequestDateTime();
                        LocalDateTime t2 = sortedPocList.get(end).getRequestDateTime();
                        Period diff = new Period(t1, t2); // time elapsed between first and last request
                        if (diff.toStandardSeconds().getSeconds() < upocr) { // we are inside the interval for detecting
                            int totreq = end - begin + 1;
                            if (totreq > upoc_threshold) {
                                logger.error("WARNING_SEC_UNEXPECTED_NUMBER_OF_REQUESTS_FOR_UNIQUE_POINT_OF_CARE : " +
                                                "[Total requests: '{}' exceeding threshold of: '{}' requests inside an interval " +
                                                "of '{}' seconds] - begin event : ['{}'] end event : ['{}']",
                                        totreq, upoc_threshold, diff.toStandardSeconds().getSeconds(),
                                        sortedPocList.get(begin), sortedPocList.get(end));
                            }
                        }
                    }
                });
            }
        }

        List<AbuseEvent> distinctPatientIds = list.stream()
                .filter(distinctByKey(AbuseEvent::getPatientId))
                .collect(Collectors.toList());
        if (upatr > 0 && sortedAllList.size() > upat_threshold) { // Analyze unique Patient requests

            if (!distinctPatientIds.isEmpty()) {
                distinctPatientIds.forEach(pat -> {

                    List<AbuseEvent> sortedXcpdList = list.stream()
                            .filter(p -> p.getTransactionType().equals(AbuseTransactionType.XCPD_SERVICE_REQUEST))
                            .filter(p -> p.getPatientId().equals(pat.getPatientId()))
                            .sorted(Comparator.comparing(AbuseEvent::getPatientId))
                            .sorted(Comparator.comparing(AbuseEvent::getRequestDateTime))
                            .collect(Collectors.toList());

                    for (int i = 0; i < sortedXcpdList.size(); i++) {
                        int begin;
                        int end;

                        begin = i;
                        end = begin + Math.min(begin + upat_threshold, sortedXcpdList.size() - 1 - begin);
                        LocalDateTime t1 = sortedXcpdList.get(begin).getRequestDateTime();
                        LocalDateTime t2 = sortedXcpdList.get(end).getRequestDateTime();
                        Period diff = new Period(t1, t2); // time elapsed between first and last request
                        if (diff.toStandardSeconds().getSeconds() < upatr) { // we are inside the interval for detecting
                            int totreq = end - begin + 1;
                            if (totreq > upat_threshold) {
                                logger.error("WARNING_SEC_UNEXPECTED_NUMBER_OF_REQUESTS_FOR_UNIQUE_PATIENT : [Total requests: " + totreq + " exceeding threshold of : " + upat_threshold + " requests inside an interval of " + diff.toStandardSeconds().getSeconds() + " seconds] - begin event : [" + sortedXcpdList.get(begin) + "] end event : [" + sortedXcpdList.get(end) + "]");
                            }
                        }
                    }
                });
            }
        }

        // strip from table file older than ABUSE_ALL_REQUEST_REFERENCE_REQUEST_PERIOD
        int purge_limit = NumberUtils.max(new int[]{areqr, upocr, upatr});
        List<AbuseEvent> ret = list.stream()
                .sorted(Comparator.comparing(AbuseEvent::getRequestDateTime))
                .filter(p -> Period.fieldDifference(p.getRequestDateTime(), now).toStandardSeconds().getSeconds() <= purge_limit)
                .collect(Collectors.toList());

        if (ret.size() < list.size()) {
            logger.info("'{}' events purged from active list, new list size; '{}'", list.size() - ret.size(), ret.size());
        } else {
            logger.info("Events in active list: '{}'", list.size());
        }
        return ret;
    }

    private String getActiveParticipants(List<AuditMessage.ActiveParticipant> activeParticipant) {
        StringBuilder val = new StringBuilder();
        for (AuditMessage.ActiveParticipant p : activeParticipant) {
            val.append("ActiveParticipant ").append(p.getUserID()).append(" - ").append(p.isUserIsRequestor()).append(" ");
        }
        return StringUtils.trim(val.toString());
    }

    private String getTypeCodes(List<CodedValueType> eventTypeCode) {
        StringBuilder val = new StringBuilder();
        for (CodedValueType t : eventTypeCode) {
            val.append("EventTypeCode ").append(t.getCode()).append(" - ").append(t.getDisplayName()).append(" ");
        }
        return StringUtils.trim(val.toString());
    }
}
