package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.Constants;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.SMPFieldProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.SimpleErrorHandler;
import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ehdsi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.lang.Object;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Service responsible for converting the data introduced by the user to a xml file.
 *
 * @author Inês Garganta
 */
@Service
public class SMPConverter {

    private static final JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(SignedServiceMetadata.class, ServiceMetadata.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(SMPConverter.class);
    private final Environment environment;
    private String certificateSubjectName;
    private File generatedFile;
    private boolean nullExtension = false;

    @Autowired
    public SMPConverter(Environment environment) {
        this.environment = environment;
    }

    /**
     * Converts the data received from the SMPGenerateFileController to a XML file
     */
    public void convertToXml(String type, String issuanceType, String countryCode, String endpointUri, String servDescription,
                             String tecContact, String tecInformation, Date servActDate, Date servExpDate,
                             String extension, FileInputStream certificateFile, String fileName,
                             SMPFieldProperties businessLevelSignature, SMPFieldProperties minimumAuthLevel,
                             String certificateUID, String redirectHref) {

        logger.debug("Converting SMP Model to XML");
        ObjectFactory objectFactory = new ObjectFactory();
        ServiceMetadata serviceMetadata = objectFactory.createServiceMetadata();

        //XML file generated at path
        generatedFile = new File(Constants.SMP_DIR_PATH + File.separator + fileName);

        //Type of SMP File -> Redirect | Service Information
        if ("Redirect".equals(type)) {

            //  Redirect SMP Type
            logger.debug("Type Redirect");
            RedirectType redirectType = objectFactory.createRedirectType();

            redirectType.setCertificateUID(certificateUID);
            redirectType.setHref(redirectHref);

            serviceMetadata.setRedirect(redirectType);
        } else {
            //  ServiceInformation SMP Type
            logger.debug("Type ServiceInformation");
            DocumentIdentifier documentIdentifier = objectFactory.createDocumentIdentifier();
            EndpointType endpointType = objectFactory.createEndpointType();
            ExtensionType extensionType = objectFactory.createExtensionType();
            ParticipantIdentifierType participantIdentifierType = objectFactory.createParticipantIdentifierType();
            ProcessIdentifier processIdentifier = objectFactory.createProcessIdentifier();
            ProcessListType processListType = objectFactory.createProcessListType();
            ProcessType processType = objectFactory.createProcessType();
            ServiceEndpointList serviceEndpointList = objectFactory.createServiceEndpointList();
            ServiceInformationType serviceInformationType = objectFactory.createServiceInformationType();

            createStaticFields(type, issuanceType, countryCode, documentIdentifier, endpointType, participantIdentifierType,
                    processIdentifier, businessLevelSignature, minimumAuthLevel);

            //  URI definition
            if (endpointUri == null) {
                endpointUri = "";
            }
            //  Set by user
            endpointType.setEndpointURI(endpointUri);

            /*
             * Dates parse to Calendar
             */
            Calendar activationDate = Calendar.getInstance();
            activationDate.setTime(servActDate);
            endpointType.setServiceActivationDate(activationDate);


            if (servExpDate == null) {

                endpointType.setServiceExpirationDate(null);
            } else {
                Calendar expirationDate = Calendar.getInstance();
                expirationDate.setTime(servExpDate);
                endpointType.setServiceExpirationDate(expirationDate);
            }

            //  Parsing Certificate
            if (certificateFile != null) {

                try {
                    String certPass = environment.getProperty(type + ".certificate.password");
                    String certAlias = environment.getProperty(type + ".certificate.alias");
                    String certificatePass = ConfigurationManagerFactory.getConfigurationManager().getProperty(certPass);
                    String certificateAlias = ConfigurationManagerFactory.getConfigurationManager().getProperty(certAlias);
                    logger.debug("Certificate Info: '{}', '{}', '{}', '{}'", certAlias, certificateAlias, certPass,
                            StringUtils.isNotBlank(certificatePass) ? "******" : "N/A");

                    // eHDSI OpenNCP has been using only JKS keystore.
                    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                    ks.load(certificateFile, certificatePass.toCharArray());

                    if (ks.isKeyEntry(certificateAlias)) {

                        Certificate[] certs = ks.getCertificateChain(certificateAlias);
                        if (certs != null) {
                            if (logger.isDebugEnabled()) {

                                for (Certificate certificate : certs) {
                                    logger.debug("Certificate Info: '{}' - '{}'", ((X509Certificate) certificate).getSerialNumber(),
                                            ((X509Certificate) certificate).getSubjectDN().getName());
                                }
                            }
                            if (certs[0] instanceof X509Certificate) {

                                X509Certificate x509 = (X509Certificate) certs[0];
                                endpointType.setCertificate(x509.getEncoded());
                                certificateSubjectName = x509.getIssuerX500Principal().getName() + " Serial Number #" + x509.getSerialNumber();
                            }
                        } else {
                            logger.error("Keystore not configured for TLS certificate with alias: '{}'", certificateAlias);
                        }
                    } else if (ks.isCertificateEntry(certificateAlias)) {

                        Certificate c = ks.getCertificate(certificateAlias);
                        if (c != null) {
                            if (c instanceof X509Certificate) {
                                X509Certificate x509 = (X509Certificate) c;
                                endpointType.setCertificate(x509.getEncoded());
                                certificateSubjectName = x509.getIssuerX500Principal().getName() + " Serial Number #" + x509.getSerialNumber();
                            }
                        } else {
                            logger.error("Keystore not configured for TLS certificate with alias: '{}'", certificateAlias);
                        }
                    } else {
                        logger.error("\n ********** '{}' is unknown to this keystore", certificateAlias);
                    }

                } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
                    logger.error("{}: - '{}'", e.getClass(), e.getMessage(), e);
                }

            } else {
                byte[] by = "".getBytes();
                endpointType.setCertificate(by);
            }

            //   Endpoint Service Description, Technical ContactUrl and Technical InformationUrl definition
            endpointType.setServiceDescription(servDescription); //Set by User
            endpointType.setTechnicalContactUrl(tecContact); //Set by User
            endpointType.setTechnicalInformationUrl(tecInformation); //Set by User

            //  Extension values not used in eHDSI
            extensionType.setExtensionAgencyID(null);
            extensionType.setExtensionAgencyName(null);
            extensionType.setExtensionAgencyURI(null);
            extensionType.setExtensionID(null);
            extensionType.setExtensionName(null);
            extensionType.setExtensionReason(null);
            extensionType.setExtensionReasonCode(null);
            extensionType.setExtensionURI(null);
            extensionType.setExtensionVersionID(null);

            //    Endpoint Extension file parse
            if (StringUtils.isNotBlank(extension)) {
                nullExtension = false;
                logger.info("XML Extension Content:\n'{}'", extension);
                Document docOriginal = parseStringToDocument(extension);
                if (docOriginal != null) {
                    //Adding ISM Extension to SMP file.
                    docOriginal.getDocumentElement().normalize();
                    extensionType.setAny(docOriginal.getDocumentElement()); //Set by user
                    endpointType.getExtensions().add(extensionType);
                }
            }

            processType.setProcessIdentifier(processIdentifier);
            processType.setServiceEndpointList(serviceEndpointList);

            serviceEndpointList.getEndpoints().add(endpointType);
            processListType.getProcesses().add(processType);

            serviceInformationType.setDocumentIdentifier(documentIdentifier);
            serviceInformationType.setParticipantIdentifier(participantIdentifierType);
            serviceInformationType.setProcessList(processListType);

            serviceMetadata.setServiceInformation(serviceInformationType);
        }

        getXMLFile(serviceMetadata);
    }

