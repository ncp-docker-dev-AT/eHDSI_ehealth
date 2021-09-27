package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.web.rest;

import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.Constants;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.SMPFileOps;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.SMPHttp;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.AuditManager;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.DynamicDiscoveryService;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.SimpleErrorHandler;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.BdxSmpValidator;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.DynamicDiscoveryClient;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.SMPConverter;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.util.HttpUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ehdsi.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@RequestMapping(path = "/api")
public class SMPUploadFileController {

    private final Logger logger = LoggerFactory.getLogger(SMPUploadFileController.class);
    private final SMPConverter smpconverter;
    private final Environment environment;
    private final DynamicDiscoveryClient dynamicDiscoveryClient;

    public SMPUploadFileController(SMPConverter smpconverter, Environment environment, DynamicDiscoveryClient dynamicDiscoveryClient) {
        this.dynamicDiscoveryClient = dynamicDiscoveryClient;
        this.smpconverter = smpconverter;
        this.environment = environment;
    }

    @PostMapping(path = "/smpeditor/uploader/fromSmpFileOps")
    public ResponseEntity<SMPHttp> createSMPFileOps(@RequestBody SMPFileOps smpFileOps) {

        if (smpFileOps.getGeneratedFile() == null) {
            throw new RuntimeException("The requested file does not exists");
        }
        File file = new File(smpFileOps.getGeneratedFile().getPath());
        SMPHttp smpHttp = new SMPHttp();
        smpHttp.setSmpFile(file);
        return ResponseEntity.ok(smpHttp);
    }

    @PostMapping(path = "/smpeditor/uploader/fileToUpload")
    public ResponseEntity<SMPHttp> createSMPHttp(@RequestPart MultipartFile multipartFile) {
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
        return ResponseEntity.ok(smpHttp);

    }

