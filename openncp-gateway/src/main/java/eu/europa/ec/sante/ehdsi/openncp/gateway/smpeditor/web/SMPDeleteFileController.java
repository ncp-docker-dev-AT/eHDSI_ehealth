package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.web;

import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.DocumentIdentifier;
import eu.europa.ec.dynamicdiscovery.model.ParticipantIdentifier;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.StandardProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.Alert;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.SMPHttp;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.SMPHttp.ReferenceCollection;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.SMPType;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service.*;
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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * @author Ines Garganta
 */

@Controller
@SessionAttributes("smpdelete")
public class SMPDeleteFileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMPDeleteFileController.class);

    private ReadSMPProperties readProperties = new ReadSMPProperties();
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
    @RequestMapping(value = "/smpeditor/deletesmpfile", method = RequestMethod.GET)
    public String deleteFile(Model model) {
        LOGGER.debug("\n==== in deleteFile ====");
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
    @RequestMapping(value = "smpeditor/deletesmpfile", method = RequestMethod.POST)
    public String postDelete(@ModelAttribute("smpdelete") SMPHttp smpdelete, Model model, final RedirectAttributes redirectAttributes) {
        LOGGER.debug("\n==== in postDelete ====");
        model.addAttribute("smpdelete", smpdelete);

        String partID = "urn:ehealth:" + smpdelete.getCountry().name() + ":ncp-idp"; //SPECIFICATION
        String partScheme = env.getProperty("ParticipantIdentifier.Scheme");

        Boolean success = true;
        String errorType = "";
        ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(partID, partScheme);
        DynamicDiscovery smpClient = null;

//        ProxyCredentials proxyCredentials = null;
//        if (ProxyUtil.isProxyAnthenticationMandatory()) {
//            proxyCredentials = ProxyUtil.getProxyCredentials();
//        }
//        KeyStore truststore = null;
//        try {
//            truststore = loadTrustStore();
//        } catch (KeyStoreException e) {
//            LOGGER.error("KeyStoreException: '{}'", e.getMessage(), e);
//        } catch (IOException e) {
//            LOGGER.error("IOException: '{}'", e.getMessage(), e);
//        } catch (NoSuchAlgorithmException e) {
//            LOGGER.error("NoSuchAlgorithmException: '{}'", e.getMessage(), e);
//        } catch (CertificateException e) {
//            LOGGER.error("CertificateException: '{}'", e.getMessage(), e);
//        }

//        if (proxyCredentials != null) {
//            try {
//
//                smpClient = DynamicDiscoveryBuilder.newInstance()
//                        .locator(new DefaultBDXRLocator(ConfigurationManagerFactory.getConfigurationManager()
//                                .getProperty(StandardProperties.SMP_SML_DNS_DOMAIN), new DefaultDNSLookup()))
//                        .reader(new DefaultBDXRReader(new DefaultSignatureValidator(truststore)))
//                        .fetcher(new DefaultURLFetcher(new CustomProxy(proxyCredentials.getProxyHost(),
//                                Integer.parseInt(proxyCredentials.getProxyPort()), proxyCredentials.getProxyUser(),
//                                proxyCredentials.getProxyPassword())))
//                        .build();
//            } catch (ConnectionException ex) {
//                success = false;
//                errorType = "ConnectionException";
//                LOGGER.error("\n ConnectionException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
//            } catch (TechnicalException e) {
//                LOGGER.error("TechnicalException: '{}'" + e.getMessage(), e);
//            }
//        } else {
//            try {
//                smpClient = DynamicDiscoveryBuilder.newInstance()
//                        .locator(new DefaultBDXRLocator(ConfigurationManagerFactory.getConfigurationManager()
//                                .getProperty(StandardProperties.SMP_SML_DNS_DOMAIN), new DefaultDNSLookup()))
//                        .reader(new DefaultBDXRReader(new DefaultSignatureValidator(truststore)))
//                        .build();
//            } catch (TechnicalException e) {
//                LOGGER.error("Technical Exception: '{}'", e.getMessage(), e);
//            }
//        }
        try {
            smpClient = DynamicDiscoveryClient.getInstance();
        } catch (ConnectionException ex) {
            success = false;
            errorType = "ConnectionException";
            LOGGER.error("\n ConnectionException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (TechnicalException | CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            LOGGER.error("Technical Exception: '{}'", e.getMessage(), e);
        }

        List<DocumentIdentifier> documentIdentifiers = new ArrayList<>();
        try {
            //documentIdentifiers = smpClient.getDocumentIdentifiers(participantIdentifier);
            documentIdentifiers = smpClient.getService().getServiceGroup(participantIdentifier).getDocumentIdentifiers();
        } catch (TechnicalException ex) {
            success = false;
            errorType = "TechnicalException";
            LOGGER.error("TechnicalException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        URI serviceGroup = null;
        List<ReferenceCollection> referenceCollection = new ArrayList<>();
        for (int i = 0; i < documentIdentifiers.size(); i++) {
            String smptype = "Unknown type";
            String documentID = "";
            HashMap<String, String> propertiesMap = readProperties.readPropertiesFile();
            Set set2 = propertiesMap.entrySet();
            for (Object aSet2 : set2) {
                Map.Entry mentry2 = (Map.Entry) aSet2;
                if (StringUtils.equalsIgnoreCase(documentIdentifiers.get(i).getIdentifier(), mentry2.getKey().toString())) {
                    //if (documentIdentifiers.get(i).getIdentifier().equals(mentry2.getKey().toString())) {
                    //urn:ehealth:patientidentificationandauthentication::xcpd::crossgatewaypatientdiscovery##iti-55
                    String[] docs = mentry2.getValue().toString().split("\\.");
                    documentID = docs[0];
                    break;
                }
            }
            String smpType = documentID;
            LOGGER.debug("\n******** DOC ID - '{}'", documentIdentifiers.get(i).getIdentifier());
            LOGGER.debug("\n******** SMP Type - '{}'", smpType);

            for (SMPType smptype1 : SMPType.getALL()) {
                if (smptype1.name().equals(smpType)) {
                    smptype = smptype1.getDescription();
                    break;
                }
            }
            URI smpURI = null;
            try {
                smpURI = smpClient.getService().getMetadataLocator().lookup(participantIdentifier);
            } catch (TechnicalException ex) {
                success = false;
                errorType = "TechnicalException";
                LOGGER.error("\n TechnicalException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            }
            smpdelete.setSmpURI(smpURI.toString());
            URI uri = smpClient.getService().getMetadataProvider().resolveServiceMetadata(smpURI, participantIdentifier, documentIdentifiers.get(i));
            ReferenceCollection reference = new ReferenceCollection(uri.toString(), smptype, i);
            referenceCollection.add(reference);

            serviceGroup = smpClient.getService().getMetadataProvider().resolveDocumentIdentifiers(smpURI, participantIdentifier);
        }
        smpdelete.setReferenceCollection(referenceCollection);

        //Audit
        ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();
        String ncp = configurationManager.getProperty(StandardProperties.NCP_COUNTRY);
        String ncpemail = configurationManager.getProperty(StandardProperties.NCP_EMAIL);
        String country = configurationManager.getProperty(StandardProperties.NCP_COUNTRY_PRINCIPAL_SUBDIVISION);
        String localip = smpdelete.getSmpURI();
        String remoteip = configurationManager.getProperty(StandardProperties.NCP_SERVER);
        String smp = configurationManager.getProperty(StandardProperties.SMP_SML_SUPPORT);
        String smpemail = configurationManager.getProperty(StandardProperties.SMP_SML_SUPPORT_EMAIL);
        //ET_ObjectID --> Base64 of url
        String objectID = serviceGroup.toString();
        byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());

        if (success) {
            //Audit Success
            Audit.sendAuditQuery(smp, smpemail, ncp, ncpemail, country, localip, remoteip,
                    new String(encodedObjectID), null, null);
        } else {
            //Audit Error
            Audit.sendAuditQuery(smp, smpemail, ncp, ncpemail, country, localip, remoteip,
                    new String(encodedObjectID), "500", errorType.getBytes());//TODO
        }

        if (referenceCollection.isEmpty()) {
            String message = env.getProperty("error.nodoc"); //messages.properties
            Alert alert = new Alert(message, Alert.alertType.warning);
            smpdelete.setAlert(alert);
            redirectAttributes.addFlashAttribute("alert", alert);
            return "redirect:/smpeditor/deletesmpinfo";
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("\n********* MODEL - '{}'", model.toString());
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
    @RequestMapping(value = "smpeditor/deletesmpinfo", method = RequestMethod.GET)
    public String deleteInfo(@ModelAttribute("smpdelete") SMPHttp smpdelete, Model model, final RedirectAttributes redirectAttributes) {
        LOGGER.debug("\n==== in deleteInfo ====");
   
    /* Builds html colors and alerts */
        if (smpdelete.getStatusCode() == 400) {
            String messag = env.getProperty("http.failure");//messages.properties
            Alert status = new Alert(messag, Alert.fontColor.red, "#f2dede");
            smpdelete.setStatus(status);
            if (smpdelete.getBusinessCode().equals("OTHER_ERROR")) {
                String message = "400 (OTHER_ERROR): " + env.getProperty("http.400.OTHER_ERROR");//messages.properties
                Alert alert = new Alert(message, Alert.alertType.danger);
                smpdelete.setAlert(alert);
            }

        } else if (smpdelete.getStatusCode() == 404) {
            String messag = env.getProperty("http.failure");//messages.properties
            Alert status = new Alert(messag, Alert.fontColor.red, "#f2dede");
            smpdelete.setStatus(status);

            String message = "404: " + env.getProperty("http.404"); //messages.properties
            Alert alert = new Alert(message, Alert.alertType.danger);
            smpdelete.setAlert(alert);

        } else if (smpdelete.getStatusCode() == 500) {
            String messag = env.getProperty("http.failure");//messages.properties
            Alert status = new Alert(messag, Alert.fontColor.red, "#f2dede");
            smpdelete.setStatus(status);

            String message = "500: " + env.getProperty("http.500"); //messages.properties
            Alert alert = new Alert(message, Alert.alertType.danger);
            smpdelete.setAlert(alert);
        }


        model.addAttribute("smpdelete", smpdelete);
        model.addAttribute("referenceCollection", smpdelete.getReferenceCollection());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("\n********* MODEL - '{}'", model.toString());
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
    @RequestMapping(value = "smpeditor/deletesmpinfo", method = RequestMethod.POST)
    public String deletePost(@ModelAttribute("smpdelete") SMPHttp smpdelete, Model model, final RedirectAttributes redirectAttributes) {
        LOGGER.debug("\n==== in deletePost ====");
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

    /*
      Iterate through all references selected to delete
    */
        for (int i = 0; i < referencesSelected.size(); i++) {
            SMPHttp itemDelete = new SMPHttp();

            LOGGER.debug("\n ************** referencesSelected.get(i) - " + referencesSelected.get(i));
            String[] refs = referencesSelected.get(i).split("&&");

            LOGGER.debug("\n ************** SMPTYPEEEE - '{}'", refs[1]);
            LOGGER.debug("\n ************** reference - '{}'", refs[0]);
            itemDelete.setSmptype(refs[1]);

            String reference = refs[0];
            itemDelete.setReference(reference);
            if (reference.startsWith("http://") || reference.startsWith("https://")) {
                reference = reference.substring(smpdelete.getSmpURI().length());
            }

            try {
                reference = java.net.URLDecoder.decode(reference, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                LOGGER.error("\n UnsupportedEncodingException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            }
            LOGGER.debug("\n ************** referencesSelected - {}", reference);

            URI uri = null;
            try {
                uri = new URIBuilder()
                        .setScheme("https")
                        .setHost(urlServer)
                        .setPath(reference)
                        .build();
            } catch (URISyntaxException e) {
                LOGGER.error("URISyntaxException", e);
            }

            LOGGER.debug("\n ************** URI - {}", uri);

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
                LOGGER.error("\n NoSuchAlgorithmException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (KeyStoreException ex) {
                LOGGER.error("\n KeyStoreException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (CertificateException ex) {
                LOGGER.error("\n CertificateException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (IOException ex) {
                LOGGER.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (KeyManagementException ex) {
                LOGGER.error("\n KeyManagementException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (UnrecoverableKeyException ex) {
                LOGGER.error("\n UnrecoverableKeyException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            //DELETE
            HttpDelete httpdelete = new HttpDelete(uri);

            CloseableHttpResponse response;
            try {
                response = DynamicDiscoveryService.buildHttpClient(sslcontext).execute(httpdelete);
            } catch (IOException ex) {
                LOGGER.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                String message = env.getProperty("error.server.failed"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                return "redirect:/smpeditor/deletesmpfile";
            }

            /*Get response*/
            itemDelete.setStatusCode(response.getStatusLine().getStatusCode());
            org.apache.http.HttpEntity entity = response.getEntity();

            LOGGER.debug("\n ************ RESPONSE DELETE - {}", response.getStatusLine().getStatusCode());
            LOGGER.debug("\n ************ RESPONSE REASON - {}", response.getStatusLine().getReasonPhrase());

            //Audit vars
            String ncp = configurationManager.getProperty("ncp.country");
            String ncpemail = configurationManager.getProperty("ncp.email");
            String country = configurationManager.getProperty("COUNTRY_PRINCIPAL_SUBDIVISION");
            String remoteip = configurationManager.getProperty("SERVER_IP");//Target Gateway
            String localip = configurationManager.getProperty(StandardProperties.SMP_SML_ADMIN_URL);
            String smp = configurationManager.getProperty(StandardProperties.SMP_SML_SUPPORT);
            String smpemail = configurationManager.getProperty(StandardProperties.SMP_SML_SUPPORT_EMAIL);
            //ET_ObjectID --> Base64 of url
            String objectID = uri.toString();
            byte[] encodedObjectID = Base64.encodeBase64(objectID.getBytes());

            if (itemDelete.getStatusCode() == 503 || itemDelete.getStatusCode() == 405) {
                String message = env.getProperty("error.server.failed"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                //Audit error
                byte[] encodedObjectDetail = Base64.encodeBase64(response.getStatusLine().getReasonPhrase().getBytes());
                //Audit.sendAuditPush(smp, smpemail, ncp, ncpemail, country, localip, remoteip,
                Audit.sendAuditPush(ncp, ncpemail, smp, smpemail, country, remoteip, localip,
                        new String(encodedObjectID), Integer.toString(response.getStatusLine().getStatusCode()), encodedObjectDetail);

                return "redirect:/smpeditor/deletesmpfile";
            } else if (itemDelete.getStatusCode() == 401) {
                String message = env.getProperty("error.nouser"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                //Audit error
                byte[] encodedObjectDetail = Base64.encodeBase64(response.getStatusLine().getReasonPhrase().getBytes());
                Audit.sendAuditPush(ncp, ncpemail, smp, smpemail, country, remoteip, localip,
                        //Audit.sendAuditPush(smp, smpemail, ncp, ncpemail, country, localip, remoteip,
                        new String(encodedObjectID), Integer.toString(response.getStatusLine().getStatusCode()), encodedObjectDetail);

                return "redirect:/smpeditor/deletesmpfile";
            }

            if (!(itemDelete.getStatusCode() == 200 || itemDelete.getStatusCode() == 201)) {
            /* Get BusinessCode and ErrorDescription from response */

                //Save InputStream of response in ByteArrayOutputStream in order to read it more than once.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    org.apache.commons.io.IOUtils.copy(entity.getContent(), baos);
                } catch (IOException ex) {
                    LOGGER.error("\n IOException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (UnsupportedOperationException ex) {
                    LOGGER.error("\n UnsupportedOperationException response - " + SimpleErrorHandler.printExceptionStackTrace(ex));
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
                    LOGGER.error("\n ParserConfigurationException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (SAXException ex) {
                    LOGGER.error("\n SAXException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (IOException ex) {
                    LOGGER.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                }
        
                /*transform xml to string in order to send in Audit*/
                String errorResult = Audit.prepareEventLog(bytes);
                LOGGER.debug("\n ***************** ERROR RESULT - '{}'", errorResult);
                //Audit error
                //Audit.sendAuditPush(smp, smpemail, ncp, ncpemail, country, localip, remoteip,
                Audit.sendAuditPush(ncp, ncpemail, smp, smpemail, country, remoteip, localip,
                        new String(encodedObjectID), Integer.toString(response.getStatusLine().getStatusCode()), errorResult.getBytes());
            }

            if (itemDelete.getStatusCode() == 200 || itemDelete.getStatusCode() == 201) {
                //Audit Success
                Audit.sendAuditPush(ncp, ncpemail, smp, smpemail, country, remoteip, localip,
                        //Audit.sendAuditPush(smp, smpemail, ncp, ncpemail, country, localip, remoteip,
                        new String(encodedObjectID), null, null);
            }
            itemDelete.setId(i);
            allItems.add(i, itemDelete);
        }
        smpdelete.setAllItems(allItems);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("\n********* MODEL - " + model.toString());
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
    @RequestMapping(value = "smpeditor/deletesmpresult", method = RequestMethod.GET)
    public String postInfo(@ModelAttribute("smpdelete") SMPHttp smpdelete, Model model) {
        LOGGER.debug("\n==== in deletesmpresult ====");
    
        /* Builds html colors and alerts */
        for (int i = 0; i < smpdelete.getAllItems().size(); i++) {
            if (smpdelete.getAllItems().get(i).getStatusCode() == 200) {
                String message = env.getProperty("http.deleted");//messages.properties
                Alert status = new Alert(message, Alert.fontColor.green, "#dff0d8");
                smpdelete.getAllItems().get(i).setStatus(status);

            } else if (smpdelete.getAllItems().get(i).getStatusCode() == 400) {
                String messag = env.getProperty("http.failure");//messages.properties
                Alert status = new Alert(messag, Alert.fontColor.red, "#f2dede");
                smpdelete.getAllItems().get(i).setStatus(status);
                if ("OTHER_ERROR".equals(smpdelete.getAllItems().get(i).getBusinessCode())) {
                    String message = "400 (OTHER_ERROR): " + env.getProperty("http.400.OTHER_ERROR");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.danger);
                    smpdelete.getAllItems().get(i).setAlert(alert);
                }

            } else if (smpdelete.getAllItems().get(i).getStatusCode() == 404) {
                String messag = env.getProperty("http.failure");//messages.properties
                Alert status = new Alert(messag, Alert.fontColor.red, "#f2dede");
                smpdelete.getAllItems().get(i).setStatus(status);

                String message = "404: " + env.getProperty("http.404"); //messages.properties
                Alert alert = new Alert(message, Alert.alertType.danger);
                smpdelete.getAllItems().get(i).setAlert(alert);

            } else if (smpdelete.getAllItems().get(i).getStatusCode() == 500) {
                String messag = env.getProperty("http.failure");//messages.properties
                Alert status = new Alert(messag, Alert.fontColor.red, "#f2dede");
                smpdelete.getAllItems().get(i).setStatus(status);

                String message = "500: " + env.getProperty("http.500"); //messages.properties
                Alert alert = new Alert(message, Alert.alertType.danger);
                smpdelete.getAllItems().get(i).setAlert(alert);
            }
        }
        model.addAttribute("smpdelete", smpdelete);
        model.addAttribute("items", smpdelete.getAllItems());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("\n********* MODEL - {}", model.toString());
        }
        return "smpeditor/deletesmpresult";
    }
}
