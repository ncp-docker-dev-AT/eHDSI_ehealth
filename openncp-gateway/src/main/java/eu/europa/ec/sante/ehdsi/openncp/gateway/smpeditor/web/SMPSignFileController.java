package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.web;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.Constants;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.*;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.exception.GenericException;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service.*;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.eu.EndpointType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.eu.RedirectType;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.eu.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Inês Garganta
 */
@Controller
@SessionAttributes("smpfilesign")
public class SMPSignFileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMPSignFileController.class);

    private SMPConverter smpconverter;

    private Environment env;

    private ReadSMPProperties readProperties;

    private String type;

    @Autowired
    public SMPSignFileController(SMPConverter smpconverter, Environment env, ReadSMPProperties readProperties) {
        this.smpconverter = smpconverter;
        this.env = env;
        this.readProperties = readProperties;
    }

    /**
     * Generate SignFile page
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/smpeditor/signsmpfile", method = RequestMethod.GET)
    public String signFile(Model model) {
        LOGGER.debug("\n==== in signFile ====");
        model.addAttribute("smpfilesign", new SMPFileOps());

        return "smpeditor/signsmpfile";
    }

    /**
     * Generate signsmpfile page for generated files
     *
     * @param model
     * @param smpfile
     * @return
     */
    @RequestMapping(value = "/smpeditor/signsmpfile/generated", method = RequestMethod.GET)
    public String signCreatedFile(Model model, @ModelAttribute("smpfile") SMPFile smpfile) {

        LOGGER.debug("\n==== in signCreatedFile ====");
        SMPFileOps smpfilesign = new SMPFileOps();

        if (smpfile.getGeneratedFile() == null) {
            throw new GenericException("Not Found", "The requested file does not exists");
        }

        File file = new File(smpfile.getGeneratedFile().getPath());
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            LOGGER.error("\n FileNotFoundException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }
        MultipartFile fileSign = null;
        try {
            fileSign = new MockMultipartFile("fileSign", file.getName(), "text/xml", input);
            List<MultipartFile> files = new ArrayList<>();
            files.add(0, fileSign);

            smpfilesign.setSignFiles(files);
            smpfilesign.setSignFileName(fileSign.getOriginalFilename());
        } catch (IOException ex) {
            LOGGER.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        model.addAttribute("hasfile", true);
        model.addAttribute("smpfilesign", smpfilesign);

        return "smpeditor/signsmpfile";
    }

    /**
     * Generate signsmpfile page for updated files
     *
     * @param model
     * @param smpfileupdate
     * @return
     */
    @RequestMapping(value = "/smpeditor/signsmpfile/updated", method = RequestMethod.GET)
    public String signUpdatedFile(Model model, @ModelAttribute("smpfileupdate") SMPFileOps smpfileupdate) {

        LOGGER.debug("\n==== in signCreatedFile ====");
        SMPFileOps smpfilesign = new SMPFileOps();

        if (smpfileupdate.getGeneratedFile() == null) {
            throw new GenericException("Not Found", "The requested file does not exists");
        }

        File file = new File(smpfileupdate.getGeneratedFile().getPath());

        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            LOGGER.error("\nFileNotFoundException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }
        MultipartFile fileSign;
        try {
            fileSign = new MockMultipartFile("fileSign", file.getName(), "text/xml", input);
            List<MultipartFile> files = new ArrayList<>();
            files.add(0, fileSign);

            smpfilesign.setSignFiles(files);
            smpfilesign.setSignFileName(fileSign.getOriginalFilename());
        } catch (IOException ex) {
            LOGGER.error("\nIOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        model.addAttribute("hasfile", true);
        model.addAttribute("smpfilesign", smpfilesign);

        return "smpeditor/signsmpfile";
    }

    /**
     * POST of file/files to sign
     *
     * @param smpfilesign
     * @param model
     * @param redirectAttributes
     * @return
     */
    @RequestMapping(value = "/smpeditor/signsmpfile", method = RequestMethod.POST)
    public String postSign(@ModelAttribute("smpfilesign") SMPFileOps smpfilesign, Model model, final RedirectAttributes redirectAttributes) throws IOException {

        LOGGER.debug("\n==== in postSign ====");
        model.addAttribute("smpfilesign", smpfilesign);

        List<SMPFileOps> allFiles = new ArrayList<>();
        List<MultipartFile> signFiles;
        signFiles = smpfilesign.getSignFiles();

        /*Iterate each chosen file*/
        for (int k = 0; k < signFiles.size(); k++) {

            LOGGER.debug("\n***** MULTIPLE FILE NAME '{}-{}'", k, signFiles.get(k).getOriginalFilename());
            SMPFileOps smpfile = new SMPFileOps();
            SMPFields smpfields = new SMPFields();

            smpfile.setSignFile(signFiles.get(k));

            File convFile = new File(Constants.SMP_DIR_PATH + smpfile.getSignFile().getOriginalFilename());
            try {
                smpfile.getSignFile().transferTo(convFile);
            } catch (IOException ex) {
                LOGGER.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
            } catch (IllegalStateException ex) {
                LOGGER.error("\n IllegalStateException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
            }
            String contentFile = new String(Files.readAllBytes(Paths.get(convFile.getPath())));
            boolean valid = XMLValidator.validate(contentFile, "/bdx-smp-201605.xsd");
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
                return "redirect:/smpeditor/signsmpfile";
            }
            fileDeleted = convFile.delete();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Converted File deleted: '{}'", fileDeleted);
            }

            ServiceMetadata serviceMetadata;
            serviceMetadata = smpconverter.convertFromXml(smpfile.getSignFile());

            boolean isSigned = smpconverter.getIsSignedServiceMetadata();
            if (isSigned) {
                LOGGER.debug("\n****SIGNED SMP File");
                fileDeleted = convFile.delete();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Converted File deleted: '{}'", fileDeleted);
                }
                String message = env.getProperty("warning.isSigned.sigmenu"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.warning));
                return "redirect:/smpeditor/signsmpfile";
            } else {
                LOGGER.debug("\n****NOT SIGNED File");
            }

      /*
       Condition to know the type of file (Redirect|ServiceInformation) in order to build the form
       */
            if (serviceMetadata.getRedirect() != null) {
                LOGGER.debug("\n******** REDIRECT");
                type = "Redirect";
                smpfile.setType(SMPType.Redirect);

                if (!serviceMetadata.getRedirect().getExtensions().isEmpty()) {
                    LOGGER.debug("\n******* SIGNED EXTENSION - " + serviceMetadata.getRedirect().getExtensions().get(0).getAny().getNodeName());
                    String message = env.getProperty("warning.isSignedExtension");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.warning);
                    smpfile.setAlert(alert);
                }
        
        /*
          get documentIdentifier and participantIdentifier from redirect href
        */
                String href = serviceMetadata.getRedirect().getHref();
                String participantID;
                String documentID = "";
                Pattern pattern = Pattern.compile(env.getProperty("ParticipantIdentifier.Scheme") + ".*");//SPECIFICATION
                Matcher matcher = pattern.matcher(href);
                if (matcher.find()) {
                    String result = matcher.group(0);
                    try {
                        result = java.net.URLDecoder.decode(result, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        LOGGER.error("\n UnsupportedEncodingException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                    }
                    String[] ids = result.split("/services/");//SPECIFICATION
                    participantID = ids[0];
                    String[] cc = participantID.split(":");//SPECIFICATION May change if Participant Identifier specification change

                    Countries[] countries = Countries.getALL();
                    for (Countries country : countries) {
                        if (cc[4].equals(country.name())) {
                            smpfile.setCountry(cc[4]);
                        }
                    }
                    if (smpfile.getCountry() == null) {
                        String message = env.getProperty("error.redirect.href.participantID"); //messages.properties
                        redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                        return "redirect:/smpeditor/signsmpfile";
                    }

                    String docID = ids[1];
                    HashMap<String, String> propertiesMap = readProperties.readPropertiesFile();
                    String[] nIDs = docID.split(env.getProperty("DocumentIdentifier.Scheme") + "::");//SPECIFICATION May change if Document Identifier specification change
                    String docuID = nIDs[1];
                    Set set2 = propertiesMap.entrySet();
                    for (Object aSet2 : set2) {
                        Map.Entry mentry2 = (Map.Entry) aSet2;
                        // LOGGER.debug("\n ****** " + mentry2.getKey().toString() + " = " + mentry2.getValue().toString());
                        if (docuID.equals(mentry2.getKey().toString())) {
                            String[] docs = mentry2.getValue().toString().split("\\.");
                            documentID = docs[0];
                            LOGGER.debug("\n ****** documentID - " + documentID);
                            break;
                        }
                    }

                    String smpType = documentID; //smpeditor.properties
                    if (smpType.equals("")) {
                        String message = env.getProperty("error.redirect.href.documentID"); //messages.properties
                        redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                        return "redirect:/smpeditor/signsmpfile";
                    }

                    /*Builds final file name*/
                    String timeStamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new java.util.Date());
                    String fileName = smpfile.getType().name() + "_" + smpType + "_" + smpfile.getCountry().toUpperCase() + "_Signed_" + timeStamp + ".xml";
                    smpfile.setFileName(fileName);
                    LOGGER.debug("\n********* FILENAME REDIRECT - " + fileName);

                } else {
                    LOGGER.error("\n****NOT VALID HREF IN REDIRECT");
                    String message = env.getProperty("error.redirect.href"); //messages.properties
                    redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                    return "redirect:/smpeditor/signsmpfile";
                }

            } else if (serviceMetadata.getServiceInformation() != null) { /*Service Information Type*/
                LOGGER.debug("\n******** SERVICE INFORMATION");
                type = "ServiceInformation";

                if (!serviceMetadata.getServiceInformation().getExtensions().isEmpty()) {
                    LOGGER.debug("\n******* SIGNED EXTENSION - " + serviceMetadata.getServiceInformation().getExtensions().get(0).getAny().getNodeName());
                    String message = env.getProperty("warning.isSignedExtension");//messages.properties
                    Alert alert = new Alert(message, Alert.alertType.warning);
                    smpfile.setAlert(alert);
                }

                smpfile.setDocumentIdentifier(serviceMetadata.getServiceInformation().getDocumentIdentifier().getValue());
                smpfile.setDocumentIdentifierScheme(serviceMetadata.getServiceInformation().getDocumentIdentifier().getScheme());
                String documentIdentifier = smpfile.getDocumentIdentifier();
                LOGGER.debug("\n************ DOC ID - " + documentIdentifier);

                String documentID = "";
                HashMap<String, String> propertiesMap = readProperties.readPropertiesFile();
                Set set2 = propertiesMap.entrySet();
                for (Object aSet2 : set2) {
                    Map.Entry mentry2 = (Map.Entry) aSet2;
                    // LOGGER.debug("\n ****** " + mentry2.getKey().toString() + " = " + mentry2.getValue().toString());
                    if (documentIdentifier.equals(mentry2.getKey().toString())) {
                        String[] docs = mentry2.getValue().toString().split("\\.");
                        documentID = docs[0];
                        LOGGER.debug("\n ****** documentID - " + documentID);
                        break;
                    }
                }

                SMPType[] smptypes = SMPType.getALL();
                for (SMPType smptype : smptypes) {
                    if (smptype.name().equals(documentID)) {
                        smpfile.setType(smptype);
                        break;
                    }
                }
                if (smpfile.getType() == null) {
                    String message = env.getProperty("error.serviceinformation.documentID"); //messages.properties
                    redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                    return "redirect:/smpeditor/signsmpfile";
                }

                String participanteID = serviceMetadata.getServiceInformation().getParticipantIdentifier().getValue();
                LOGGER.debug("\n ************* participanteID - " + participanteID);
                String[] cc = participanteID.split(":");
                for (int i = 0; i < cc.length; i++) {
                    LOGGER.debug("\n ************* CC - " + i + " -> " + cc[i]);
                }

                Countries[] countries = Countries.getALL();
                for (Countries country : countries) {
                    if (cc[2].equals(country.name())) {
                        smpfile.setCountry(cc[2]);
                    }
                }
                if (smpfile.getCountry() == null) {
                    String message = env.getProperty("error.serviceinformation.participantID"); //messages.properties
                    redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                    return "redirect:/smpeditor/signsmpfile";
                }

                /*Builds final file name*/
                String timeStamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new java.util.Date());
                String fileName = smpfile.getType().name() + "_" + smpfile.getCountry().toUpperCase() + "_Signed_" + timeStamp + ".xml";
                smpfile.setFileName(fileName);

                smpfile.setParticipantIdentifier(participanteID);
                smpfile.setParticipantIdentifierScheme(serviceMetadata.getServiceInformation().getParticipantIdentifier().getScheme());
                smpfile.setProcessIdentifier(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getProcessIdentifier().getValue());
                smpfile.setProcessIdentifierScheme(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getProcessIdentifier().getScheme());
                smpfile.setTransportProfile(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0).getTransportProfile());
                smpfile.setRequiredBusinessLevelSig(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0).isRequireBusinessLevelSignature());
                smpfile.setMinimumAutenticationLevel(serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0).getMinimumAuthenticationLevel());
            }

            /*
             * Read smpeditor.properties file
             */
            readProperties.readProperties(smpfile);

            smpfields.setUri(readProperties.getUri());
            smpfields.setServiceActivationDate(readProperties.getServiceActDate());
            smpfields.setServiceExpirationDate(readProperties.getServiceExpDate());
            smpfields.setCertificate(readProperties.getCertificate());
            smpfields.setServiceDescription(readProperties.getServiceDesc());
            smpfields.setTechnicalContactUrl(readProperties.getTechContact());
            smpfields.setTechnicalInformationUrl(readProperties.getTechInformation());
            smpfields.setExtension(readProperties.getExtension());
            smpfields.setRedirectHref(readProperties.getRedirectHref());
            smpfields.setCertificateUID(readProperties.getCertificateUID());


            if ("ServiceInformation".equals(type)) {
                EndpointType endpoint = serviceMetadata.getServiceInformation().getProcessList().getProcesses().get(0).getServiceEndpointList().getEndpoints().get(0);

                X509Certificate cert = null;
                String subjectName = null;
                if (smpfields.getCertificate().isEnable()) {
                    try {
                        InputStream in = new ByteArrayInputStream(endpoint.getCertificate());
                        cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
                        if (cert != null) {
                            subjectName = "Issuer: " + cert.getIssuerX500Principal().getName() + "\nSerial Number #" + cert.getSerialNumber();
                            smpfile.setCertificateContent(subjectName);
                            smpfile.setCertificate(cert.getEncoded());
                        }
                    } catch (CertificateException ex) {
                        LOGGER.error("\n CertificateException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                    }
                } else {
                    smpfile.setCertificate(null);
                }

                smpfile.setEndpointURI(endpoint.getEndpointURI());

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date cal = endpoint.getServiceActivationDate().toGregorianCalendar().getTime();
                String formatted = format.format(cal);
                Date cal2 = endpoint.getServiceExpirationDate().toGregorianCalendar().getTime();
                String formatted2 = format.format(cal2.getTime());

                smpfile.setServiceActivationDateS(formatted);
                smpfile.setServiceExpirationDateS(formatted2);
                smpfile.setCertificateContent(subjectName);
                smpfile.setServiceDescription(endpoint.getServiceDescription());
                smpfile.setTechnicalContactUrl(endpoint.getTechnicalContactUrl());
                smpfile.setTechnicalInformationUrl(endpoint.getTechnicalInformationUrl());

                if (smpfields.getExtension().isEnable()) {
                    try (Scanner scanner = new Scanner(smpfile.getSignFile().getInputStream(), StandardCharsets.UTF_8.name())) {
                        //String content = new Scanner(smpfile.getSignFile().getInputStream()).useDelimiter("\\Z").next();
                        String content = scanner.useDelimiter("\\Z").next();
                        String capturedString = content.substring(content.indexOf("<Extension>"), content.indexOf("</Extension>"));
                        String[] endA = capturedString.split("<Extension>");
                        smpfile.setExtensionContent(endA[1]);
                    } catch (IOException ex) {
                        LOGGER.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                    }
                }

            } else if ("Redirect".equals(type)) {
                RedirectType redirect = serviceMetadata.getRedirect();
                smpfile.setCertificateUID(redirect.getCertificateUID());
                smpfile.setHref(redirect.getHref());
            }

            smpfile.setTypeS(smpfile.getType().getDescription());
            smpfile.setId(k);
            smpfile.setSmpfields(smpfields);

            allFiles.add(k, smpfile);
        }
        smpfilesign.setAllFiles(allFiles);

        LOGGER.debug("\n********* MODEL - " + model.toString());

        return "redirect:checksignsmpfile";
    }

    /**
     * Generate UpdateFileForm page
     *
     * @param smpfilesign
     * @param model
     * @return
     */
    @RequestMapping(value = "smpeditor/checksignsmpfile", method = RequestMethod.GET)
    public String signFileForm(@ModelAttribute("smpfilesign") SMPFileOps smpfilesign, Model model) {
        LOGGER.debug("\n==== in signFileForm ====");
        model.addAttribute("smpfilesign", smpfilesign);
        model.addAttribute("smpfiles", smpfilesign.getAllFiles());

        LOGGER.debug("\n********* MODEL - " + model.toString());
        return "smpeditor/checksignsmpfile";
    }

    /**
     * Sign the file
     * Calls SignFile to sign the xml files
     *
     * @param smpfilesign
     * @param model
     * @param redirectAttributes
     * @return
     */
    @RequestMapping(value = "/smpeditor/checksignsmpfile", method = RequestMethod.POST)
    public String signSMPFile(@ModelAttribute("smpfilesign") SMPFileOps smpfilesign, Model model, final RedirectAttributes redirectAttributes) {
        LOGGER.debug("\n==== in signSMPFile ====");
    
    /*
    mock
    sample_national_infrastructure
    mock
    */

        File file = new File(ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_KEYSTORE_PATH"));
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            LOGGER.error("\n FileNotFoundException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }
        MultipartFile keystore = null;
        try {
            keystore = new MockMultipartFile("keystore", file.getName(), "text/xml", input);
        } catch (IOException ex) {
            LOGGER.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        for (int i = 0; i < smpfilesign.getAllFiles().size(); i++) {
            SignFile signFile = new SignFile();
            try {
                signFile.signFiles(smpfilesign.getAllFiles().get(i).getType().name(),
                        smpfilesign.getAllFiles().get(i).getFileName(),
                        keystore,
                        ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_KEYSTORE_PASSWORD"),
                        ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_PRIVATEKEY_ALIAS"),
                        ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_PRIVATEKEY_PASSWORD"),
                        smpfilesign.getAllFiles().get(i).getSignFile());
            } catch (Exception ex) {
                LOGGER.error("\nException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
            }

            if (signFile.isInvalidKeystoreSMP()) {
                LOGGER.error("\n****INVALID KEYSTORE");
                String message = env.getProperty("error.keystore.invalid"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                return "redirect:/smpeditor/signsmpfile";
            }
            if (signFile.isInvalidKeyPairSMP()) {
                LOGGER.error("\n****INVALID KEY PAIR");
                String message = env.getProperty("error.keypair.invalid"); //messages.properties
                redirectAttributes.addFlashAttribute("alert", new Alert(message, Alert.alertType.danger));
                return "redirect:/smpeditor/signsmpfile";
            }

            smpfilesign.getAllFiles().get(i).setGeneratedFile(signFile.getGeneratedSignFile());
        }

        return "redirect:/smpeditor/savesignedsmpfile";
    }

    /**
     * Generate savesignedsmpfile page
     *
     * @param smpfilesign
     * @param model
     * @return
     */
    @RequestMapping(value = "smpeditor/savesignedsmpfile", method = RequestMethod.GET)
    public String saveSignFile(@ModelAttribute("smpfilesign") SMPFileOps smpfilesign, Model model) {
        LOGGER.debug("\n==== in saveSignFile ====");
        model.addAttribute("smpfilesign", smpfilesign);
        model.addAttribute("smpfiles", smpfilesign.getAllFiles());

        return "smpeditor/savesignedsmpfile";
    }

    /**
     * Download SignedSMPFile
     *
     * @param smpfilesign
     * @param request
     * @param response
     * @param model
     * @param filename
     */
    @RequestMapping(value = "smpeditor/savesignedsmpfile/download/{filename}", method = RequestMethod.GET)
    public void downloadFile(@ModelAttribute("smpfilesign") SMPFileOps smpfilesign, HttpServletRequest request,
                             HttpServletResponse response, Model model, @PathVariable("filename") String filename) {
        LOGGER.debug("\n==== in download Signed File ====");
        model.addAttribute("smpfilesign", smpfilesign);

        for (int i = 0; i < smpfilesign.getAllFiles().size(); i++) {

            if (smpfilesign.getAllFiles().get(i).getFileName().equals(filename + ".xml")) {
                response.setContentType("application/xml");
                response.setHeader("Content-Disposition", "attachment; filename=" + smpfilesign.getAllFiles().get(i).getFileName());
                response.setContentLength((int) smpfilesign.getAllFiles().get(i).getGeneratedFile().length());
                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(smpfilesign.getAllFiles().get(i).getGeneratedFile()))) {

                    FileCopyUtils.copy(inputStream, response.getOutputStream());

                } catch (FileNotFoundException ex) {
                    LOGGER.error("\n FileNotFoundException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (IOException ex) {
                    LOGGER.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                }
            }
        }
    }

    /**
     * Clean SMPFile - clean the generated xml file in server
     *
     * @param smpfilesign
     * @param model
     * @return
     */
    @RequestMapping(value = "smpeditor/smpeditor/clean3", method = RequestMethod.GET)
    public String cleanSmpFile(@ModelAttribute("smpfilesign") SMPFileOps smpfilesign, Model model) {
        LOGGER.debug("\n==== in cleanSmpFile ====");
        model.addAttribute("smpfilesign", smpfilesign);
        for (int i = 0; i < smpfilesign.getAllFiles().size(); i++) {
            if (smpfilesign.getAllFiles().get(i).getGeneratedFile() != null) {
                LOGGER.debug("\n****DELETED ? " + smpfilesign.getAllFiles().get(i).getGeneratedFile().delete());
            }
        }
        return "redirect:/smpeditor/smpeditor";
    }

    /**
     * Clean SMPFile - clean the generated xml file in server
     *
     * @param smpfilesign
     * @param model
     * @return
     */
    @RequestMapping(value = "smpeditor/checksignsmpfile/clean", method = RequestMethod.GET)
    public String cleanFile(@ModelAttribute("smpfilesign") SMPFileOps smpfilesign, Model model) {
        LOGGER.debug("\n==== in cleanFile ====");
        model.addAttribute("smpfilesign", smpfilesign);
        for (int i = 0; i < smpfilesign.getAllFiles().size(); i++) {
            if (smpfilesign.getAllFiles().get(i).getGeneratedFile() != null) {
                LOGGER.debug("\n****DELETED ? " + smpfilesign.getAllFiles().get(i).getGeneratedFile().delete());
            }
        }
        return "redirect:/smpeditor/checksignsmpfile";
    }
}