    /**
     * Converts the data received from the NewSMPFileUpdate to a xml file
     *
     * @param type
     * @param countryCode
     * @param documentID
     * @param documentIDScheme
     * @param participantID
     * @param participantIDScheme
     * @param processID
     * @param processIDScheme
     * @param transportProfile
     * @param requiredBusinessLevelSig
     * @param minimumAutenticationLevel
     * @param endpointUri
     * @param servDescription
     * @param tecContact
     * @param tecInformation
     * @param servActDate
     * @param servExpDate
     * @param certificate
     * @param certificateFile
     * @param extension
     * @param extensionFile
     * @param fileName
     * @param redirectHref
     * @param certificateUID
     */
    public void updateToXml(String type, String countryCode, String documentID, String documentIDScheme, String participantID,
                            String participantIDScheme, String processID, String processIDScheme, String transportProfile,
                            Boolean requiredBusinessLevelSig, String minimumAutenticationLevel, String endpointUri,
                            String servDescription, String tecContact, String tecInformation, Date servActDate,
                            Date servExpDate, byte[] certificate, FileInputStream certificateFile, Element extension,
                            MultipartFile extensionFile, String fileName, String certificateUID, String redirectHref) {

        logger.debug("Update SMP Model to XML");

        ObjectFactory objectFactory = new ObjectFactory();
        ServiceMetadata serviceMetadata = objectFactory.createServiceMetadata();
//        ServiceMetadataType serviceMetadataType
        //XML file generated at path
        generatedFile = new File(Constants.SMP_DIR_PATH + File.separator + fileName);

        //Type of SMP File -> Redirect | Service Information
        if ("Redirect".equals(type)) {

            //  Redirect SMP Type
            logger.debug("Type Redirect");
            RedirectType redirectType = objectFactory.createRedirectType();
            redirectType.setCertificateUID(certificateUID);
            redirectType.setHref(redirectHref);
            serviceMetadata.setRedirect(redirectType);

        } else {

            //  ServiceInformation SMP Type
            logger.debug("Type ServiceInformation");
            DocumentIdentifier documentIdentifier = objectFactory.createDocumentIdentifier();
            EndpointType endpointType = objectFactory.createEndpointType();
            ExtensionType extensionType = objectFactory.createExtensionType();
            ParticipantIdentifierType participantIdentifierType = objectFactory.createParticipantIdentifierType();
            ProcessIdentifier processIdentifier = objectFactory.createProcessIdentifier();
            ProcessListType processListType = objectFactory.createProcessListType();
            ProcessType processType = objectFactory.createProcessType();
            ServiceEndpointList serviceEndpointList = objectFactory.createServiceEndpointList();
            ServiceInformationType serviceInformationType = objectFactory.createServiceInformationType();

            //  Fields fetched from file
            participantIdentifierType.setScheme(participantIDScheme);
            participantIdentifierType.setValue(participantID);
            documentIdentifier.setScheme(documentIDScheme);
            documentIdentifier.setValue(documentID);
            processIdentifier.setScheme(processIDScheme);
            processIdentifier.setValue(processID);

            endpointType.setTransportProfile(transportProfile);
            endpointType.setRequireBusinessLevelSignature(requiredBusinessLevelSig);
            endpointType.setMinimumAuthenticationLevel(minimumAutenticationLevel);

            /*User fields*/
            /*
             * URI definition
             */
            if (endpointUri == null) {
                endpointUri = "";
            }
            endpointType.setEndpointURI(endpointUri);//Set by user

            /*
             * Dates parse to Calendar
             */
            Calendar activationDate = Calendar.getInstance();
            activationDate.setTime(servActDate);
            endpointType.setServiceActivationDate(activationDate);

            if (servExpDate == null) {

                endpointType.setServiceExpirationDate(null);
            } else {
                Calendar expirationDate = Calendar.getInstance();
                activationDate.setTime(servExpDate);
                endpointType.setServiceExpirationDate(expirationDate);
            }

            // Parsing TLS certificate
            if (certificateFile != null) {
                try {
                    String certPass = environment.getProperty(type + ".certificate.password");
                    String certAlias = environment.getProperty(type + ".certificate.alias");
                    String certificatePass = ConfigurationManagerFactory.getConfigurationManager().getProperty(certPass);
                    String certificateAlias = ConfigurationManagerFactory.getConfigurationManager().getProperty(certAlias);
                    logger.info("Certificate Info: '{}', '{}', '{}', '{}'", certAlias, certificateAlias, certPass,
                            StringUtils.isNotBlank(certificatePass) ? "******" : "N/A");

                    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                    ks.load(certificateFile, null);

                    if (ks.isKeyEntry(certificateAlias)) {

                        Certificate[] certs = ks.getCertificateChain(certificateAlias);
                        if (certs != null) {

                            if (certs[0] instanceof X509Certificate) {

                                X509Certificate x509 = (X509Certificate) certs[0];
                                endpointType.setCertificate(x509.getEncoded());
                                certificateSubjectName = x509.getIssuerX500Principal().getName() + " Serial Number #" + x509.getSerialNumber();
                            }
                        } else {
                            logger.error("Keystore not configured for TLS certificate with alias: '{}'", certificateAlias);
                        }
                    } else if (ks.isCertificateEntry(certificateAlias)) {

                        Certificate c = ks.getCertificate(certificateAlias);
                        if (c != null) {
                            if (c instanceof X509Certificate) {
                                X509Certificate x509 = (X509Certificate) c;
                                endpointType.setCertificate(x509.getEncoded());
                                certificateSubjectName = x509.getIssuerX500Principal().getName() + " Serial Number #" + x509.getSerialNumber();
                            }
                        } else {
                            logger.error("Keystore not configured for TLS certificate with alias: '{}'", certificateAlias);
                        }
                    } else {
                        logger.debug("\n ********** '{}' is unknown to this keystore", certificateAlias);
                    }
                } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
                    logger.error("\n{} - '{}'", ex.getClass(), SimpleErrorHandler.printExceptionStackTrace(ex));
                }
            } else {
                byte[] by = "".getBytes();
                endpointType.setCertificate(by);
            }

            //  Endpoint Service Description, Technical ContactUrl and Technical InformationUrl definition
            endpointType.setServiceDescription(servDescription); //Set by User
            endpointType.setTechnicalContactUrl(tecContact); //Set by User
            endpointType.setTechnicalInformationUrl(tecInformation); //Set by User

            /*Not used*/
            extensionType.setExtensionAgencyID(null);
            extensionType.setExtensionAgencyName(null);
            extensionType.setExtensionAgencyURI(null);
            extensionType.setExtensionID(null);
            extensionType.setExtensionName(null);
            extensionType.setExtensionReason(null);
            extensionType.setExtensionReasonCode(null);
            extensionType.setExtensionURI(null);
            extensionType.setExtensionVersionID(null);

      /*
       Endpoint Extension file parse
       */
            if (extensionFile == null) {
                if (extension != null) {
                    extensionType.setAny(extension); //Set by user
                    endpointType.getExtensions().add(extensionType);
                } else {
                    //Does not add extension
                }

            } else {
                logger.debug("\n********* CONVERTER EXTENSION FILE - '{}'", extensionFile.getOriginalFilename());

                nullExtension = false;
                Document docOriginal = null;
                try {
                    String content = new Scanner(extensionFile.getInputStream()).useDelimiter("\\Z").next();

                    docOriginal = parseDocument(content);

                } catch (FileNotFoundException ex) {
                    nullExtension = true;
                    logger.error("\n FileNotFoundException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (IOException ex) {
                    nullExtension = true;
                    logger.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (SAXException ex) {
                    nullExtension = true;
                    logger.error("\n SAXException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (ParserConfigurationException ex) {
                    nullExtension = true;
                    logger.error("\n ParserConfigurationException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                }

                if (nullExtension) {
                    //Does not add extension
                } else {
                    docOriginal.getDocumentElement().normalize();
                    extensionType.setAny(docOriginal.getDocumentElement()); //Set by user
                    endpointType.getExtensions().add(extensionType);
                }
            }

            processType.setProcessIdentifier(processIdentifier);
            processType.setServiceEndpointList(serviceEndpointList);

            serviceEndpointList.getEndpoints().add(endpointType);
            processListType.getProcesses().add(processType);

            serviceInformationType.setDocumentIdentifier(documentIdentifier);
            serviceInformationType.setParticipantIdentifier(participantIdentifierType);
            serviceInformationType.setProcessList(processListType);

            serviceMetadata.setServiceInformation(serviceInformationType);
        }

        //Generate XML file
        getXMLFile(serviceMetadata);
    }

    public Object convertFromXml(File file) {

        ObjectFactory objectFactory = new ObjectFactory();

        Object result = null;

        try {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            result = jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException ex) {
            logger.error("JAXBException - " + ex.getErrorCode(), ex);
        }

        return result;
    }

    public boolean isSignedServiceMetadata(Object object) {
        return (object instanceof SignedServiceMetadata);
    }

    public ServiceMetadata getServiceMetadata(Object object) {

        ServiceMetadata serviceMetadata;

        if (object instanceof SignedServiceMetadata) {
            serviceMetadata = ((SignedServiceMetadata) object).getServiceMetadata();
        } else if (object instanceof ServiceMetadata) {
            serviceMetadata = (ServiceMetadata) object;
        } else {
            throw new RuntimeException("Should be instance of SignedServiceMetadata or ServiceMetadata");
        }

        return serviceMetadata;
    }


    /**
     * Defines the static fields of the SMP File
     */
    private void createStaticFields(String type, String issuanceType, String countryCode, DocumentIdentifier documentIdentifier,
                                    EndpointType endpointType, ParticipantIdentifierType participantIdentifierType,
                                    ProcessIdentifier processIdentifier, SMPFieldProperties businessLevelSignature,
                                    SMPFieldProperties minimumAuthLevel) {

    /*
     Document and Participant identifiers definition
     */
        participantIdentifierType.setScheme(environment.getProperty(type + ".ParticipantIdentifier.Scheme")); ///in smpeditor.properties
    /*
    servidor -- :ncpa-idp
    client -- :ncp-idp
    */
   /* if(clientServer == 1){
      participantIdentifierType.setValue("urn:ehealth:" + countryCode + ":ncpa-idp"); //set by user (countryCode - country)
    } else if (clientServer == 2){
      participantIdentifierType.setValue("urn:ehealth:" + countryCode + ":ncp-idp"); //set by user (countryCode - country)
    }*/

        String participantID = environment.getProperty(type + ".ParticipantIdentifier.value");//in smpeditor.properties
        participantID = String.format(participantID, countryCode); //Add country in place of %2s
        participantIdentifierType.setValue(participantID);

        documentIdentifier.setScheme(environment.getProperty(type + ".DocumentIdentifier.Scheme"));//in smpeditor.properties

        if (StringUtils.isNotBlank(issuanceType)) {
            documentIdentifier.setValue(environment.getProperty(type + ".DocumentIdentifier." + issuanceType));//in smpeditor.properties
        } else {
            documentIdentifier.setValue(environment.getProperty(type + ".DocumentIdentifier"));//in smpeditor.properties
        }

    /*
     Process identifiers definition
     */
        processIdentifier.setScheme(environment.getProperty(type + ".ProcessIdentifier.Scheme"));//in smpeditor.properties
        if (StringUtils.isNotBlank(issuanceType)) {
            processIdentifier.setValue(environment.getProperty(type + ".ProcessIdentifier." + issuanceType));
        } else {
            String processID = environment.getProperty(type + ".ProcessIdentifier");//in smpeditor.properties
            processID = String.format(processID, countryCode); //Add country if %2s is present in the string
            processIdentifier.setValue(processID);
        }

    /*
     Endpoint Transport Profile definition
     */
        endpointType.setTransportProfile(environment.getProperty(type + ".transportProfile")); //in smpeditor.properties

    /*
     BusinessLevelSignature and MinimumAuthenticationLevel definition
     */
        if (businessLevelSignature.isEnable()) {
            Boolean requireBusinessLevelSignature = Boolean.parseBoolean(environment.getProperty(type + ".RequireBusinessLevelSignature"));
            endpointType.setRequireBusinessLevelSignature(requireBusinessLevelSignature); //in smpeditor.properties
        }
        if (minimumAuthLevel.isEnable()) {
            endpointType.setMinimumAuthenticationLevel(environment.getProperty(type + ".MinimumAuthenticationLevel")); //in smpeditor.properties
        }
    }

    /**
     * Marshal the data to a XML file
     */
    private void getXMLFile(ServiceMetadata serviceMetadata) {

        logger.debug("Generate XML SMP file: '{}'", serviceMetadata.getServiceInformation().getParticipantIdentifier().getValue());

        // Generates the final SMP XML file
        XMLStreamWriter xsw;

        try (FileOutputStream generatedFileOS = new FileOutputStream(generatedFile)) {

            xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(generatedFileOS, StandardCharsets.UTF_8.name());
            xsw.setNamespaceContext(new NamespaceContext() {
                @Override
                public Iterator getPrefixes(String namespaceURI) {
                    return null;
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    return "";
                }

                @Override
                public String getNamespaceURI(String prefix) {
                    return null;
                }
            });

            StringWriter stringWriter = new StringWriter();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
            jaxbMarshaller.marshal(serviceMetadata, generatedFileOS);
            jaxbMarshaller.marshal(serviceMetadata, stringWriter);

            logger.debug("Service Metadata:\n{}", stringWriter);
            generatedFileOS.flush();

        } catch (JAXBException | IOException | XMLStreamException e) {
            logger.error("Exception:'{}'", SimpleErrorHandler.printExceptionStackTrace(e));
        }
    }

    /*Sets and gets*/
    public String getCertificateSubjectName() {
        return certificateSubjectName;
    }

    public void setCertificateSubjectName(String certificateSubjectName) {
        this.certificateSubjectName = certificateSubjectName;
    }

    public File getFile() {
        return generatedFile;
    }

    public void setFile(File generatedFile) {
        this.generatedFile = generatedFile;
    }

    public boolean isNullExtension() {
        return nullExtension;
    }

    public void setNullExtension(boolean nullExtension) {
        this.nullExtension = nullExtension;
    }

    /*Auxiliary*/
    public Document parseDocument(String docContent) throws IOException, SAXException, ParserConfigurationException {

        InputStream inputStream = new ByteArrayInputStream(docContent.getBytes(StandardCharsets.UTF_8));
        getDocumentBuilder().setErrorHandler(new SimpleErrorHandler());
        return getDocumentBuilder().parse(inputStream);
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder();
    }

    private Document parseStringToDocument(String document) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException e) {
            logger.error("SAXException: ", e);
        } catch (IOException e) {
            logger.error("IOException: ", e);
        } catch (ParserConfigurationException e) {
            logger.error("ParserConfigurationException: ", e);
        }
        return doc;
    }
}
