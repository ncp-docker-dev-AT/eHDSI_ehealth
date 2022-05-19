package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.web.rest;

import eu.europa.ec.sante.ehdsi.openncp.gateway.error.ApiException;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.Constants;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.cfg.ReadSMPProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.*;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.SimpleErrorHandler;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.BdxSmpValidator;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.SMPConverter;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.FileUtil;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ehdsi.EndpointType;
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
    private final Environment environment;
    private final SMPConverter smpconverter;
    private final ReadSMPProperties readProperties;

    @Autowired
    public SMPUpdateFileController(SMPConverter smpconverter, ReadSMPProperties readProperties, Environment environment) {
        logger.debug("SMPUpdateFileController('{}', '{}', '{}'", smpconverter, environment, readProperties);
        this.readProperties = readProperties;
        this.smpconverter = smpconverter;
        this.environment = environment;
    }


    @PostMapping(path = "/smpeditor/updater/setSmpFileToUpdate")
    public ResponseEntity<SMPUpdateFields> createSMPHttp(@RequestPart MultipartFile multipartFile) throws IOException {

        FileUtil.initializeFolders(Constants.SMP_DIR_PATH);
        File smpFile = new File(Constants.SMP_DIR_PATH + File.separator + multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(smpFile);
        } catch (IOException | IllegalStateException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getClass().getSimpleName());
        }

        /*Validate xml file*/
        String contentFile = new String(Files.readAllBytes(Paths.get(smpFile.getPath())));
        if (!BdxSmpValidator.validateFile(contentFile)) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, environment.getProperty("error.notsmp"));
        }

        /*Read data from xml*/
        Object o = smpconverter.convertFromXml(smpFile);
        ServiceMetadata serviceMetadata;
        if (o instanceof SignedServiceMetadata) {
            serviceMetadata = ((SignedServiceMetadata) o).getServiceMetadata();
        } else {
            serviceMetadata = (ServiceMetadata) o;
        }

        boolean isSigned;
        Countries country;

        SMPType documentType = getDocumentType(serviceMetadata);
        SMPFields smpfields = readProperties.readProperties(documentType);

        if (documentType.equals(SMPType.Redirect)) {

            smpfields.getRedirectHref().setCurrValue(serviceMetadata.getRedirect().getCertificateUID());
            smpfields.getCertificateUID().setCurrValue(serviceMetadata.getRedirect().getHref());

            isSigned = !serviceMetadata.getRedirect().getExtensions().isEmpty();

            String participantID;
            Pattern pattern = Pattern.compile(environment.getProperty("ParticipantIdentifier.Scheme") + ".*");//SPECIFICATION
            Matcher matcher = pattern.matcher(serviceMetadata.getRedirect().getHref());
            if (matcher.find()) {

                String result = matcher.group(0);
                result = URLDecoder.decode(result, StandardCharsets.UTF_8);
                String[] ids = result.split("/services/");
                participantID = ids[0];
                String[] cc = participantID.split(":");

                try {
                    country = Countries.valueOf(cc[4]);
                } catch (IllegalArgumentException e) {
                    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, environment.getProperty("error.redirect.href.participantID"));
                }

            } else {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, environment.getProperty("error.redirect.href"));
            }

        } else {

            isSigned = !serviceMetadata.getServiceInformation().getExtensions().isEmpty();

            String documentIdentifier = serviceMetadata.getServiceInformation().getDocumentIdentifier().getValue();

            Map<String, String> propertiesMap = readProperties.readPropertiesFile();
            Set<Map.Entry<String, String>> set2 = propertiesMap.entrySet();
            for (Map.Entry<String, String> aSet2 : set2) {
                if (documentIdentifier.equals(aSet2.getKey())) {
                    String[] docs = aSet2.getValue().split("\\.");
                    //  Country_B_Identity_Provider case: can have two different DocIds
                    if (docs.length > 2) {
                        smpfields.getIssuanceType().setCurrValue(docs[2]);
                    }
                    break;
                }
            }

            String participantIdentifier = serviceMetadata.getServiceInformation().getParticipantIdentifier().getValue();
            String[] cc = participantIdentifier.split(":");//SPECIFICATION

            try {
                country = Countries.valueOf(cc[2]);
            } catch (IllegalArgumentException e) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, environment.getProperty("error.serviceinformation.participantID"));
            }

            EndpointType endpoint = serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0);

            smpfields.getRequireBusinessLevelSignature().setCurrValue(endpoint.isRequireBusinessLevelSignature());
            smpfields.getMinimumAuthLevel().setCurrValue(endpoint.getMinimumAuthenticationLevel());
            smpfields.getServiceDescription().setCurrValue(endpoint.getServiceDescription());
            smpfields.getTechnicalContactUrl().setCurrValue(endpoint.getTechnicalContactUrl());
            smpfields.getTechnicalInformationUrl().setCurrValue(endpoint.getTechnicalInformationUrl());
            smpfields.getUri().setCurrValue(endpoint.getEndpointURI());

            X509Certificate cert;
            String subjectName = null;
            if (smpfields.getCertificate().isEnable()) {
                try {
                    InputStream in = new ByteArrayInputStream(endpoint.getCertificate());
                    cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
                    if (cert != null) {
                        subjectName = "Issuer: " + cert.getIssuerX500Principal().getName() + "\nSerial Number #"
                                + cert.getSerialNumber();
                        smpfields.getCertificate().setCurrValue(subjectName);
                    }
                } catch (CertificateException ex) {
                    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "CertificateException");
                }
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date cal = endpoint.getServiceActivationDate().getTime();
            String formatted = format.format(cal);
            Date cal2 = endpoint.getServiceExpirationDate().getTime();
            String formatted2 = format.format(cal2);
            try {
                smpfields.getServiceActivationDate().setCurrValue(format.parse(formatted));
                smpfields.getServiceExpirationDate().setCurrValue(format.parse(formatted2));
            } catch (ParseException ex) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "ParseException");
            }

            if (smpfields.getExtension().isEnable()) {
                try (Scanner scanner = new Scanner(smpFile, StandardCharsets.UTF_8.name())) {
                    String content = scanner.useDelimiter("\\Z").next();
                    String capturedString = content.substring(content.indexOf("<Extension>"), content.indexOf("</Extension>"));
                    String[] endA = capturedString.split("<Extension>");
                    smpfields.getExtension().setCurrValue(endA[1]);
                } catch (IOException ex) {
                    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "IOException");
                }
            }
        }

        SMPUpdateFields resp = new SMPUpdateFields();
        resp.setSmpFileName(smpFile.getAbsoluteFile().toString());
        resp.setSmpFileTypeId(documentType.name());
        resp.setSmpFileTypeDescription(documentType.getDescription());
        resp.setSmpFileCountryId(country.name());
        resp.setSmpFileCountryDescription(country.getDescription());
        resp.setSigned(isSigned);
        resp.setFields(smpfields);

        return ResponseEntity.ok(resp);
    }

    private SMPType getDocumentType(ServiceMetadata serviceMetadata) {
        if (serviceMetadata.getRedirect() != null) {
            return SMPType.Redirect;
        } else if (serviceMetadata.getServiceInformation() != null) {

            String documentID = "";
            Map<String, String> propertiesMap = readProperties.readPropertiesFile();
            Set<Map.Entry<String, String>> set2 = propertiesMap.entrySet();

            for (Map.Entry<String, String> aSet2 : set2) {
                if (serviceMetadata.getServiceInformation().getDocumentIdentifier().getValue().equals(aSet2.getKey())) {
                    String[] docs = aSet2.getValue().split("\\.");
                    documentID = docs[0];
                    break;
                }
            }

            for (SMPType smptype1 : SMPType.getAll()) {
                if (smptype1.name().equals(documentID)) {
                    return smptype1;
                }
            }
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not find SMP document type");
    }

}

