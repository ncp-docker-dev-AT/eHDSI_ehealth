package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.web.rest;

import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.error.ApiException;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.cfg.ReadSMPProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.ReferenceCollection;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.SMPType;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.AuditManager;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.DynamicDiscoveryService;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.SimpleErrorHandler;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.DynamicDiscoveryClient;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.util.HttpUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

@RestController
@RequestMapping(path = "/api")
public class SMPDeleteFileController {

    private final Logger logger = LoggerFactory.getLogger(SMPDeleteFileController.class);
    private final DynamicDiscoveryClient dynamicDiscoveryClient;
    private final ReadSMPProperties readProperties;
    private final Environment env;

    @Autowired
    public SMPDeleteFileController(DynamicDiscoveryClient dynamicDiscoveryClient, ReadSMPProperties readProperties, Environment env) {
        this.dynamicDiscoveryClient = dynamicDiscoveryClient;
        this.readProperties = readProperties;
        this.env = env;
    }

    @GetMapping(path = "smpeditor/smpfileinfo")
    public ResponseEntity<List<ReferenceCollection>> getFilesToDeleteInfo(String countryName) {

        String partID = "urn:ehealth:" + countryName + ":ncp-idp";
        String partScheme = env.getProperty("ParticipantIdentifier.Scheme");

        boolean success = true;
        String errorType = "";
        ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(partID, partScheme);
        DynamicDiscovery smpClient = null;

        try {
            smpClient = dynamicDiscoveryClient.getInstance();
        } catch (ConnectionException ex) {
            success = false;
            errorType = "ConnectionException";
            logger.error("\n ConnectionException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (TechnicalException | CertificateException | KeyStoreException | IOException |
                 NoSuchAlgorithmException e) {
            logger.error("Technical Exception: '{}'", e.getMessage(), e);
        }

        List<DocumentIdentifier> documentIdentifiers = null;
        try {
            documentIdentifiers = smpClient.getService().getServiceGroup(participantIdentifier).getDocumentIdentifiers();
        } catch (TechnicalException ex) {
            success = false;
            errorType = "TechnicalException";
            logger.error("TechnicalException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        URI serviceGroup = null;
        List<ReferenceCollection> referenceCollection = new ArrayList<>();
        URI smpURI = null;
        int i = 0;

        if (!documentIdentifiers.isEmpty()) {
            for (DocumentIdentifier documentIdentifier : documentIdentifiers) {
                String smptype = "Unknown type";
                String documentID = "";
                Map<String, String> propertiesMap = readProperties.readPropertiesFile();
                Set set2 = propertiesMap.entrySet();
                for (Object aSet2 : set2) {
                    Map.Entry mentry2 = (Map.Entry) aSet2;
                    if (StringUtils.equalsIgnoreCase(documentIdentifier.getIdentifier(), mentry2.getKey().toString())) {
                        String[] docs = mentry2.getValue().toString().split("\\.");
                        documentID = docs[0];
                        break;
                    }
                }
                String smpType = documentID;
                logger.debug("\n******** DOC ID - '{}'", documentIdentifier.getIdentifier());
                logger.debug("\n******** SMP Type - '{}'", smpType);

                for (SMPType smptype1 : SMPType.values()) {
                    if (smptype1.name().equals(smpType)) {
                        smptype = smptype1.getDescription();
                        break;
                    }
                }

                try {
                    smpURI = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
                } catch (TechnicalException ex) {
                    success = false;
                    errorType = "TechnicalException";
                    logger.error("\n TechnicalException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                }
                URI uri = smpClient.getService().getMetadataProvider().resolveServiceMetadata(smpURI, participantIdentifier, documentIdentifier);
                ReferenceCollection reference = new ReferenceCollection();
                reference.setReference(uri.toString());
                reference.setSmpType(smptype);
                reference.setSmpUri(smpURI.toString());
                reference.setId(i++);
                referenceCollection.add(reference);

                serviceGroup = smpClient.getService().getMetadataProvider().resolveDocumentIdentifiers(smpURI, participantIdentifier);
            }
        } else {
            logger.info("Smp file list is empty");
            //  Returning empty List and  no Audit message expected in this case.
            return ResponseEntity.ok(Collections.emptyList());
        }

        //Audit
        String objectID = serviceGroup.toString();
        byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());

        if (success) {
            AuditManager.handleDynamicDiscoveryQuery(smpURI.toString(), new String(encodedObjectID), null, null);
            return ResponseEntity.ok(referenceCollection);
        } else {
            AuditManager.handleDynamicDiscoveryQuery(smpURI.toString(), new String(encodedObjectID), "500", errorType.getBytes());
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, errorType);
        }
    }

    @DeleteMapping(value = "smpeditor/smpfile")
    public ResponseEntity deleteSmpFile(@RequestBody ReferenceCollection ref) {

        logger.info("[REST Api] Delete SMP file: '{}' from server: '{}'", ref.getReference(), ref.getSmpUri());
        ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();

        String urlServer = configurationManager.getProperty(StandardProperties.SMP_SML_ADMIN_URL);
        if (urlServer.endsWith("/")) {
            urlServer = urlServer.substring(0, urlServer.length() - 1);
        }
        /*Removes https:// from entered by the user so it won't repeat in uri set scheme*/
        if (urlServer.startsWith("https")) {
            urlServer = urlServer.substring(8);
        }

        String reference = ref.getReference();
        String smpType = ref.getSmpType();
        logger.debug("SMP Type- '{}', reference - '{}'", smpType, reference);

        if (reference.startsWith("http://") || reference.startsWith("https://")) {
            reference = reference.substring(ref.getSmpUri().length());
        }

        reference = java.net.URLDecoder.decode(reference, StandardCharsets.UTF_8);
        logger.debug("references decoded UTF-8 - {}", reference);

        URI uri;
        try {
            uri = new URIBuilder().setScheme("https").setHost(urlServer).setPath(reference).build();
            logger.debug("\n ************** URI - {}", uri);
        } catch (URISyntaxException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "URISyntaxException");
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = HttpUtil.createSSLContext();

        //DELETE
        HttpDelete httpdelete = new HttpDelete(uri);

        CloseableHttpResponse response;
        try {
            response = DynamicDiscoveryService.buildHttpClient(sslcontext).execute(httpdelete);
        } catch (IOException ex) {
            logger.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        /*Get response*/
        int responseStatus = response.getStatusLine().getStatusCode();
        String responseReason = response.getStatusLine().getReasonPhrase();
        logger.debug("Delete Response Status - {}, Reason - {} ", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());

        HttpEntity entity = response.getEntity();

        //Audit vars
        String remoteIp = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_ADMIN_URL");
        //ET_ObjectID --> Base64 of url
        String objectID = uri.toString();
        byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());

        switch (responseStatus) {
            case 503:
            case 405:
                //Audit error
                byte[] encodedObjectDetail = Base64.encodeBase64(responseReason.getBytes());
                AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                        Integer.toString(responseStatus), encodedObjectDetail);
                throw new ApiException(HttpStatus.valueOf(responseStatus), env.getProperty("error.server.failed"));
            case 401:
                //Audit error
                encodedObjectDetail = Base64.encodeBase64(responseReason.getBytes());
                AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                        Integer.toString(responseStatus), encodedObjectDetail);
                throw new ApiException(HttpStatus.valueOf(responseStatus), env.getProperty("error.nouser"));
            case 200:
            case 201:
                //Audit Success
                AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                        null, null);
                return ResponseEntity.ok().build();


        }
        /* Get BusinessCode and ErrorDescription from response */

        //Save InputStream of response in ByteArrayOutputStream in order to read it more than once.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            IOUtils.copy(entity.getContent(), byteArrayOutputStream);
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "\n IOException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (UnsupportedOperationException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "\n UnsupportedOperationException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();

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
                }
                if (nodes.item(j).getNodeName().equals("ErrorDescription")) {
                    String errorDescription = nodes.item(j).getTextContent();
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getClass().getName() + " - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        /*transform xml to string in order to send in Audit*/
        String errorResult = AuditManager.prepareEventLog(bytes);
        logger.debug("\n ***************** ERROR RESULT - '{}'", errorResult);
        //Audit error
        AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                Integer.toString(responseStatus), errorResult.getBytes());

