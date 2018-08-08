package eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.service;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.Constants;
import eu.europa.ec.sante.ehdsi.openncp.gateway.smpeditor.entities.SMPFieldProperties;
import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.eu.*;
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
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Service responsible for converting the data introduced by the user to a xml file.
 *
 * @author Inês Garganta
 */
@Service
public class SMPConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMPConverter.class);
    SimpleErrorHandler error;
    Boolean isSignedServiceMetadata;
    @Autowired
    private Environment env;
    private String certificateSubjectName;
    private File generatedFile;
    private boolean nullExtension = false;

    /**
     * Converts the data received from the SMPGenerateFileController to a xml file
     */
    public void convertToXml(String type, String issuanceType, String CC, String endpointUri,
                             String servDescription, String tecContact, String tecInformation, Date servActDate,
                             Date servExpDate, MultipartFile extension, FileInputStream certificateFile, String fileName,
                             SMPFieldProperties businessLevelSignature, SMPFieldProperties minimumAuthLevel,
                             String certificateUID, String redirectHref) {

        LOGGER.debug("\n==== in converteToXML ====");

        ObjectFactory objectFactory = new ObjectFactory();
        ServiceMetadata serviceMetadata = objectFactory.createServiceMetadata();

        //XML file generated at path
        generatedFile = new File(Constants.SMP_DIR_PATH + "/" + fileName);

        //Type of SMP File -> Redirect | Service Information
        if ("Redirect".equals(type)) {
      /*
       Redirect SMP Type
      */
            LOGGER.debug("\n******* Redirect ************");
            RedirectType redirectType = objectFactory.createRedirectType();

            redirectType.setCertificateUID(certificateUID);
            redirectType.setHref(redirectHref);

            serviceMetadata.setRedirect(redirectType);
        } else {
      /*
       ServiceInformation SMP Type
       */
            LOGGER.debug("\n******* ServiceInformation ************");
            DocumentIdentifier documentIdentifier = objectFactory.createDocumentIdentifier();
            EndpointType endpointType = objectFactory.createEndpointType();
            ExtensionType extensionType = objectFactory.createExtensionType();
            ParticipantIdentifierType participantIdentifierType = objectFactory.createParticipantIdentifierType();
            ProcessIdentifier processIdentifier = objectFactory.createProcessIdentifier();
            ProcessListType processListType = objectFactory.createProcessListType();
            ProcessType processType = objectFactory.createProcessType();
            ServiceEndpointList serviceEndpointList = objectFactory.createServiceEndpointList();
            ServiceInformationType serviceInformationType = objectFactory.createServiceInformationType();

            createStaticFields(type, /*clientServer,*/ issuanceType, CC, documentIdentifier, endpointType, participantIdentifierType,
                    processIdentifier, businessLevelSignature, minimumAuthLevel);

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
            Calendar calad = Calendar.getInstance();
            calad.setTime(servActDate);
            int yearad = calad.get(Calendar.YEAR);
            int monthad = calad.get(Calendar.MONTH);
            int dayad = calad.get(Calendar.DAY_OF_MONTH);
            int hourat = calad.get(Calendar.HOUR_OF_DAY);
            int minat = calad.get(Calendar.MINUTE);

            GregorianCalendar calendarAD = new GregorianCalendar(yearad, monthad, dayad, hourat, minat);
            try {
                XMLGregorianCalendar xmlGregorianCalendarAD = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendarAD);
                endpointType.setServiceActivationDate(xmlGregorianCalendarAD);//Set by user
            } catch (DatatypeConfigurationException e) {
                LOGGER.error("DatatypeConfigurationException: '{}'", e.getMessage(), e);
            }


            if (servExpDate == null) {
                endpointType.setServiceExpirationDate(null);
            } else {
                Calendar caled = Calendar.getInstance();
                caled.setTime(servExpDate);
                int yeared = caled.get(Calendar.YEAR);
                int monthed = caled.get(Calendar.MONTH);
                int dayed = caled.get(Calendar.DAY_OF_MONTH);
                int houret = caled.get(Calendar.HOUR_OF_DAY);
                int minet = caled.get(Calendar.MINUTE);

                GregorianCalendar calendarED = new GregorianCalendar(yeared, monthed, dayed, houret, minet);
                try {
                    XMLGregorianCalendar xmlGregorianCalendarED = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendarED);
                    endpointType.setServiceExpirationDate(xmlGregorianCalendarED);//Set by user
                } catch (DatatypeConfigurationException e) {
                    LOGGER.error("DatatypeConfigurationException: '{}'", e.getMessage(), e);
                }

            }

            /*
             * certificate parse
             */
            if (certificateFile != null) {
                try {
                    String certPass = env.getProperty(type + ".certificate.password");
                    String certAlias = env.getProperty(type + ".certificate.alias");
                    String certificatePass = ConfigurationManagerFactory.getConfigurationManager().getProperty(certPass);
                    String certificateAlias = ConfigurationManagerFactory.getConfigurationManager().getProperty(certAlias);
                    LOGGER.info("Certificate Info: '{}', '{}', '{}', '{}'", certPass, certAlias, certificatePass, certificateAlias);
                    KeyStore ks = null;
                    try {
                        ks = KeyStore.getInstance(KeyStore.getDefaultType());
                        ks.load(certificateFile, null);
                    } catch (KeyStoreException ex) {
                        LOGGER.error("\n KeyStoreException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                    } catch (IOException ex) {
                        LOGGER.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                    } catch (NoSuchAlgorithmException ex) {
                        LOGGER.error("\n NoSuchAlgorithmException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                    } catch (CertificateException ex) {
                        LOGGER.error("\n CertificateException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                    }

                    if (ks != null && ks.isKeyEntry(certificateAlias)) {
                        char c[] = new char[certificatePass.length()];
                        certificatePass.getChars(0, c.length, c, 0);
                        Certificate certs[] = ks.getCertificateChain(certificateAlias);
                        if (LOGGER.isDebugEnabled()) {
                            for (Certificate certificate : certs) {
                                LOGGER.debug("Certificate Info: '{}' - '{}'", ((X509Certificate) certificate).getSerialNumber(), ((X509Certificate) certificate).getSubjectDN().getName());
                            }
                        }
                        if (certs[0] instanceof X509Certificate) {
                            X509Certificate x509 = (X509Certificate) certs[0];
                            endpointType.setCertificate(x509.getEncoded());
                            certificateSubjectName = x509.getIssuerX500Principal().getName() + " Serial Number #" + x509.getSerialNumber();
                        }
                    } else if (ks != null && ks.isCertificateEntry(certificateAlias)) {
                        Certificate c = ks.getCertificate(certificateAlias);
                        if (c instanceof X509Certificate) {
                            X509Certificate x509 = (X509Certificate) c;
                            endpointType.setCertificate(x509.getEncoded());
                            certificateSubjectName = x509.getIssuerX500Principal().getName() + " Serial Number #" + x509.getSerialNumber();
                        }
                    } else {
                        LOGGER.debug("\n ********** '{}' is unknown to this keystore", certificateAlias);
                    }

                } catch (KeyStoreException ex) {
                    LOGGER.error("\n KeyStoreException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (CertificateEncodingException ex) {
                    LOGGER.error("\n CertificateEncodingException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                }

            } else {
                byte[] by = "".getBytes();
                endpointType.setCertificate(by);
            }


            //   Endpoint Service Description, Technical ContactUrl and Technical InformationUrl definition
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

            //    Endpoint Extension file parse
            if (extension != null) {
                nullExtension = false;
                Document docOriginal = null;
                try {
                    String content = new Scanner(extension.getInputStream()).useDelimiter("\\Z").next();
                    LOGGER.info("XML Extension Content:\n'{}'", content);
                    //docOriginal = parseDocument(content);
                    docOriginal = parseStringToDocument(content);

                } catch (FileNotFoundException ex) {
                    nullExtension = true;
                    LOGGER.error("\n FileNotFoundException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (IOException ex) {
                    nullExtension = true;
                    LOGGER.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                }
//                catch (SAXException ex) {
//                    nullExtension = true;
//                    LOGGER.error("\n SAXException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
//                } catch (ParserConfigurationException ex) {
//                    nullExtension = true;
//                    LOGGER.error("\n ParserConfigurationException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
//                }

                if (nullExtension) {
                    //Does not add extension
                } else {
                    //Adding ISM Extension to SMP file.
                    docOriginal.getDocumentElement().normalize();
                    extensionType.setAny(docOriginal.getDocumentElement()); //Set by user
                    endpointType.getExtensions().add(extensionType);
                }
            } else {
                //Does not add extension
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
     * @param CC
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
    public void updateToXml(String type, String CC, String documentID, String documentIDScheme, String participantID,
                            String participantIDScheme, String processID, String processIDScheme, String transportProfile,
                            Boolean requiredBusinessLevelSig, String minimumAutenticationLevel, String endpointUri,
                            String servDescription, String tecContact, String tecInformation, Date servActDate,
                            Date servExpDate, byte[] certificate, FileInputStream certificateFile, Element extension,
                            MultipartFile extensionFile, String fileName, String certificateUID, String redirectHref) {

        LOGGER.debug("\n==== in updateToXml ====");

        ObjectFactory objectFactory = new ObjectFactory();
        ServiceMetadata serviceMetadata = objectFactory.createServiceMetadata();
        //XML file generated at path
        //generatedFile = new File("/" + fileName);

        generatedFile = new File(Constants.SMP_DIR_PATH + "/" + fileName);

        //Type of SMP File -> Redirect | Service Information
        if ("Redirect".equals(type)) {
      /*
       Redirect SMP Type
       */
            LOGGER.debug("\n******* Redirect ************");
            RedirectType redirectType = objectFactory.createRedirectType();

            redirectType.setCertificateUID(certificateUID);
            redirectType.setHref(redirectHref);

            serviceMetadata.setRedirect(redirectType);
        } else {
      /*
       ServiceInformation SMP Type
       */
            LOGGER.debug("\n******* ServiceInformation ************");
            DocumentIdentifier documentIdentifier = objectFactory.createDocumentIdentifier();
            EndpointType endpointType = objectFactory.createEndpointType();
            ExtensionType extensionType = objectFactory.createExtensionType();
            ParticipantIdentifierType participantIdentifierType = objectFactory.createParticipantIdentifierType();
            ProcessIdentifier processIdentifier = objectFactory.createProcessIdentifier();
            ProcessListType processListType = objectFactory.createProcessListType();
            ProcessType processType = objectFactory.createProcessType();
            ServiceEndpointList serviceEndpointList = objectFactory.createServiceEndpointList();
            ServiceInformationType serviceInformationType = objectFactory.createServiceInformationType();


      /*
        Fields fetched from file
      */
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
            Calendar calad = Calendar.getInstance();
            calad.setTime(servActDate);
            int yearad = calad.get(Calendar.YEAR);
            int monthad = calad.get(Calendar.MONTH);
            int dayad = calad.get(Calendar.DAY_OF_MONTH);
            int hourat = calad.get(Calendar.HOUR_OF_DAY);
            int minat = calad.get(Calendar.MINUTE);

            GregorianCalendar calendarAD = new GregorianCalendar(yearad, monthad, dayad, hourat, minat);
            try {
                XMLGregorianCalendar xmlGregorianCalendarAD = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendarAD);
                endpointType.setServiceActivationDate(xmlGregorianCalendarAD);//Set by user
            } catch (DatatypeConfigurationException e) {
                LOGGER.error("DatatypeConfigurationException: '{}'", e.getMessage(), e);
            }


            if (servExpDate == null) {
                endpointType.setServiceExpirationDate(null);
            } else {
                Calendar caled = Calendar.getInstance();
                caled.setTime(servExpDate);
                int yeared = caled.get(Calendar.YEAR);
                int monthed = caled.get(Calendar.MONTH);
                int dayed = caled.get(Calendar.DAY_OF_MONTH);
                int houret = caled.get(Calendar.HOUR_OF_DAY);
                int minet = caled.get(Calendar.MINUTE);

                GregorianCalendar calendarED = new GregorianCalendar(yeared, monthed, dayed, houret, minet);
                try {
                    XMLGregorianCalendar xmlGregorianCalendarED = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendarED);
                    endpointType.setServiceExpirationDate(xmlGregorianCalendarED);//Set by user
                } catch (DatatypeConfigurationException e) {
                    LOGGER.error("DatatypeConfigurationException: '{}'", e.getMessage(), e);
                }
            }

            /**
             * certificate parse
             */
            if (certificateFile != null) {
                try {
                    String certPass = env.getProperty(type + ".certificate.password");
                    String certAlias = env.getProperty(type + ".certificate.alias");
                    String certificatePass = ConfigurationManagerFactory.getConfigurationManager().getProperty(certPass);
                    String certificateAlias = ConfigurationManagerFactory.getConfigurationManager().getProperty(certAlias);

                    KeyStore ks = null;
                    try {
                        ks = KeyStore.getInstance(KeyStore.getDefaultType());
                        ks.load(certificateFile, null);
                    } catch (KeyStoreException ex) {
                        LOGGER.error("\n KeyStoreException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                    } catch (IOException ex) {
                        LOGGER.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                    } catch (NoSuchAlgorithmException ex) {
                        LOGGER.error("\n NoSuchAlgorithmException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                    } catch (CertificateException ex) {
                        LOGGER.error("\n CertificateException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                    }
                    if (ks != null && ks.isKeyEntry(certificateAlias)) {
                        char c[] = new char[certificatePass.length()];
                        certificatePass.getChars(0, c.length, c, 0);
                        Certificate certs[] = ks.getCertificateChain(certificateAlias);
                        if (certs[0] instanceof X509Certificate) {
                            X509Certificate x509 = (X509Certificate) certs[0];
                        }
                        if (certs[certs.length - 1] instanceof X509Certificate) {
                            X509Certificate x509 = (X509Certificate) certs[certs.length - 1];
                            endpointType.setCertificate(x509.getEncoded());
                            certificateSubjectName = x509.getIssuerX500Principal().getName() + " Serial Number #" + x509.getSerialNumber();
                        }
                    } else if (ks != null && ks.isCertificateEntry(certificateAlias)) {
                        Certificate c = ks.getCertificate(certificateAlias);
                        if (c instanceof X509Certificate) {
                            X509Certificate x509 = (X509Certificate) c;
                            endpointType.setCertificate(x509.getEncoded());
                            certificateSubjectName = x509.getIssuerX500Principal().getName() + " Serial Number #" + x509.getSerialNumber();
                        }
                    } else {
                        LOGGER.debug("\n ********** '{}' is unknown to this keystore", certificateAlias);
                    }
                } catch (KeyStoreException ex) {
                    LOGGER.error("\n KeyStoreException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (CertificateEncodingException ex) {
                    LOGGER.error("\n CertificateEncodingException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                }
            } else {
                byte[] by = "".getBytes();
                endpointType.setCertificate(by);
            }

      /*
       Endpoint Service Description, Technical ContactUrl and Technical InformationUrl definition
       */
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
                LOGGER.debug("\n********* CONVERTER EXTENSION FILE - " + extensionFile.getOriginalFilename());

                nullExtension = false;
                Document docOriginal = null;
                try {
                    String content = new Scanner(extensionFile.getInputStream()).useDelimiter("\\Z").next();

                    docOriginal = parseDocument(content);

                } catch (FileNotFoundException ex) {
                    nullExtension = true;
                    LOGGER.error("\n FileNotFoundException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (IOException ex) {
                    nullExtension = true;
                    LOGGER.error("\n IOException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (SAXException ex) {
                    nullExtension = true;
                    LOGGER.error("\n SAXException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
                } catch (ParserConfigurationException ex) {
                    nullExtension = true;
                    LOGGER.error("\n ParserConfigurationException - " + SimpleErrorHandler.printExceptionStackTrace(ex));
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

    /**
     * Parse the xml of the file to be updated
     *
     * @param fileUpdate
     * @return
     */
    public ServiceMetadata convertFromXml(MultipartFile fileUpdate) {
        LOGGER.debug("\n======= in convertFromXml ======= ");
        LOGGER.debug("\n************* fileUpdate - '{}'", fileUpdate.getOriginalFilename());

        isSignedServiceMetadata = false;
        ObjectFactory objectFactory = new ObjectFactory();
        ServiceMetadata serviceMetadata = objectFactory.createServiceMetadata();
        SignedServiceMetadata signedServiceMetadata = objectFactory.createSignedServiceMetadata();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SignedServiceMetadata.class, ServiceMetadata.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Object result = jaxbUnmarshaller.unmarshal(fileUpdate.getInputStream());

            if (result instanceof SignedServiceMetadata) {
                LOGGER.debug("\n******* CONVERTER SignedServiceMetadata SMPFILE");
                isSignedServiceMetadata = true;
                signedServiceMetadata = (SignedServiceMetadata) result;
                serviceMetadata = signedServiceMetadata.getServiceMetadata();
            } else if (result instanceof ServiceMetadata) {
                LOGGER.debug("\n******* CONVERTER ServiceMetadata SMPFILE");
                serviceMetadata = (ServiceMetadata) result;
            }
        } catch (JAXBException ex) {
            LOGGER.error("JAXBException - " + ex.getErrorCode(), ex);
        } catch (IOException ex) {
            LOGGER.error("IOException - " + ex.getLocalizedMessage(), ex);
        }

        return serviceMetadata;
    }

    /**
     * Defines the static fields of the SMP File
     */
    private void createStaticFields(String type, String issuanceType, String CC, DocumentIdentifier documentIdentifier,
                                    EndpointType endpointType, ParticipantIdentifierType participantIdentifierType,
                                    ProcessIdentifier processIdentifier, SMPFieldProperties businessLevelSignature,
                                    SMPFieldProperties minimumAuthLevel) {

    /*
     Document and Participant identifiers definition
     */
        participantIdentifierType.setScheme(env.getProperty(type + ".ParticipantIdentifier.Scheme")); ///in smpeditor.properties
    /*
    servidor -- :ncpa-idp
    client -- :ncp-idp
    */
   /* if(clientServer == 1){
      participantIdentifierType.setValue("urn:ehealth:" + CC + ":ncpa-idp"); //set by user (CC - country)
    } else if (clientServer == 2){
      participantIdentifierType.setValue("urn:ehealth:" + CC + ":ncp-idp"); //set by user (CC - country)
    }*/

        String participantID = env.getProperty(type + ".ParticipantIdentifier.value");//in smpeditor.properties
        participantID = String.format(participantID, CC); //Add country in place of %2s
        participantIdentifierType.setValue(participantID);

        documentIdentifier.setScheme(env.getProperty(type + ".DocumentIdentifier.Scheme"));//in smpeditor.properties

        //if (!issuanceType.equals("")) {
        if (StringUtils.isNotBlank(issuanceType)) {
            documentIdentifier.setValue(env.getProperty(type + ".DocumentIdentifier." + issuanceType));//in smpeditor.properties
        } else {
            documentIdentifier.setValue(env.getProperty(type + ".DocumentIdentifier"));//in smpeditor.properties
        }

    /*
     Process identifiers definition
     */
        processIdentifier.setScheme(env.getProperty(type + ".ProcessIdentifier.Scheme"));//in smpeditor.properties
        //if (!issuanceType.equals("")) {
        if (StringUtils.isNotBlank(issuanceType)) {
            processIdentifier.setValue(env.getProperty(type + ".ProcessIdentifier." + issuanceType));
        } else {
            String processID = env.getProperty(type + ".ProcessIdentifier");//in smpeditor.properties
            processID = String.format(processID, CC); //Add country if %2s is present in the string
            processIdentifier.setValue(processID);
        }

    /*
     Endpoint Transport Profile definition
     */
        endpointType.setTransportProfile(env.getProperty(type + ".transportProfile")); //in smpeditor.properties

    /*
     BusinessLevelSignature and MinimumAuthenticationLevel definition
     */
        if (businessLevelSignature.isEnable()) {
            Boolean requireBusinessLevelSignature = Boolean.parseBoolean(env.getProperty(type + ".RequireBusinessLevelSignature"));
            endpointType.setRequireBusinessLevelSignature(requireBusinessLevelSignature); //in smpeditor.properties
        }
        if (minimumAuthLevel.isEnable()) {
            endpointType.setMinimumAuthenticationLevel(env.getProperty(type + ".MinimumAuthenticationLevel")); //in smpeditor.properties
        }
    }

    /**
     * Marshal the data to a XML file
     */
    private void getXMLFile(ServiceMetadata serviceMetadata) {

        LOGGER.info("Generate XML SMP file: '{}'", serviceMetadata.getServiceInformation().getParticipantIdentifier().getValue());

        // Generates the final SMP XML file
        XMLStreamWriter xsw = null;

        try (FileOutputStream generatedFileOS = new FileOutputStream(generatedFile)) {

            xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(generatedFileOS, "UTF-8");
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

            JAXBContext jaxbContext = JAXBContext.newInstance(ServiceMetadata.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            //jaxbMarshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "false");
            jaxbMarshaller.marshal(serviceMetadata, generatedFileOS);
            jaxbMarshaller.marshal(serviceMetadata, stringWriter);

            LOGGER.info("JAXB Class: '{}'", jaxbContext.getClass());
            LOGGER.info("Service Metadata:\n{}", stringWriter.toString());

            generatedFileOS.flush();

        } catch (JAXBException ex) {
            LOGGER.error("\n JAXBException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (FileNotFoundException ex) {
            LOGGER.error("\n FileNotFoundException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (IOException ex) {
            LOGGER.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (XMLStreamException ex) {
            LOGGER.error("\n XMLStreamException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        } finally {
            if (xsw != null) {
                try {
                    xsw.close();
                } catch (XMLStreamException ex) {
                    LOGGER.error("\n XMLStreamException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                }
            }
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

    public Boolean getIsSignedServiceMetadata() {
        return isSignedServiceMetadata;
    }

    public void setIsSignedServiceMetadata(Boolean isSignedServiceMetadata) {
        this.isSignedServiceMetadata = isSignedServiceMetadata;
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
            LOGGER.error("SAXException: ", e);
        } catch (IOException e) {
            LOGGER.error("IOException: ", e);
        } catch (ParserConfigurationException e) {
            LOGGER.error("ParserConfigurationException: ", e);
        }
        return doc;
    }
}