    @PostMapping(path = "smpeditor/uploader/upload")
    public ResponseEntity<SMPHttp> uploadToServer(@RequestBody SMPHttp smpHttp) throws Exception {

        String contentFile = new String(Files.readAllBytes(Paths.get(smpHttp.getSmpFile().getPath())));
        boolean fileDeleted;

        if (!BdxSmpValidator.validateFile(contentFile)) {
            fileDeleted = smpHttp.getSmpFile().delete();
            if (logger.isDebugEnabled()) {
                logger.debug("Converted File deleted: '{}'", fileDeleted);
            }
            logger.error(environment.getProperty("error.notsmp"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


        Object fileConverted = smpconverter.convertFromXml(smpHttp.getSmpFile());
        if (smpconverter.isSignedServiceMetadata(fileConverted)) {
            logger.error(environment.getProperty("warning.isSigned.sigmenu"));
            fileDeleted = smpHttp.getSmpFile().delete();
            logger.debug("Converted File deleted: '{}'", fileDeleted);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        ServiceMetadata serviceMetadata = smpconverter.getServiceMetadata(fileConverted);

        String participantID = "";
        String documentTypeID = "";
        String partID = "";
        String partScheme = "";
        String docID = "";
        String docScheme = "";

        if (serviceMetadata.getRedirect() != null) {
            logger.debug("\n******** REDIRECT");

            if (serviceMetadata.getRedirect().getExtensions().isEmpty()) {
                logger.error(environment.getProperty("error.notsigned")); //messages.properties
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            /* Check if url of redirect is correct */
            String href = serviceMetadata.getRedirect().getHref();
            Pattern pattern = Pattern.compile("ehealth-participantid-qns.*");
            Matcher matcher = pattern.matcher(href);
            if (matcher.find()) {
                String result = matcher.group(0);
                result = java.net.URLDecoder.decode(result, StandardCharsets.UTF_8);
                String[] ids = result.split("/services/");
                participantID = ids[0];
                documentTypeID = ids[1];
            } else {
                logger.error(environment.getProperty("error.redirect.href")); //messages.properties
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

        } else if (serviceMetadata.getServiceInformation() != null) {
            logger.debug("\n******** SERVICE INFORMATION");

            if (serviceMetadata.getServiceInformation().getExtensions().isEmpty()) {
                logger.error(environment.getProperty("error.notsigned")); //messages.properties
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            partID = serviceMetadata.getServiceInformation().getParticipantIdentifier().getValue();
            partScheme = serviceMetadata.getServiceInformation().getParticipantIdentifier().getScheme();
            participantID = partScheme + "::" + partID;

            docID = serviceMetadata.getServiceInformation().getDocumentIdentifier().getValue();
            docScheme = serviceMetadata.getServiceInformation().getDocumentIdentifier().getScheme();
            documentTypeID = docScheme + "::" + docID;
        }

        String urlServer = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_ADMIN_URL");
        if (urlServer.endsWith("/")) {
            urlServer = urlServer.substring(0, urlServer.length() - 1);
        }

        String serviceMetdataUrl = "/" + participantID + "/services/" + documentTypeID;

        // Removes https:// from entered by the user so it won't repeat in uri set scheme
        if (urlServer.startsWith("https")) {
            urlServer = urlServer.substring(8);
        }

        logger.info("Build SMP Admin Uri: '{}' - '{}'", urlServer, serviceMetdataUrl);

        URI uri = null;
        try {

            uri = new URIBuilder().setScheme("https").setHost(urlServer).setPath(serviceMetdataUrl).build();
        } catch (URISyntaxException ex) {
            logger.error("URISyntaxException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        logger.info("SMP Uri endpoint: '{}'", uri);

        String content = "";
        try (Scanner scanner = new Scanner(smpHttp.getSmpFile(), StandardCharsets.UTF_8.name())) {
            content = scanner.useDelimiter("\\Z").next();
        } catch (IOException ex) {
            logger.error("IOException: '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        StringEntity entityPut = new StringEntity(content, ContentType.create("application/xml", "UTF-8"));
        if (logger.isDebugEnabled()) {
            logger.debug("Entity that will be put on the SMP server : '{}'", IOUtils.toString(entityPut.getContent(), StandardCharsets.UTF_8));
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = HttpUtil.createSSLContext();

        //PUT
        HttpPut httpput = new HttpPut(uri);
        httpput.setEntity(entityPut);
        CloseableHttpResponse response;
        try {
            response = DynamicDiscoveryService.buildHttpClient(sslcontext).execute(httpput);
        } catch (IOException ex) {
            logger.error("IOException response - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
            logger.error(environment.getProperty("error.server.failed")); //messages.properties
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // Get Http Client response
        smpHttp.setStatusCode(response.getStatusLine().getStatusCode());
        org.apache.http.HttpEntity entity = response.getEntity();

        logger.debug("Http Client Response Status Code: '{}' - Reason: '{}'", response.getStatusLine().getStatusCode(),
                response.getStatusLine().getReasonPhrase());

        //Audit vars
        ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();
        String ncp = configurationManager.getProperty("ncp.country");
        String ncpemail = configurationManager.getProperty("ncp.email");
        String country = configurationManager.getProperty("COUNTRY_PRINCIPAL_SUBDIVISION");
        String serverSMPUrl = configurationManager.getProperty(StandardProperties.SMP_SML_ADMIN_URL);//Source Gateway
        String smp = configurationManager.getProperty(StandardProperties.SMP_SML_SUPPORT);
        String smpemail = configurationManager.getProperty(StandardProperties.SMP_SML_SUPPORT_EMAIL);

        //ET_ObjectID --> Base64 of url
        String objectID = uri.toString(); //ParticipantObjectID
        byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());

        logger.info("SMP Put request response code: '{}'", smpHttp.getStatusCode());
        if (smpHttp.getStatusCode() == 404 || smpHttp.getStatusCode() == 503 || smpHttp.getStatusCode() == 405) {
            //Audit Error
            byte[] encodedObjectDetail = Base64.encodeBase64(response.getStatusLine().getReasonPhrase().getBytes());
            AuditManager.handleDynamicDiscoveryPush(serverSMPUrl, new String(encodedObjectID),
                    Integer.toString(response.getStatusLine().getStatusCode()), encodedObjectDetail);

            logger.error(environment.getProperty("error.server.failed")); //messages.properties
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } else if (smpHttp.getStatusCode() == 401) {
            //Audit Error
            byte[] encodedObjectDetail = Base64.encodeBase64(response.getStatusLine().getReasonPhrase().getBytes());
            AuditManager.handleDynamicDiscoveryPush(serverSMPUrl, new String(encodedObjectID),
                    Integer.toString(response.getStatusLine().getStatusCode()), encodedObjectDetail);

            logger.error(environment.getProperty("error.nouser")); //messages.properties
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (!(smpHttp.getStatusCode() == 200 || smpHttp.getStatusCode() == 201)) {
            /* Get BusinessCode and ErrorDescription from response */

            //Save InputStream of response in ByteArrayOutputStream in order to read it more than once.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                IOUtils.copy(entity.getContent(), baos);
            } catch (IOException ex) {
                logger.error("IOException response: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (UnsupportedOperationException ex) {
                logger.error("UnsupportedOperationException response: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            }
            byte[] bytes = baos.toByteArray();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(bais);
                Element element = doc.getDocumentElement();
                NodeList nodes = element.getChildNodes();
                for (int j = 0; j < nodes.getLength(); j++) {
                    if (nodes.item(j).getNodeName().equals("BusinessCode")) {
                        String businessCode = nodes.item(j).getTextContent();
                        smpHttp.setBusinessCode(businessCode);
                    }
                    if (nodes.item(j).getNodeName().equals("ErrorDescription")) {
                        String errorDescription = nodes.item(j).getTextContent();
                        smpHttp.setErrorDescription(errorDescription);
                    }
                }
            } catch (ParserConfigurationException ex) {
                logger.error("ParserConfigurationException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (SAXException ex) {
                logger.error("SAXException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (IOException ex) {
                logger.error("IOException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            // Transform XML to String in order to send in Audit
            String errorResult = AuditManager.prepareEventLog(bytes);
            logger.debug("Error Result: '{}", errorResult);
            //Audit error
            AuditManager.handleDynamicDiscoveryPush(serverSMPUrl, new String(encodedObjectID),
                    Integer.toString(response.getStatusLine().getStatusCode()), errorResult.getBytes(StandardCharsets.UTF_8));
        }

        if (smpHttp.getStatusCode() == 200 || smpHttp.getStatusCode() == 201) {
            //Audit Success
            AuditManager.handleDynamicDiscoveryPush(serverSMPUrl, new String(encodedObjectID),
                    null, null);
        }

        //GET
        boolean success = true;
        String errorType = "";
        ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(partID, partScheme);
        DocumentIdentifier documentIdentifier = new DocumentIdentifier(docID, docScheme);

        logger.info("Instantiating DynamicDiscovery: '{}', '{}', '{}', '{}'", partID, partScheme, docID, docScheme);
        DynamicDiscovery smpClient = dynamicDiscoveryClient.getInstance();

        if (smpClient == null) {
            throw new Exception("Cannot instantiate SMPClient!!!!");
        }
        URI smpURI = null;
        try {
            smpURI = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
        } catch (TechnicalException ex) {
            success = false;
            errorType = "TechnicalException";
            logger.error("TechnicalException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        URI serviceGroup = smpClient.getService().getMetadataProvider().resolveDocumentIdentifiers(smpURI, participantIdentifier);
        URI serviceMetadataUri = smpClient.getService().getMetadataProvider().resolveServiceMetadata(smpURI, participantIdentifier, documentIdentifier);

        logger.info("URI ServiceGroup: '{}'\nURI serviceMetadataUri: '{}'", serviceGroup.toASCIIString(), serviceMetadataUri.toASCIIString());

        smpHttp.setServiceGroupUrl(serviceGroup.toString());
        smpHttp.setSignedServiceMetadataUrl(serviceMetadataUri.toString());


        return ResponseEntity.ok(smpHttp);

    }


    @PostMapping(value = "smpeditor/uploader/clean")
    public ResponseEntity cleanFile(@RequestBody SMPHttp smpHttp) {
        if (smpHttp.getSmpFile() != null) {
            boolean delete = smpHttp.getSmpFile().delete();
            logger.debug("\n****DELETED ? '{}'", delete);
        }

        return ResponseEntity.ok().build();
    }
}
