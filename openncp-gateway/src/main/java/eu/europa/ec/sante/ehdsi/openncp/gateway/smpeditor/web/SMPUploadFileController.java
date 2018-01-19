package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.web;

import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.Constants;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.Alert;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.SMPHttp;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.eu.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ines Garganta
 */
@Controller
@SessionAttributes("smpupload")
public class SMPUploadFileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMPUploadFileController.class);

    private SMPConverter smpconverter = new SMPConverter();

    private XMLValidator xmlValidator = new XMLValidator();

    private Environment env;

    @Autowired
    public SMPUploadFileController(SMPConverter smpconverter, XMLValidator xmlValidator, Environment env) {
        LOGGER.debug("Constructor SMPUploadFileController({}, {}, {})", smpconverter.toString(), xmlValidator.toString(), env.toString());
        this.smpconverter = smpconverter;
        this.xmlValidator = xmlValidator;
        this.env = env;
    }

    /**
     * Generate UploadFile page
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/smpeditor/uploadsmpfile", method = RequestMethod.GET)
    public String uploadFile(Model model) {
        LOGGER.debug("\n==== in uploadFile ====");
        model.addAttribute("smpupload", new SMPHttp());

        return "smpeditor/uploadsmpfile";
    }

    /**
     * UPLOAD files to server
     *
     * @param smpupload
     * @param model
     * @param redirectAttributes
     * @return
     */
    @RequestMapping(value = "smpeditor/uploadsmpfile", method = RequestMethod.POST)
    public String postUpload(@ModelAttribute("smpupload") SMPHttp smpupload, Model model, final RedirectAttributes redirectAttributes) throws Exception {
        LOGGER.debug("\n==== in postUpload ====");
        model.addAttribute("smpupload", smpupload);

        /*Iterate through all chosen files*/
        List<SMPHttp> allItems = new ArrayList<>();
        for (int i = 0; i < smpupload.getUploadFiles().size(); i++) {
            SMPHttp itemUpload = new SMPHttp();

            itemUpload.setUploadFileName(smpupload.getUploadFiles().get(i).getOriginalFilename());

            File convFile = new File(Constants.SMP_DIR_PATH + smpupload.getUploadFiles().get(i).getOriginalFilename());
            try {
                smpupload.getUploadFiles().get(i).transferTo(convFile);
            } catch (IOException ex) {
                LOGGER.error("IOException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (IllegalStateException ex) {
                LOGGER.error("IllegalStateException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            boolean valid = xmlValidator.validator(convFile.getPath());
            boolean fileDeleted;

            if (valid) {
                LOGGER.debug("\n****VALID XML File");
            } else {
                LOGGER.debug("\n****NOT VALID XML File");
                String message = env.getProperty("error.notsmp"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                fileDeleted = convFile.delete();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Converted File deleted: '{}'", fileDeleted);
                }
                return "redirect:/smpeditor/uploadsmpfile";
            }
            fileDeleted = convFile.delete();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Converted File deleted: '{}'", fileDeleted);
            }

            ServiceMetadata serviceMetadata = smpconverter.convertFromXml(smpupload.getUploadFiles().get(i));

            boolean isSigned = smpconverter.getIsSignedServiceMetadata();
            if (isSigned) {
                LOGGER.debug("\n****SIGNED SMP File");
                fileDeleted = convFile.delete();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Converted File deleted: '{}'", fileDeleted);
                }
                String message = env.getProperty("warning.isSigned.sigmenu"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.warning));
                return "redirect:/smpeditor/uploadsmpfile";
            } else {
                LOGGER.debug("\n****NOT SIGNED File");
            }

            String participantID = "";
            String documentTypeID = "";
            String partID = "";
            String partScheme = "";
            String docID = "";
            String docScheme = "";

            if (serviceMetadata.getRedirect() != null) {
                LOGGER.debug("\n******** REDIRECT");

                if (serviceMetadata.getRedirect().getExtensions().isEmpty()) {
                    LOGGER.error("\n******* NOT SIGNED EXTENSION (National Authority signature)");
                    String message = env.getProperty("error.notsigned"); //messages.properties
                    redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                    return "redirect:/smpeditor/uploadsmpfile";
                }

                /* Check if url of redirect is correct */
                String href = serviceMetadata.getRedirect().getHref();
                Pattern pattern = Pattern.compile("ehealth-participantid-qns.*");
                Matcher matcher = pattern.matcher(href);
                if (matcher.find()) {
                    String result = matcher.group(0);
                    try {
                        result = java.net.URLDecoder.decode(result, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        LOGGER.error("UnsupportedEncodingException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
                    }
                    String[] ids = result.split("/services/");
                    participantID = ids[0];
                    documentTypeID = ids[1];
                } else {
                    LOGGER.error("\n****NOT VALID HREF IN REDIRECT");
                    String message = env.getProperty("error.redirect.href"); //messages.properties
                    redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                    return "redirect:/smpeditor/uploadsmpfile";
                }

            } else if (serviceMetadata.getServiceInformation() != null) {
                LOGGER.debug("\n******** SERVICE INFORMATION");

                if (serviceMetadata.getServiceInformation().getExtensions().isEmpty()) {
                    LOGGER.error("\n******* NOT SIGNED EXTENSION ");
                    String message = env.getProperty("error.notsigned"); //messages.properties
                    redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                    return "redirect:/smpeditor/uploadsmpfile";
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

            LOGGER.info("Build SMP Admin Uri: '{}' - '{}'", urlServer, serviceMetdataUrl);

            URI uri = null;
            try {
                uri = new URIBuilder()
                        .setScheme("https")
                        .setHost(urlServer)
                        .setPath(serviceMetdataUrl)
                        .build();
            } catch (URISyntaxException ex) {
                LOGGER.error("URISyntaxException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            LOGGER.info("SMP Uri endpoint: '{}'", uri);

            String content = "";
            try (Scanner scanner = new Scanner(smpupload.getUploadFiles().get(i).getInputStream(), StandardCharsets.UTF_8.name())) {
                content = scanner.useDelimiter("\\Z").next();
            } catch (IOException ex) {
                LOGGER.error("IOException: '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            StringEntity entityPut = new StringEntity(content, ContentType.create("application/xml", "UTF-8"));
            LOGGER.debug("Entity that will be put on the SMP server : " + IOUtils.toString(entityPut.getContent(), StandardCharsets.UTF_8));

            ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();

            PrivateKeyStrategy privatek = (map, socket) -> configurationManager.getProperty(StandardProperties.SMP_SML_CLIENT_KEY_ALIAS);

            // Trust own CA and all self-signed certs
            SSLContext sslcontext = null;
            try {
                //must be the same as SC_KEYSTORE_PASSWORD
                sslcontext = SSLContexts.custom()
                        .loadKeyMaterial(new File(configurationManager.getProperty(StandardProperties.NCP_KEYSTORE)),
                                configurationManager.getProperty(StandardProperties.NCP_KEYSTORE_PASSWORD).toCharArray(),
                                configurationManager.getProperty(StandardProperties.SMP_SML_CLIENT_KEY_PASSWORD).toCharArray(),
                                privatek)
                        .loadTrustMaterial(new File(configurationManager.getProperty(StandardProperties.NCP_TRUSTSTORE)),
                                configurationManager.getProperty(StandardProperties.NCP_TRUSTSTORE_PASSWORD).toCharArray(),
                                new TrustSelfSignedStrategy())
                        .build();
            } catch (NoSuchAlgorithmException ex) {
                LOGGER.error("NoSuchAlgorithmException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (KeyStoreException ex) {
                LOGGER.error("KeyStoreException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (CertificateException ex) {
                LOGGER.error("CertificateException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (IOException ex) {
                LOGGER.error("IOException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (KeyManagementException ex) {
                LOGGER.error("KeyManagementException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (UnrecoverableKeyException ex) {
                LOGGER.error("UnrecoverableKeyException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            //PUT
            HttpPut httpput = new HttpPut(uri);
            httpput.setEntity(entityPut);
            CloseableHttpResponse response;
            try {
                response = DynamicDiscoveryService.buildHttpClient(sslcontext).execute(httpput);
            } catch (IOException ex) {
                LOGGER.error("IOException response - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                String message = env.getProperty("error.server.failed"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                return "redirect:/smpeditor/uploadsmpfile";
            }

            // Get Http Client response
            itemUpload.setStatusCode(response.getStatusLine().getStatusCode());
            org.apache.http.HttpEntity entity = response.getEntity();

            LOGGER.debug("Http Client Response Status Code: '{}' - Reason: '{}'", response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase());

            //Audit vars
            String ncp = configurationManager.getProperty("ncp.country");
            String ncpemail = configurationManager.getProperty("ncp.email");
            String country = configurationManager.getProperty("COUNTRY_PRINCIPAL_SUBDIVISION");
            String localip = configurationManager.getProperty(StandardProperties.SMP_SML_ADMIN_URL);//Source Gateway
            String remoteip = configurationManager.getProperty("SERVER_IP");//Target Gateway
            String smp = configurationManager.getProperty(StandardProperties.SMP_SML_SUPPORT);
            String smpemail = configurationManager.getProperty(StandardProperties.SMP_SML_SUPPORT_EMAIL);
            //ET_ObjectID --> Base64 of url
            String objectID = uri.toString(); //ParticipantObjectID
            byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());

            LOGGER.info("SMP Put request response code: '{}'", itemUpload.getStatusCode());
            if (itemUpload.getStatusCode() == 404 || itemUpload.getStatusCode() == 503 || itemUpload.getStatusCode() == 405) {
                String message = env.getProperty("error.server.failed"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                //Audit Error
                byte[] encodedObjectDetail = Base64.encodeBase64(response.getStatusLine().getReasonPhrase().getBytes());

                Audit.sendAuditPush(ncp, ncpemail, smp, smpemail, country, remoteip, localip,
                        new String(encodedObjectID), Integer.toString(response.getStatusLine().getStatusCode()), encodedObjectDetail);

                return "redirect:/smpeditor/uploadsmpfile";
            } else if (itemUpload.getStatusCode() == 401) {
                String message = env.getProperty("error.nouser"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                //Audit Error
                byte[] encodedObjectDetail = Base64.encodeBase64(response.getStatusLine().getReasonPhrase().getBytes());

                Audit.sendAuditPush(ncp, ncpemail, smp, smpemail, country, remoteip, localip,
                        new String(encodedObjectID), Integer.toString(response.getStatusLine().getStatusCode()), encodedObjectDetail);

                return "redirect:/smpeditor/uploadsmpfile";
            }

            if (!(itemUpload.getStatusCode() == 200 || itemUpload.getStatusCode() == 201)) {
                /* Get BusinessCode and ErrorDescription from response */

                //Save InputStream of response in ByteArrayOutputStream in order to read it more than once.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    org.apache.commons.io.IOUtils.copy(entity.getContent(), baos);
                } catch (IOException ex) {
                    LOGGER.error("IOException response: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (UnsupportedOperationException ex) {
                    LOGGER.error("UnsupportedOperationException response: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
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
                            itemUpload.setBusinessCode(businessCode);
                        }
                        if (nodes.item(j).getNodeName().equals("ErrorDescription")) {
                            String errorDescription = nodes.item(j).getTextContent();
                            itemUpload.setErrorDescription(errorDescription);
                        }
                    }
                } catch (ParserConfigurationException ex) {
                    LOGGER.error("ParserConfigurationException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (SAXException ex) {
                    LOGGER.error("SAXException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (IOException ex) {
                    LOGGER.error("IOException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
                }

                // Transform XML to String in order to send in Audit
                String errorResult = Audit.prepareEventLog(bytes);
                LOGGER.debug("Error Result: '{}", errorResult);
                //Audit error
                Audit.sendAuditPush(ncp, ncpemail, smp, smpemail, country, remoteip, localip, new String(encodedObjectID),
                        Integer.toString(response.getStatusLine().getStatusCode()), errorResult.getBytes());
            }

            if (itemUpload.getStatusCode() == 200 || itemUpload.getStatusCode() == 201) {
                //Audit Success
                Audit.sendAuditPush(ncp, ncpemail, smp, smpemail, country, remoteip, localip, new String(encodedObjectID),
                        null, null);
            }

            //GET
            Boolean success = true;
            String errorType = "";
            ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(partID, partScheme);
            DocumentIdentifier documentIdentifier = new DocumentIdentifier(docID, docScheme);

            LOGGER.info("Instantiating DynamicDiscovery: '{}', '{}', '{}', '{}'", partID, partScheme, docID, docScheme);
            DynamicDiscovery smpClient = DynamicDiscoveryClient.getInstance();

            if (smpClient == null) {
                throw new Exception("Cannot instantiate SMPClient!!!!");
            }
            URI smpURI = null;
            try {
                smpURI = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
            } catch (TechnicalException ex) {
                success = false;
                errorType = "TechnicalException";
                LOGGER.error("TechnicalException: '{}", SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            URI serviceGroup = smpClient.getService().getMetadataProvider().resolveDocumentIdentifiers(smpURI, participantIdentifier);
            URI serviceMetadataUri = smpClient.getService().getMetadataProvider().resolveServiceMetadata(smpURI, participantIdentifier, documentIdentifier);

            LOGGER.info("URI ServiceGroup: '{}'\nURI serviceMetadataUri: '{}'", serviceGroup.toASCIIString(), serviceMetadataUri.toASCIIString());

            itemUpload.setServiceGroupUrl(serviceGroup.toString());
            itemUpload.setSignedServiceMetadataUrl(serviceMetadataUri.toString());
            itemUpload.setId(i);
            allItems.add(i, itemUpload);
        }
        smpupload.setAllItems(allItems);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Model Attibute: '{}'", model.toString());
        }
        return "redirect:/smpeditor/uploadsmpinfo";

    }

    /**
     * Generate uploadInfo page
     *
     * @param smpupload
     * @param model
     * @return
     */
    @RequestMapping(value = "smpeditor/uploadsmpinfo", method = RequestMethod.GET)
    public String uploadInfo(@ModelAttribute("smpupload") SMPHttp smpupload, Model model) {
        LOGGER.debug("\n==== in uploadInfo ====");
        model.addAttribute("smpupload", smpupload);

        //  Builds html colors and alerts
        for (int i = 0; i < smpupload.getAllItems().size(); i++) {
            if (smpupload.getAllItems().get(i).getStatusCode() == 200) {
                String message = env.getProperty("http.updated");//messages.properties
                Alert status = new Alert(message, Alert.fontColor.green, "#dff0d8");
                smpupload.getAllItems().get(i).setStatus(status);

            } else if (smpupload.getAllItems().get(i).getStatusCode() == 201) {
                String message = env.getProperty("http.created");//messages.properties
                Alert status = new Alert(message, Alert.fontColor.green, "#dff0d8");
                smpupload.getAllItems().get(i).setStatus(status);

            } else if (smpupload.getAllItems().get(i).getStatusCode() == 400) {
                String messag = env.getProperty("http.failure");//messages.properties
                Alert status = new Alert(messag, Alert.fontColor.red, "#f2dede");
                smpupload.getAllItems().get(i).setStatus(status);

                if (StringUtils.equals(smpupload.getAllItems().get(i).getBusinessCode(), "XSD_INVALID")) {
                    String message = "400 (XSD_INVALID): " + env.getProperty("http.400.XSD_INVALID");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.danger);
                    smpupload.getAllItems().get(i).setAlert(alert);
                } else if (StringUtils.equals(smpupload.getAllItems().get(i).getBusinessCode(), "MISSING_FIELD")) {
                    String message = "400 (MISSING_FIELD): " + env.getProperty("http.400.MISSING_FIELD");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.danger);
                    smpupload.getAllItems().get(i).setAlert(alert);
                } else if (StringUtils.equals(smpupload.getAllItems().get(i).getBusinessCode(), "WRONG_FIELD")) {
                    String message = "400 (WRONG_FIELD): " + env.getProperty("http.400.WRONG_FIELD");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.danger);
                    smpupload.getAllItems().get(i).setAlert(alert);
                } else if (StringUtils.equals(smpupload.getAllItems().get(i).getBusinessCode(), "OUT_OF_RANGE")) {
                    String message = "400 (OUT_OF_RANGE): " + env.getProperty("http.400.OUT_OF_RANGE");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.danger);
                    smpupload.getAllItems().get(i).setAlert(alert);
                } else if (StringUtils.equals(smpupload.getAllItems().get(i).getBusinessCode(), "UNAUTHOR_FIELD")) {
                    String message = "400 (UNAUTHOR_FIELD): " + env.getProperty("http.400.UNAUTHOR_FIELD");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.danger);
                    smpupload.getAllItems().get(i).setAlert(alert);
                } else if (StringUtils.equals(smpupload.getAllItems().get(i).getBusinessCode(), "FORMAT_ERROR")) {
                    String message = "400 (FORMAT_ERROR): " + env.getProperty("http.400.FORMAT_ERROR");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.danger);
                    smpupload.getAllItems().get(i).setAlert(alert);
                } else if (StringUtils.equals(smpupload.getAllItems().get(i).getBusinessCode(), "OTHER_ERROR")) {
                    String message = "400 (OTHER_ERROR): " + env.getProperty("http.400.OTHER_ERROR");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.danger);
                    smpupload.getAllItems().get(i).setAlert(alert);
                }
            } else if (smpupload.getAllItems().get(i).getStatusCode() == 500) {
                String messag = env.getProperty("http.failure");//messages.properties
                Alert status = new Alert(messag, Alert.fontColor.red, "#f2dede");
                smpupload.getAllItems().get(i).setStatus(status);

                String message = "500: " + env.getProperty("http.500"); //messages.properties
                Alert alert = new Alert(message, Alert.alertType.danger);
                smpupload.getAllItems().get(i).setAlert(alert);
            }
        }

        model.addAttribute("items", smpupload.getAllItems());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MVC Model: '{}", model.toString());
        }
        return "smpeditor/uploadsmpinfo";
    }
}