        return ResponseEntity.status(responseStatus).build();
    }

    @PostMapping(value = "smpeditor/deleteSmpFile")
    public ResponseEntity postDeleteSmpFile(@RequestBody ReferenceCollection ref) {

        ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();
        String urlServer = configurationManager.getProperty(StandardProperties.SMP_SML_ADMIN_URL);
        if (urlServer.endsWith("/")) {
            urlServer = urlServer.substring(0, urlServer.length() - 1);
        }
        //  Removes https:// from entered by the user so it won't repeat in uri set scheme
        if (urlServer.startsWith("https")) {
            urlServer = urlServer.substring(8);
        }

        String reference = ref.getReference();
        String smpType = ref.getSmpType();
        logger.debug("SMP Type- '{}', reference - '{}'", smpType, reference);

        if (reference.startsWith("http://") || reference.startsWith("https://")) {
            reference = reference.substring(ref.getSmpUri().length());
        }

        reference = java.net.URLDecoder.decode(reference, StandardCharsets.UTF_8);
        logger.debug("references decoded UTF-8 - {}", reference);

        URI uri = null;
        try {
            uri = new URIBuilder().setScheme("https").setHost(urlServer).setPath(reference).build();
            logger.debug("\n ************** URI - {}", uri);
        } catch (URISyntaxException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "URISyntaxException");
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = HttpUtil.createSSLContext();

        //DELETE
        HttpDelete httpdelete = new HttpDelete(uri);

        CloseableHttpResponse response;

        try {
            response = DynamicDiscoveryService.buildHttpClient(sslcontext).execute(httpdelete);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(e));
        }

        //  Get response
        int responseStatus = response.getStatusLine().getStatusCode();
        String responseReason = response.getStatusLine().getReasonPhrase();
        logger.debug("Delete Response Status - {}, Reason - {} ", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());

        HttpEntity entity = response.getEntity();

        //  Audit vars
        String remoteIp = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_ADMIN_URL");
        //  ET_ObjectID --> Base64 of url
        String objectID = uri.toString();
        byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());

        switch (responseStatus) {
            case 503:
            case 405:
                //Audit error
                byte[] encodedObjectDetail = Base64.encodeBase64(responseReason.getBytes());
                AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                        Integer.toString(responseStatus), encodedObjectDetail);
                throw new ApiException(HttpStatus.valueOf(responseStatus), env.getProperty("error.server.failed"));
            case 401:
                //Audit error
                encodedObjectDetail = Base64.encodeBase64(responseReason.getBytes());
                AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                        Integer.toString(responseStatus), encodedObjectDetail);
                throw new ApiException(HttpStatus.valueOf(responseStatus), env.getProperty("error.nouser"));
            case 200:
            case 201:
                //Audit Success
                AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                        null, null);
                return ResponseEntity.ok().build();
        }
        /* Get BusinessCode and ErrorDescription from response */

        //Save InputStream of response in ByteArrayOutputStream in order to read it more than once.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            IOUtils.copy(entity.getContent(), byteArrayOutputStream);
        } catch (IOException | UnsupportedOperationException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getClass().getName() + " - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();

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
                }
                if (nodes.item(j).getNodeName().equals("ErrorDescription")) {
                    String errorDescription = nodes.item(j).getTextContent();
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getClass().getName() + " - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        /*transform xml to string in order to send in Audit*/
        String errorResult = AuditManager.prepareEventLog(bytes);
        logger.debug("\n ***************** ERROR RESULT - '{}'", errorResult);
        //Audit error
        AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                Integer.toString(responseStatus), errorResult.getBytes());

        return ResponseEntity.status(responseStatus).build();
    }

}
