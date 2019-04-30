package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.web;

import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.DynamicDiscoveryService;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.Alert;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.SMPHttp;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.SMPHttp.ReferenceCollection;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.SMPType;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.AuditManager;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service.DynamicDiscoveryClient;
import eu.europa.ec.sante.ehdsi.openncp.gateway.cfg.ReadSMPProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service.SimpleErrorHandler;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ines Garganta
 */

@Controller
@SessionAttributes("smpdelete")
public class SMPDeleteFileController {

    private static final String ALERT_BACKGROUND_COLOR = "#f2dede";
    private final Logger logger = LoggerFactory.getLogger(SMPDeleteFileController.class);
    private ReadSMPProperties readProperties;
    private Environment env;

    @Autowired
    public SMPDeleteFileController(ReadSMPProperties readProperties, Environment env) {
        this.readProperties = readProperties;
        this.env = env;
    }

    /**
     * Generate UploadFile page
     *
     * @param model
     * @return
     */
    @GetMapping(value = "/smpeditor/deletesmpfile")
    public String deleteFile(Model model) {
        logger.debug("\n==== in deleteFile ====");
        model.addAttribute("smpdelete", new SMPHttp());

        return "smpeditor/deletesmpfile";
    }

    /**
     * DELETE files from server
     *
     * @param smpdelete
     * @param model
     * @param redirectAttributes
     * @return
     */
    @PostMapping(value = "smpeditor/deletesmpfile")
    public String postDelete(@ModelAttribute("smpdelete") SMPHttp smpdelete, Model model, final RedirectAttributes redirectAttributes) {

        logger.debug("Processing SMP delete action...");
        model.addAttribute("smpdelete", smpdelete);

        String partID = "urn:ehealth:" + smpdelete.getCountry().name() + ":ncp-idp";
        String partScheme = env.getProperty("ParticipantIdentifier.Scheme");

        boolean success = true;
        String errorType = "";
        ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(partID, partScheme);
        DynamicDiscovery smpClient = null;

        try {
            smpClient = DynamicDiscoveryClient.getInstance();
        } catch (ConnectionException ex) {
            success = false;
            errorType = "ConnectionException";
            logger.error("\n ConnectionException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (TechnicalException | CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            logger.error("Technical Exception: '{}'", e.getMessage(), e);
        }

        List<DocumentIdentifier> documentIdentifiers = new ArrayList<>();
        try {
            documentIdentifiers = smpClient.getService().getServiceGroup(participantIdentifier).getDocumentIdentifiers();
        } catch (TechnicalException ex) {
            success = false;
            errorType = "TechnicalException";
            logger.error("TechnicalException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        URI serviceGroup = null;
        List<ReferenceCollection> referenceCollection = new ArrayList<>();
        for (int i = 0; i < documentIdentifiers.size(); i++) {
            String smptype = "Unknown type";
            String documentID = "";
            Map<String, String> propertiesMap = readProperties.readPropertiesFile();
            Set set2 = propertiesMap.entrySet();
            for (Object aSet2 : set2) {
                Map.Entry mentry2 = (Map.Entry) aSet2;
                if (StringUtils.equalsIgnoreCase(documentIdentifiers.get(i).getIdentifier(), mentry2.getKey().toString())) {
                    String[] docs = mentry2.getValue().toString().split("\\.");
                    documentID = docs[0];
                    break;
                }
            }
            String smpType = documentID;
            logger.debug("\n******** DOC ID - '{}'", documentIdentifiers.get(i).getIdentifier());
            logger.debug("\n******** SMP Type - '{}'", smpType);

            for (SMPType smptype1 : SMPType.getALL()) {
                if (smptype1.name().equals(smpType)) {
                    smptype = smptype1.getDescription();
                    break;
                }
            }
            URI smpURI = null;
            try {
                smpURI = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
                smpdelete.setSmpURI(smpURI.toString());
            } catch (TechnicalException ex) {
                success = false;
                errorType = "TechnicalException";
                logger.error("\n TechnicalException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            }
            URI uri = smpClient.getService().getMetadataProvider().resolveServiceMetadata(smpURI, participantIdentifier, documentIdentifiers.get(i));
            ReferenceCollection reference = new ReferenceCollection(uri.toString(), smptype, i);
            referenceCollection.add(reference);

            serviceGroup = smpClient.getService().getMetadataProvider().resolveDocumentIdentifiers(smpURI, participantIdentifier);
        }
        smpdelete.setReferenceCollection(referenceCollection);

        //Audit
        String objectID = serviceGroup.toString();
        byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());
        logger.info("DNS: '{}'", smpdelete.getSmpURI());
        if (success) {
            //Audit Success
            AuditManager.handleDynamicDiscoveryQuery(smpdelete.getSmpURI(), new String(encodedObjectID), null, null);

        } else {
            //Audit Error
            AuditManager.handleDynamicDiscoveryQuery(smpdelete.getSmpURI(), new String(encodedObjectID), "500", errorType.getBytes());
        }

        if (referenceCollection.isEmpty()) {
            //  messages.properties
            String message = env.getProperty("error.nodoc");
            Alert alert = new Alert(message, Alert.alertType.warning);
            smpdelete.setAlert(alert);
            redirectAttributes.addFlashAttribute("alert", alert);
            return "redirect:/smpeditor/deletesmpinfo";
        }
        if (logger.isDebugEnabled()) {
            logger.debug("\n********* MODEL - '{}'", model.toString());
        }
        return "redirect:/smpeditor/deletesmpinfo";
    }

    /**
     * Generate deleteInfo page
     *
     * @param smpdelete
     * @param model
     * @param redirectAttributes
     * @return
     */
    @GetMapping(value = "smpeditor/deletesmpinfo")
    public String deleteInfo(@ModelAttribute("smpdelete") SMPHttp smpdelete, Model model, final RedirectAttributes redirectAttributes) {
        logger.debug("\n==== in deleteInfo ====");

        /* Builds html colors and alerts */
        if (smpdelete.getStatusCode() == 400) {
            //  messages.properties
            String messag = env.getProperty("http.failure");
            Alert status = new Alert(messag, Alert.fontColor.red, ALERT_BACKGROUND_COLOR);
            smpdelete.setStatus(status);
            if (smpdelete.getBusinessCode().equals("OTHER_ERROR")) {
                String message = "400 (OTHER_ERROR): " + env.getProperty("http.400.OTHER_ERROR");//messages.properties
                Alert alert = new Alert(message, Alert.alertType.danger);
                smpdelete.setAlert(alert);
            }

        } else if (smpdelete.getStatusCode() == 404) {
            String messag = env.getProperty("http.failure");//messages.properties
            Alert status = new Alert(messag, Alert.fontColor.red, ALERT_BACKGROUND_COLOR);
            smpdelete.setStatus(status);

            String message = "404: " + env.getProperty("http.404"); //messages.properties
            Alert alert = new Alert(message, Alert.alertType.danger);
            smpdelete.setAlert(alert);

        } else if (smpdelete.getStatusCode() == 500) {
            String messag = env.getProperty("http.failure");//messages.properties
            Alert status = new Alert(messag, Alert.fontColor.red, ALERT_BACKGROUND_COLOR);
            smpdelete.setStatus(status);

            String message = "500: " + env.getProperty("http.500"); //messages.properties
            Alert alert = new Alert(message, Alert.alertType.danger);
            smpdelete.setAlert(alert);
        }

        model.addAttribute("smpdelete", smpdelete);
        model.addAttribute("referenceCollection", smpdelete.getReferenceCollection());

        if (logger.isDebugEnabled()) {
            logger.debug("\n********* MODEL - '{}'", model.toString());
        }
        return "smpeditor/deletesmpinfo";
    }

    /**
     * deleteInfo post
     *
     * @param smpdelete
     * @param model
     * @param redirectAttributes
     * @return
     */
    @PostMapping(value = "smpeditor/deletesmpinfo")
    public String deletePost(@ModelAttribute("smpdelete") SMPHttp smpdelete, Model model, final RedirectAttributes redirectAttributes) {

        logger.debug("\n==== in deletePost ====");
        model.addAttribute("smpdelete", smpdelete);

        ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();

        String urlServer = configurationManager.getProperty(StandardProperties.SMP_SML_ADMIN_URL);
        if (urlServer.endsWith("/")) {
            urlServer = urlServer.substring(0, urlServer.length() - 1);
        }
        /*Removes https:// from entered by the user so it won't repeat in uri set scheme*/
        if (urlServer.startsWith("https")) {
            urlServer = urlServer.substring(8);
        }

        List<String> referencesSelected = smpdelete.getReferenceSelected();

        List<SMPHttp> allItems = new ArrayList<>();

        //  Iterate through all references selected to delete
        for (int i = 0; i < referencesSelected.size(); i++) {
            SMPHttp itemDelete = new SMPHttp();

            logger.debug("\n ************** referencesSelected.get(i) - " + referencesSelected.get(i));
            String[] refs = referencesSelected.get(i).split("&&");

            logger.debug("\n ************** SMPTYPEEEE - '{}'", refs[1]);
            logger.debug("\n ************** reference - '{}'", refs[0]);
            itemDelete.setSmptype(refs[1]);

            String reference = refs[0];
            itemDelete.setReference(reference);
            if (reference.startsWith("http://") || reference.startsWith("https://")) {
                reference = reference.substring(smpdelete.getSmpURI().length());
            }

            try {
                reference = java.net.URLDecoder.decode(reference, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                logger.error("\n UnsupportedEncodingException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            }
            logger.debug("\n ************** referencesSelected - {}", reference);

            URI uri = null;
            try {
                uri = new URIBuilder().setScheme("https").setHost(urlServer).setPath(reference).build();
            } catch (URISyntaxException e) {
                logger.error("URISyntaxException", e);
            }

            logger.debug("\n ************** URI - {}", uri);

            PrivateKeyStrategy privatek = (map, socket) -> configurationManager.getProperty(StandardProperties.SMP_SML_CLIENT_KEY_ALIAS);

            // Trust own CA and all self-signed certs
            SSLContext sslcontext = null;
            try {
                sslcontext = SSLContexts.custom()
                        .loadKeyMaterial(new File(configurationManager.getProperty("SC_KEYSTORE_PATH")),
                                configurationManager.getProperty("SC_KEYSTORE_PASSWORD").toCharArray(),
                                configurationManager.getProperty("SC_SMP_CLIENT_PRIVATEKEY_PASSWORD").toCharArray(), //must be the same as SC_KEYSTORE_PASSWORD
                                privatek)
                        .loadTrustMaterial(new File(configurationManager.getProperty(StandardProperties.NCP_TRUSTSTORE)),
                                configurationManager.getProperty(StandardProperties.NCP_TRUSTSTORE_PASSWORD).toCharArray(),
                                new TrustSelfSignedStrategy())
                        .build();
            } catch (NoSuchAlgorithmException ex) {
                logger.error("\n NoSuchAlgorithmException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (KeyStoreException ex) {
                logger.error("\n KeyStoreException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (CertificateException ex) {
                logger.error("\n CertificateException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (IOException ex) {
                logger.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (KeyManagementException ex) {
                logger.error("\n KeyManagementException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (UnrecoverableKeyException ex) {
                logger.error("\n UnrecoverableKeyException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            //DELETE
            HttpDelete httpdelete = new HttpDelete(uri);

            CloseableHttpResponse response;
            try {
                response = DynamicDiscoveryService.buildHttpClient(sslcontext).execute(httpdelete);
            } catch (IOException ex) {
                logger.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                String message = env.getProperty("error.server.failed"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                return "redirect:/smpeditor/deletesmpfile";
            }

            /*Get response*/
            itemDelete.setStatusCode(response.getStatusLine().getStatusCode());
            org.apache.http.HttpEntity entity = response.getEntity();

            logger.debug("\n ************ RESPONSE DELETE - {}", response.getStatusLine().getStatusCode());
            logger.debug("\n ************ RESPONSE REASON - {}", response.getStatusLine().getReasonPhrase());

            //Audit vars
            String remoteIp = ConfigurationManagerFactory.getConfigurationManager().getProperty("SMP_ADMIN_URL");
            //ET_ObjectID --> Base64 of url
            String objectID = uri.toString();
            byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());

            if (itemDelete.getStatusCode() == 503 || itemDelete.getStatusCode() == 405) {
                String message = env.getProperty("error.server.failed"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                //Audit error
                byte[] encodedObjectDetail = Base64.encodeBase64(response.getStatusLine().getReasonPhrase().getBytes());
                AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                        Integer.toString(response.getStatusLine().getStatusCode()), encodedObjectDetail);
                return "redirect:/smpeditor/deletesmpfile";
            } else if (itemDelete.getStatusCode() == 401) {
                String message = env.getProperty("error.nouser"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                //Audit error
                byte[] encodedObjectDetail = Base64.encodeBase64(response.getStatusLine().getReasonPhrase().getBytes());
                AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                        Integer.toString(response.getStatusLine().getStatusCode()), encodedObjectDetail);
                return "redirect:/smpeditor/deletesmpfile";
            }

            if (!(itemDelete.getStatusCode() == 200 || itemDelete.getStatusCode() == 201)) {
                /* Get BusinessCode and ErrorDescription from response */

                //Save InputStream of response in ByteArrayOutputStream in order to read it more than once.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    org.apache.commons.io.IOUtils.copy(entity.getContent(), baos);
                } catch (IOException ex) {
                    logger.error("\n IOException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (UnsupportedOperationException ex) {
                    logger.error("\n UnsupportedOperationException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
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
                            itemDelete.setBusinessCode(businessCode);
                        }
                        if (nodes.item(j).getNodeName().equals("ErrorDescription")) {
                            String errorDescription = nodes.item(j).getTextContent();
                            itemDelete.setErrorDescription(errorDescription);
                        }
                    }
                } catch (ParserConfigurationException ex) {
                    logger.error("\n ParserConfigurationException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (SAXException ex) {
                    logger.error("\n SAXException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (IOException ex) {
                    logger.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                }

                /*transform xml to string in order to send in Audit*/
                String errorResult = AuditManager.prepareEventLog(bytes);
                logger.debug("\n ***************** ERROR RESULT - '{}'", errorResult);
                //Audit error
                AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                        Integer.toString(response.getStatusLine().getStatusCode()), errorResult.getBytes());
            }

            if (itemDelete.getStatusCode() == 200 || itemDelete.getStatusCode() == 201) {
                //Audit Success
                AuditManager.handleDynamicDiscoveryPush(remoteIp, new String(encodedObjectID),
                        null, null);
            }
            itemDelete.setId(i);
            allItems.add(i, itemDelete);
        }
        smpdelete.setAllItems(allItems);

        if (logger.isDebugEnabled()) {
            logger.debug("Web Model:\n'{}'", model.toString());
        }
        return "redirect:/smpeditor/deletesmpresult";
    }

    /**
     * Generate deleteInfo page
     *
     * @param smpdelete
     * @param model
     * @return
     */
    @GetMapping(value = "smpeditor/deletesmpresult")
    public String postInfo(@ModelAttribute("smpdelete") SMPHttp smpdelete, Model model) {
        logger.debug("\n==== in deletesmpresult ====");

        /* Builds html colors and alerts */
        for (int i = 0; i < smpdelete.getAllItems().size(); i++) {
            if (smpdelete.getAllItems().get(i).getStatusCode() == 200) {
                String message = env.getProperty("http.deleted");//messages.properties
                Alert status = new Alert(message, Alert.fontColor.green, "#dff0d8");
                smpdelete.getAllItems().get(i).setStatus(status);

            } else if (smpdelete.getAllItems().get(i).getStatusCode() == 400) {
                String messag = env.getProperty("http.failure");//messages.properties
                Alert status = new Alert(messag, Alert.fontColor.red, ALERT_BACKGROUND_COLOR);
                smpdelete.getAllItems().get(i).setStatus(status);
                if ("OTHER_ERROR".equals(smpdelete.getAllItems().get(i).getBusinessCode())) {
                    String message = "400 (OTHER_ERROR): " + env.getProperty("http.400.OTHER_ERROR");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.danger);
                    smpdelete.getAllItems().get(i).setAlert(alert);
                }

            } else if (smpdelete.getAllItems().get(i).getStatusCode() == 404) {
                String messag = env.getProperty("http.failure");//messages.properties
                Alert status = new Alert(messag, Alert.fontColor.red, ALERT_BACKGROUND_COLOR);
                smpdelete.getAllItems().get(i).setStatus(status);

                String message = "404: " + env.getProperty("http.404"); //messages.properties
                Alert alert = new Alert(message, Alert.alertType.danger);
                smpdelete.getAllItems().get(i).setAlert(alert);

            } else if (smpdelete.getAllItems().get(i).getStatusCode() == 500) {
                String messag = env.getProperty("http.failure");//messages.properties
                Alert status = new Alert(messag, Alert.fontColor.red, ALERT_BACKGROUND_COLOR);
                smpdelete.getAllItems().get(i).setStatus(status);

                String message = "500: " + env.getProperty("http.500"); //messages.properties
                Alert alert = new Alert(message, Alert.alertType.danger);
                smpdelete.getAllItems().get(i).setAlert(alert);
            }
        }
        model.addAttribute("smpdelete", smpdelete);
        model.addAttribute("items", smpdelete.getAllItems());

        if (logger.isDebugEnabled()) {
            logger.debug("Model: {}", model.toString());
        }
        return "smpeditor/deletesmpresult";
    }
}
