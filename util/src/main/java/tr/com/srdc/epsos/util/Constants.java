package tr.com.srdc.epsos.util;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.class);

    public static final String UUID_PREFIX = "urn:uuid:";
    public static final String OID_PREFIX = "urn:oid:";

    public static final String EP_CLASSCODE = "57833-6";
    public static final String PS_CLASSCODE = "60591-5";
    public static final String EDD_CLASSCODE = "DISCARD-60593-1";
    public static final String ED_CLASSCODE = "60593-1";
    public static final String ORCD_HOSPITAL_DISCHARGE_SUMMARY_CLASSCODE = "34105-7";
    public static final String ORCD_LABORATORY_REPORT_CLASSCODE = "11502-2";
    public static final String ORCD_DIAGNOSTIC_IMAGING_STUDY_CLASSCODE = "18748-4";
    public static final String ORCD_MEDICAL_IMAGES_CLASSCODE = "x-clinical-image";
    public static final String MRO_CLASSCODE = "56445-0";
    public static final String ORCD_LABORATORY_RESULTS_CLASSCODE = "11502-2";
    public static final String ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE= "34105-7";
    public static final String ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE= "18748-4";
    public static final String ORCD_MEDICAL_IMAGES_CLASSCODE= "x-clinical-image";
    public static final String CONSENT_CLASSCODE = "57016-8";
    public static final String HCER_CLASSCODE = "34133-9";
    public static final int FORMAT_XML = 1;
    public static final int FORMAT_PDF = 2;

    public static final String PatientIdentificationService = "PatientIdentificationService";
    public static final String PatientService = "PatientService";
    public static final String MroService = "MroService";
    public static final String ConsentService = "ConsentService";
    public static final String DispensationService = "DispensationService";
    public static final String OrderService = "OrderService";
    public static final String OrCDService = "OrCDService";

    public static final String PS_TITLE = "Patient Summary";
    public static final String MRO_TITLE = "MRO Summary";
    public static final String EP_TITLE = "ePrescription";
    public static final String ED_TITLE = "eDispensation";
    public static final String ORCD_HOSPITAL_DISCHARGE_SUMMARY_TITLE = "OrCD Hospital Discharge Summary";
    public static final String ORCD_LABORATORY_REPORT_TITLE = "OrCD Laboratory Report";
    public static final String ORCD_DIAGNOSTIC_IMAGING_STUDY_TITLE = "OrCD Diagnostic Imaging Study";
    public static final String ORCD_MEDICAL_IMAGES_TITLE = "OrCD Medical Image";
    public static final String CONSENT_TITLE = "Privacy Policy Acknowledgement Document";
    public static final String UNKNOWN_TITLE = "Unknown Document Type";

    public static final String CONSENT_PUT_SUFFIX = ".2.4.1.1";
    public static final String CONSENT_DISCARD_SUFFIX = ".2.4.1.2";

    public static final String NOT_USED_FIELD = "Not Used";
    public static final String MIME_TYPE = "text/xml";
    public static final String SERVER_IP;
    public static final String HOME_COMM_ID;
    public static final String COUNTRY_CODE;
    public static final String COUNTRY_NAME;
    public static final String COUNTRY_PRINCIPAL_SUBDIVISION;
    public static final String LANGUAGE_CODE;
    public static final String HR_ID_PREFIX = "SPProvidedID";
    /**
     * Path to the folder containing the configuration files.
     */
    public static final String EPSOS_PROPS_PATH;
    public static final String TRUSTSTORE_PATH;
    public static final String TRUSTSTORE_PASSWORD;
    public static final String SP_KEYSTORE_PATH;
    public static final String SP_KEYSTORE_PASSWORD;
    public static final String SP_PRIVATEKEY_ALIAS;
    public static final String SP_PRIVATEKEY_PASSWORD;
    public static final String SC_KEYSTORE_PATH;
    public static final String SC_KEYSTORE_PASSWORD;
    public static final String SC_PRIVATEKEY_ALIAS;
    public static final String SC_PRIVATEKEY_PASSWORD;
    public static final String NCP_SIG_KEYSTORE_PATH;
    public static final String NCP_SIG_KEYSTORE_PASSWORD;
    public static final String NCP_SIG_PRIVATEKEY_ALIAS;
    public static final String NCP_SIG_PRIVATEKEY_PASSWORD;

    /**
     * Name of the System Variable containing the path to the folder containing the configuration files.
     */
    private static final String PROPS_ENV_VAR = "EPSOS_PROPS_PATH";

    static {
        String epsosPath = System.getenv(PROPS_ENV_VAR);
        if (!epsosPath.endsWith(System.getProperty("file.separator"))) {
            epsosPath += System.getProperty("file.separator");
        }
        EPSOS_PROPS_PATH = epsosPath;
        LOGGER.info("OpenNCP Util Constants Initialization - EPSOS_PROPS_PATH: '{}'", EPSOS_PROPS_PATH);

        SERVER_IP = ConfigurationManagerFactory.getConfigurationManager().getProperty("SERVER_IP");

        HOME_COMM_ID = ConfigurationManagerFactory.getConfigurationManager().getProperty("HOME_COMM_ID");
        COUNTRY_CODE = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_CODE");
        COUNTRY_NAME = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_NAME");
        COUNTRY_PRINCIPAL_SUBDIVISION = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_PRINCIPAL_SUBDIVISION");
        LANGUAGE_CODE = ConfigurationManagerFactory.getConfigurationManager().getProperty("LANGUAGE_CODE");

        TRUSTSTORE_PATH = globalizePath(ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PATH"));
        TRUSTSTORE_PASSWORD = ConfigurationManagerFactory.getConfigurationManager().getProperty("TRUSTSTORE_PASSWORD");

        SP_KEYSTORE_PATH = globalizePath(ConfigurationManagerFactory.getConfigurationManager().getProperty("SP_KEYSTORE_PATH"));
        SP_KEYSTORE_PASSWORD = ConfigurationManagerFactory.getConfigurationManager().getProperty("SP_KEYSTORE_PASSWORD");
        SP_PRIVATEKEY_ALIAS = ConfigurationManagerFactory.getConfigurationManager().getProperty("SP_PRIVATEKEY_ALIAS");
        SP_PRIVATEKEY_PASSWORD = ConfigurationManagerFactory.getConfigurationManager().getProperty("SP_PRIVATEKEY_PASSWORD");

        SC_KEYSTORE_PATH = globalizePath(ConfigurationManagerFactory.getConfigurationManager().getProperty("SC_KEYSTORE_PATH"));
        SC_KEYSTORE_PASSWORD = ConfigurationManagerFactory.getConfigurationManager().getProperty("SC_KEYSTORE_PASSWORD");
        SC_PRIVATEKEY_ALIAS = ConfigurationManagerFactory.getConfigurationManager().getProperty("SC_PRIVATEKEY_ALIAS");
        SC_PRIVATEKEY_PASSWORD = ConfigurationManagerFactory.getConfigurationManager().getProperty("SC_PRIVATEKEY_PASSWORD");

        NCP_SIG_KEYSTORE_PATH = globalizePath(ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_KEYSTORE_PATH"));
        NCP_SIG_KEYSTORE_PASSWORD = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_KEYSTORE_PASSWORD");
        NCP_SIG_PRIVATEKEY_ALIAS = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_PRIVATEKEY_ALIAS");
        NCP_SIG_PRIVATEKEY_PASSWORD = ConfigurationManagerFactory.getConfigurationManager().getProperty("NCP_SIG_PRIVATEKEY_PASSWORD");
    }

    private Constants() {
    }

    /**
     * check whether the input path is global, modify accordingly (global paths can come within linux)
     *
     * @param path
     * @return
     */
    private static String globalizePath(String path) {

        if (path == null) {
            return null;
        }
        if (!(path.startsWith("/") || path.matches("[A-Z]:.*"))) {
            path = EPSOS_PROPS_PATH + path;
        }

        return path;
    }
}
