package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.web.rest;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.Constants;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.cfg.ReadSMPProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.*;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.SimpleErrorHandler;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.BdxSmpValidator;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.DynamicDiscoveryClient;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.SMPConverter;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ehdsi.EndpointType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ehdsi.RedirectType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ehdsi.ServiceMetadata;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ehdsi.SignedServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@RequestMapping(path = "/api")
public class SMPUpdateFileController {

    private final Logger logger = LoggerFactory.getLogger(SMPUpdateFileController.class);
    private final DynamicDiscoveryClient dynamicDiscoveryClient;
    private final Environment environment;
    private final SMPConverter smpconverter;
    private final ReadSMPProperties readProperties;
    private String type;
    private boolean isSigned;

    @Autowired
    public SMPUpdateFileController(SMPConverter smpconverter, DynamicDiscoveryClient dynamicDiscoveryClient, ReadSMPProperties readProperties, Environment environment) {
        logger.debug("SMPUpdateFileController('{}', '{}', '{}'", smpconverter, environment, readProperties);
        this.dynamicDiscoveryClient = dynamicDiscoveryClient;
        this.readProperties = readProperties;
        this.smpconverter = smpconverter;
        this.environment = environment;
    }

    @PostMapping(value = "smpeditor/updater/updateSmpFile")
    public ResponseEntity postUpdate(@RequestBody SMPHttp smpfileupdate) throws IOException {

        logger.debug("\n==== in postUpdate ====");
        if (logger.isDebugEnabled()) {
            logger.debug("SMP File Update - '{}'", smpfileupdate);
        }

        isSigned = false;
        logger.debug("File: '{}' will be loaded for update", smpfileupdate.getSmpFileName());
        /*Validate xml file*/
        String contentFile = new String(Files.readAllBytes(Paths.get(smpfileupdate.getSmpFileName())));
        boolean fileDeleted;
        boolean valid = BdxSmpValidator.validateFile(contentFile);

        if (valid) {
            logger.debug("\n****VALID XML File");
        } else {
            logger.debug("\n****NOT VALID XML File");
            String message = environment.getProperty("error.notsmp"); //messages.properties
            File f = new File(smpfileupdate.getSmpFileName());
            fileDeleted = f.delete();
            if (logger.isDebugEnabled()) {
                logger.debug("Converted File deleted: '{}'", fileDeleted);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        /*Read data from xml*/
        ServiceMetadata serviceMetadata = (ServiceMetadata) smpconverter.convertFromXml(new File(smpfileupdate.getSmpFileName()));

    /*
     Condition to know the type of file (Redirect|ServiceInformation) in order to build the form
     */
        if (serviceMetadata.getRedirect() != null) {
            logger.debug("\n******** REDIRECT");
            type = "Redirect";
            smpfileupdate.setDocumentType(SMPType.Redirect.name());

            if (!serviceMetadata.getRedirect().getExtensions().isEmpty()) {
                logger.debug("\n******* SIGNED EXTENSION - '{}'", serviceMetadata.getRedirect().getExtensions().get(0).getAny().getNodeName());
                isSigned = true;
            }

      /*
        get participantIdentifier from redirect href
      */
            /*May change if Participant Identifier specification change*/
            String href = serviceMetadata.getRedirect().getHref();
            String participantID;
            Pattern pattern = Pattern.compile(environment.getProperty("ParticipantIdentifier.Scheme") + ".*");//SPECIFICATION
            Matcher matcher = pattern.matcher(href);
            if (matcher.find()) {
                String result = matcher.group(0);
                result = URLDecoder.decode(result, StandardCharsets.UTF_8);
                String[] ids = result.split("/services/");
                participantID = ids[0];
                String[] cc = participantID.split(":");

                for (Countries country : Countries.getAll()) {
                    if (cc[4].equals(country.name())) {
                        smpfileupdate.setCountry(Countries.valueOf(cc[4]));
                    }
                }
                if (smpfileupdate.getCountry() == null) {
                    String message = environment.getProperty("error.redirect.href.participantID"); //messages.properties
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                logger.error("\n****NOT VALID HREF IN REDIRECT");
                String message = environment.getProperty("error.redirect.href"); //messages.properties
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

        } else if (serviceMetadata.getServiceInformation() != null) {
            logger.debug("\n******** SERVICE INFORMATION");
            type = "ServiceInformation";

            if (!serviceMetadata.getServiceInformation().getExtensions().isEmpty()) {
                logger.debug("\n******* SIGNED EXTENSION - '{}'", serviceMetadata.getServiceInformation().getExtensions().get(0).getAny().getNodeName());
                isSigned = true;
            }

            smpfileupdate.setDocumentIdentifier(serviceMetadata.getServiceInformation().getDocumentIdentifier().getValue());
            smpfileupdate.setDocumentIdentifierScheme(serviceMetadata.getServiceInformation().getDocumentIdentifier().getScheme());
            String documentIdentifier = smpfileupdate.getDocumentIdentifier();
            logger.debug("\n******** DOC ID 1 - '{}'", documentIdentifier);

            //  Used to check SMP File type in order to render html updatesmpfileform page
            String documentID = "";
            Map<String, String> propertiesMap = readProperties.readPropertiesFile();
            Set set2 = propertiesMap.entrySet();

            for (Object aSet2 : set2) {
                Map.Entry mentry2 = (Map.Entry) aSet2;
                if (documentIdentifier.equals(mentry2.getKey().toString())) {

                    String[] docs = mentry2.getValue().toString().split("\\.");
                    documentID = docs[0];
                    logger.debug("\n ****** documentID - '{}'", documentID);
                    //  Country_B_Identity_Provider case: can have two different DocIds
                    if (docs.length > 2) {
                        smpfileupdate.setIssuanceType(docs[2]);
                    }
                    break;
                }
            }

            SMPType[] smptypes = SMPType.getAll();
            for (SMPType smptype1 : smptypes) {
                if (smptype1.name().equals(documentID)) {
                    smpfileupdate.setDocumentType(smptype1.name());
                    break;
                }
            }

            if (smpfileupdate.getDocumentType() == null) {
                String message = environment.getProperty("error.serviceinformation.documentID"); //messages.properties
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            String participanteID = serviceMetadata.getServiceInformation().getParticipantIdentifier().getValue();
            String[] cc = participanteID.split(":");//SPECIFICATION

            for (Countries country : Countries.getAll()) {
                if (cc[2].equals(country.name())) {
                    smpfileupdate.setCountry(Countries.valueOf(cc[2]));
                    break;
                }
            }
            if (smpfileupdate.getCountry() == null) {
                //  messages.properties
                String message = environment.getProperty("error.serviceinformation.participantID");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            /*Builds final file name*/
            String timeStamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date());
            String fileName = smpfileupdate.getDocumentType() + "_" + smpfileupdate.getCountry() + "_" + timeStamp + ".xml";
            smpfileupdate.setSmpFileName(fileName);

            smpfileupdate.setParticipantIdentifier(participanteID);
            smpfileupdate.setParticipantIdentifierScheme(serviceMetadata.getServiceInformation().getParticipantIdentifier().getScheme());
            smpfileupdate.setTransportProfile(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0).getTransportProfile());
            smpfileupdate.setRequiredBusinessLevelSig(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0).isRequireBusinessLevelSignature());
            smpfileupdate.setMinimumAutenticationLevel(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0).getMinimumAuthenticationLevel());
        }

        /*
         * Read smpeditor.properties file
         */
        SMPFields smpfields = readProperties.readProperties(SMPType.valueOf(smpfileupdate.getDocumentType()));

        if ("ServiceInformation".equals(type)) {
            EndpointType endpoint = serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0);

            X509Certificate cert;
            String subjectName = null;
            if (smpfields.getCertificate().isEnable()) {
                try {
                    InputStream in = new ByteArrayInputStream(endpoint.getCertificate());
                    cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
                    if (cert != null) {
                        subjectName = "Issuer: " + cert.getIssuerX500Principal().getName() + "\nSerial Number #"
                                + cert.getSerialNumber();
                        smpfileupdate.setCertificateContent(subjectName);
                        smpfileupdate.setCertificate(cert.getEncoded());
                    }
                } catch (CertificateException ex) {
                    logger.error("\n CertificateException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                }
            } else {
                smpfileupdate.setCertificate(null);
            }


            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date cal = endpoint.getServiceActivationDate().getTime();
            String formatted = format.format(cal);
            Date datead = null;
            Date dateed = null;
            Date cal2 = endpoint.getServiceExpirationDate().getTime();
            String formatted2 = format.format(cal2);
            try {
                datead = format.parse(formatted);
                dateed = format.parse(formatted2);
            } catch (ParseException ex) {
                logger.error("\n ParseException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            smpfileupdate.setServiceActivationDate(datead);
            smpfileupdate.setServiceExpirationDate(dateed);
            smpfileupdate.setCertificateContent(subjectName);
            smpfileupdate.setServiceDescription(endpoint.getServiceDescription());
            smpfileupdate.setTechnicalContactUrl(endpoint.getTechnicalContactUrl());
            smpfileupdate.setTechnicalInformationUrl(endpoint.getTechnicalInformationUrl());

            if (smpfields.getExtension().isEnable()) {
                try (Scanner scanner = new Scanner(new File(smpfileupdate.getSmpFileName()), StandardCharsets.UTF_8.name())) {
                    String content = scanner.useDelimiter("\\Z").next();
                    String capturedString = content.substring(content.indexOf("<Extension>"), content.indexOf("</Extension>"));
                    String[] endA = capturedString.split("<Extension>");
                    logger.debug("\n*****Content from Extension 1 : \n '{}'", endA[1]);

                    Document docOriginal = smpconverter.parseDocument(endA[1]);
                    docOriginal.getDocumentElement().normalize();


                } catch (IOException ex) {
                    logger.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (SAXException ex) {
                    logger.error("\n SAXException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (ParserConfigurationException ex) {
                    logger.error("\n ParserConfigurationException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                }
            }

        } else if ("Redirect".equals(type)) {
            RedirectType redirect = serviceMetadata.getRedirect();
            smpfileupdate.setCertificateUID(redirect.getCertificateUID());
            smpfileupdate.setHref(redirect.getHref());
        }

        if (!isSigned) {
            isSigned = smpconverter.isSignedServiceMetadata(smpfileupdate);
        }

        return ResponseEntity.ok(smpfileupdate);
    }

    @PostMapping(path = "/smpeditor/updater/setSmpFileToUpdate")
    public ResponseEntity<SMPUpdateFields> createSMPHttp(@RequestPart MultipartFile multipartFile) throws IOException {
        SMPHttp smpHttp = new SMPHttp();
        File convFile = new File(Constants.SMP_DIR_PATH + File.separator + multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(convFile);
        } catch (IOException ex) {
            logger.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (IllegalStateException ex) {
            logger.error("\n IllegalStateException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        }
        smpHttp.setSmpFile(convFile);
        smpHttp.setSmpFileName(convFile.getName());

        isSigned = false;
        /*Validate xml file*/
        String contentFile = new String(Files.readAllBytes(Paths.get(smpHttp.getSmpFile().getPath())));
        boolean fileDeleted;
        boolean valid = BdxSmpValidator.validateFile(contentFile);

        if (valid) {
            logger.debug("\n****VALID XML File");
        } else {
            logger.debug("\n****NOT VALID XML File");
            String message = environment.getProperty("error.notsmp"); //messages.properties
            fileDeleted = smpHttp.getSmpFile().delete();
            if (logger.isDebugEnabled()) {
                logger.debug("Converted File deleted: '{}'", fileDeleted);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        /*Read data from xml*/
        Object o = smpconverter.convertFromXml(convFile);
        ServiceMetadata serviceMetadata;
        if(o instanceof SignedServiceMetadata) {
            serviceMetadata = ((SignedServiceMetadata)o).getServiceMetadata();
        } else {
            serviceMetadata = (ServiceMetadata)o;
        }
        //ServiceMetadata serviceMetadata = (ServiceMetadata) smpconverter.convertFromXml(convFile);

    /*
     Condition to know the type of file (Redirect|ServiceInformation) in order to build the form
     */
        if (serviceMetadata.getRedirect() != null) {
            logger.debug("\n******** REDIRECT");
            type = "Redirect";
            smpHttp.setDocumentType(SMPType.Redirect.name());

            if (!serviceMetadata.getRedirect().getExtensions().isEmpty()) {
                logger.debug("\n******* SIGNED EXTENSION - '{}'", serviceMetadata.getRedirect().getExtensions().get(0).getAny().getNodeName());
                isSigned = true;
            }

      /*
        get participantIdentifier from redirect href
      */
            /*May change if Participant Identifier specification change*/
            String href = serviceMetadata.getRedirect().getHref();
            String participantID;
            Pattern pattern = Pattern.compile(environment.getProperty("ParticipantIdentifier.Scheme") + ".*");//SPECIFICATION
            Matcher matcher = pattern.matcher(href);
            if (matcher.find()) {
                String result = matcher.group(0);
                result = URLDecoder.decode(result, StandardCharsets.UTF_8);
                String[] ids = result.split("/services/");
                participantID = ids[0];
                String[] cc = participantID.split(":");

                for (Countries country : Countries.getAll()) {
                    if (cc[4].equals(country.name())) {
                        smpHttp.setCountry(Countries.valueOf(cc[4]));
                    }
                }
                if (smpHttp.getCountry() == null) {
                    String message = environment.getProperty("error.redirect.href.participantID"); //messages.properties
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                logger.error("\n****NOT VALID HREF IN REDIRECT");
                String message = environment.getProperty("error.redirect.href"); //messages.properties
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

        } else if (serviceMetadata.getServiceInformation() != null) {
            logger.debug("\n******** SERVICE INFORMATION");
            type = "ServiceInformation";

            if (!serviceMetadata.getServiceInformation().getExtensions().isEmpty()) {
                logger.debug("\n******* SIGNED EXTENSION - '{}'", serviceMetadata.getServiceInformation().getExtensions().get(0).getAny().getNodeName());
                isSigned = true;
            }

            smpHttp.setDocumentIdentifier(serviceMetadata.getServiceInformation().getDocumentIdentifier().getValue());
            smpHttp.setDocumentIdentifierScheme(serviceMetadata.getServiceInformation().getDocumentIdentifier().getScheme());
            String documentIdentifier = smpHttp.getDocumentIdentifier();
            logger.debug("\n******** DOC ID 1 - '{}'", documentIdentifier);

            //  Used to check SMP File type in order to render html updatesmpfileform page
            String documentID = "";
            Map<String, String> propertiesMap = readProperties.readPropertiesFile();
            Set set2 = propertiesMap.entrySet();

            for (Object aSet2 : set2) {
                Map.Entry mentry2 = (Map.Entry) aSet2;
                if (documentIdentifier.equals(mentry2.getKey().toString())) {

                    String[] docs = mentry2.getValue().toString().split("\\.");
                    documentID = docs[0];
                    logger.debug("\n ****** documentID - '{}'", documentID);
                    //  Country_B_Identity_Provider case: can have two different DocIds
                    if (docs.length > 2) {
                        smpHttp.setIssuanceType(docs[2]);
                    }
                    break;
                }
            }

            SMPType[] smptypes = SMPType.getAll();
            for (SMPType smptype1 : smptypes) {
                if (smptype1.name().equals(documentID)) {
                    smpHttp.setDocumentType(smptype1.name());
                    break;
                }
            }

            if (smpHttp.getDocumentType() == null) {
                String message = environment.getProperty("error.serviceinformation.documentID"); //messages.properties
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            String participanteID = serviceMetadata.getServiceInformation().getParticipantIdentifier().getValue();
            String[] cc = participanteID.split(":");//SPECIFICATION

            for (Countries country : Countries.getAll()) {
                if (cc[2].equals(country.name())) {
                    smpHttp.setCountry(Countries.valueOf(cc[2]));
                    break;
                }
            }
            if (smpHttp.getCountry() == null) {
                //  messages.properties
                String message = environment.getProperty("error.serviceinformation.participantID");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            /*Builds final file name*/
            String timeStamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date());
            String fileName = smpHttp.getDocumentType() + "_" + smpHttp.getCountry() + "_" + timeStamp + ".xml";
            smpHttp.setSmpFileName(fileName);

            smpHttp.setParticipantIdentifier(participanteID);
            smpHttp.setParticipantIdentifierScheme(serviceMetadata.getServiceInformation().getParticipantIdentifier().getScheme());
            smpHttp.setTransportProfile(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0).getTransportProfile());
            smpHttp.setRequiredBusinessLevelSig(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0).isRequireBusinessLevelSignature());
            smpHttp.setMinimumAutenticationLevel(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0).getMinimumAuthenticationLevel());
        }

        /*
         * Read smpeditor.properties file
         */
        SMPFields smpfields = readProperties.readProperties(SMPType.valueOf(smpHttp.getDocumentType()));

        if ("ServiceInformation".equals(type)) {
            EndpointType endpoint = serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0);

            X509Certificate cert;
            String subjectName = null;
            if (smpfields.getCertificate().isEnable()) {
                try {
                    InputStream in = new ByteArrayInputStream(endpoint.getCertificate());
                    cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
                    if (cert != null) {
                        subjectName = "Issuer: " + cert.getIssuerX500Principal().getName() + "\nSerial Number #"
                                + cert.getSerialNumber();
                        smpHttp.setCertificateContent(subjectName);
                        smpHttp.setCertificate(cert.getEncoded());
                    }
                } catch (CertificateException ex) {
                    logger.error("\n CertificateException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                }
            } else {
                smpHttp.setCertificate(null);
            }


            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date cal = endpoint.getServiceActivationDate().getTime();
            String formatted = format.format(cal);
            Date datead = null;
            Date dateed = null;
            Date cal2 = endpoint.getServiceExpirationDate().getTime();
            String formatted2 = format.format(cal2);
            try {
                datead = format.parse(formatted);
                dateed = format.parse(formatted2);
            } catch (ParseException ex) {
                logger.error("\n ParseException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            smpHttp.setServiceActivationDate(datead);
            smpHttp.setServiceExpirationDate(dateed);
            smpHttp.setCertificateContent(subjectName);
            smpHttp.setServiceDescription(endpoint.getServiceDescription());
            smpHttp.setTechnicalContactUrl(endpoint.getTechnicalContactUrl());
            smpHttp.setTechnicalInformationUrl(endpoint.getTechnicalInformationUrl());
            smpHttp.setSmpURI(endpoint.getEndpointURI());

            if (smpfields.getExtension().isEnable()) {
                try (Scanner scanner = new Scanner(smpHttp.getSmpFile(), StandardCharsets.UTF_8.name())) {
                    String content = scanner.useDelimiter("\\Z").next();
                    String capturedString = content.substring(content.indexOf("<Extension>"), content.indexOf("</Extension>"));
                    String[] endA = capturedString.split("<Extension>");
                    logger.debug("\n*****Content from Extension 1 : \n '{}'", endA[1]);

                    Document docOriginal = smpconverter.parseDocument(endA[1]);
                    docOriginal.getDocumentElement().normalize();


                } catch (IOException ex) {
                    logger.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (SAXException ex) {
                    logger.error("\n SAXException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (ParserConfigurationException ex) {
                    logger.error("\n ParserConfigurationException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                }
            }

        } else if ("Redirect".equals(type)) {
            RedirectType redirect = serviceMetadata.getRedirect();
            smpHttp.setCertificateUID(redirect.getCertificateUID());
            smpHttp.setHref(redirect.getHref());
        }

        SMPUpdateFields resp = new SMPUpdateFields();
        resp.setSmpFileName(smpHttp.getSmpFile().getAbsoluteFile().toString());
        resp.setSmpFileTypeId(smpHttp.getDocumentType());
        resp.setSmpFileTypeDescription(SMPType.valueOf(smpHttp.getDocumentType()).getDescription());
        resp.setSmpFileCountryId(smpHttp.getCountry().name());
        resp.setSmpFileCountryDescription(Countries.valueOf(smpHttp.getCountry().name()).getDescription());
        resp.setSigned(isSigned);

        /* set values read from file */
        smpfields.getUri().setCurrValue(smpHttp.getSmpURI());
        smpfields.getIssuanceType().setCurrValue(smpHttp.getIssuanceType());
        smpfields.getServiceActivationDate().setCurrValue(smpHttp.getServiceActivationDate());
        smpfields.getServiceExpirationDate().setCurrValue(smpHttp.getServiceExpirationDate());
        smpfields.getCertificate().setCurrValue(smpHttp.getCertificateContent());
        smpfields.getServiceDescription().setCurrValue(smpHttp.getServiceDescription());
        smpfields.getTechnicalContactUrl().setCurrValue(smpHttp.getTechnicalContactUrl());
        smpfields.getTechnicalInformationUrl().setCurrValue(smpHttp.getTechnicalInformationUrl());
        smpfields.getRequireBusinessLevelSignature().setCurrValue(smpHttp.getRequiredBusinessLevelSig());
        smpfields.getMinimumAuthLevel().setCurrValue(smpHttp.getMinimumAutenticationLevel());
        smpfields.getRedirectHref().setCurrValue(smpHttp.getHref());
        smpfields.getCertificateUID().setCurrValue(smpHttp.getCertificateUID());

        resp.setFields(smpfields);
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "smpeditor/updater/cleanSmpFile")
    public ResponseEntity cleanFile(@RequestBody SMPHttp smpHttp) {
        if (smpHttp.getSmpFile() != null) {
            boolean delete = smpHttp.getSmpFile().delete();
            logger.debug("\n****DELETED ? '{}'", delete);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "smpeditor/updater/getSmpTypeDescription")
    public ResponseEntity<String> getSmpTypeDescription(@RequestParam String val) {
        SMPType resp = SMPType.valueOf(val);
        return ResponseEntity.ok(resp.getDescription());
    }

    @GetMapping(path = "smpeditor/updater/getSmpCountryDescription")
    public ResponseEntity<String> getSmpCountryDescription(@RequestParam String val) {
        Countries resp = Countries.valueOf(val);
        return ResponseEntity.ok(resp.getDescription());
    }

}

