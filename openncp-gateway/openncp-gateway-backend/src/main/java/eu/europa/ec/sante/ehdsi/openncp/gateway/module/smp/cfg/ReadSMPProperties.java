package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.cfg;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.SMPFieldProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.SMPFields;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.SMPType;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.SimpleErrorHandler;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class ReadSMPProperties {

    org.slf4j.Logger logger = LoggerFactory.getLogger(ReadSMPProperties.class);
    @Autowired
    private Environment env;

    public SMPFields readProperties(SMPType smpType) {


       SMPFields smpFields = new SMPFields();

        smpFields.setUri(create(smpType,  ".uri" ));
        smpFields.setIssuanceType(create(smpType,  ".issuanceType" ));
        smpFields.setServiceActivationDate(create(smpType,  ".serviceActivationDate" ));
        smpFields.setServiceExpirationDate(create(smpType,  ".serviceExpirationDate" ));
        smpFields.setCertificate(create(smpType,  ".certificate" ));
        smpFields.setServiceDescription(create(smpType,  ".serviceDescription" ));
        smpFields.setTechnicalContactUrl(create(smpType,  ".technicalContactUrl" ));
        smpFields.setTechnicalInformationUrl(create(smpType,  ".technicalInformationUrl" ));
        smpFields.setExtension(create(smpType,  ".extension" ));
        smpFields.setRequireBusinessLevelSignature(create(smpType,  ".requireBusinessLevelSignature" ));
        smpFields.setMinimumAuthLevel(create(smpType,  ".minimumAuthLevel" ));
        smpFields.setRedirectHref(create(smpType,  ".redirectHref" ));
        smpFields.setCertificateUID(create(smpType,  ".certificateUID" ));

        return smpFields;
    }

    private SMPFieldProperties create(SMPType smpType, String propertiesExtension){
        SMPFieldProperties smpFieldProperties = new SMPFieldProperties();
        smpFieldProperties.setName(env.getProperty("label.SmpEditor.form"+propertiesExtension));
        boolean enable = env.getProperty(smpType.name() + propertiesExtension + ".enable") != null
                && Boolean.parseBoolean(env.getProperty(smpType.name() + propertiesExtension + ".enable"));
        smpFieldProperties.setEnable(enable);
        boolean mandatory = env.getProperty(smpType.name() + propertiesExtension + ".mandatory") != null
                && Boolean.parseBoolean(env.getProperty(smpType.name() + propertiesExtension + ".mandatory"));
        smpFieldProperties.setMandatory(mandatory);
        boolean display = env.getProperty(smpType.name() + propertiesExtension + ".display") != null
                && Boolean.parseBoolean(env.getProperty(smpType.name() + propertiesExtension + ".display"));
        smpFieldProperties.setDisplay(display);
        return smpFieldProperties;
    }

    public Map<String, String>  readPropertiesFile() {

        logger.debug("\n *************** in readPropertiesFile **************");

        Properties properties = new Properties();
        String filename = "/smpeditor.properties";

        Resource resource = new ClassPathResource(filename);

        InputStream input = null;
        try {
            input = resource.getInputStream();
        } catch (IOException ex) {
            logger.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        if (input == null) {
            logger.error("\n Unable to find - '{}'", filename);
        }

        try {
            properties.load(input);
        } catch (IOException ex) {
            logger.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        }

        Enumeration<?> keys = properties.propertyNames();
        HashMap<String, String> propertiesMap = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = properties.getProperty(key);
            propertiesMap.put(value, key);
        }

        return propertiesMap;
    }
}
