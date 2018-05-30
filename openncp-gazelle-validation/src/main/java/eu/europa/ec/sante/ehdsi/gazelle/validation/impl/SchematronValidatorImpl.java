package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.SchematronValidator;
import eu.europa.ec.sante.ehdsi.gazelle.validation.reporting.ReportBuilder;
import net.ihe.gazelle.jaxb.result.sante.DetailedResult;
import net.ihe.gazelle.jaxb.schematron.sante.ValidateObject;
import net.ihe.gazelle.jaxb.schematron.sante.ValidateObjectResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SchematronValidatorImpl extends AbstractValidator implements SchematronValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchematronValidatorImpl.class);
    private static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("LOGGER_CLINICAL");

    public SchematronValidatorImpl(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    private static DetailedResult unmarshal(String xmlDetails) {

        DetailedResult result = null;

        if (StringUtils.isBlank(xmlDetails)) {
            LOGGER.error("The provided XML String object to unmarshall is empty.");
        } else {
            InputStream is = new ByteArrayInputStream(xmlDetails.getBytes(StandardCharsets.UTF_8));

            try {

                JAXBContext jc = JAXBContext.newInstance(DetailedResult.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                result = (DetailedResult) unmarshaller.unmarshal(is);

            } catch (JAXBException ex) {
                LOGGER.error("JAXBException: '{}'", ex.getMessage(), ex);
            }
        }
        return result;
    }

    public boolean validateObject(String base64Object, String xmlReferencedStandard, String xmlMetadata, String objectType, NcpSide ncpSide) {

        ValidateObject request = new ValidateObject();
        request.setBase64ObjectToValidate(base64Object);
        request.setXmlReferencedStandard(xmlReferencedStandard);
        request.setXmlMetadata(xmlMetadata);

        try {

            ValidateObjectResponse response = (ValidateObjectResponse) webServiceTemplate.marshalSendAndReceive(request);
            if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
                LOGGER_CLINICAL.info("Response:\n'{}'", response.getValidationResult());
            }
            DetailedResult detailedResult = unmarshal(response.getValidationResult());
            ReportBuilder.build(xmlReferencedStandard, objectType, base64Object, detailedResult, response.getValidationResult(), ncpSide);
            return StringUtils.isEmpty(response.getValidationResult());

        } catch (WebServiceClientException e) {
            LOGGER.error("An error occurred during validation process of the SchematronValidator. Please check the stack trace for more details.", e);
            return false;
        }
    }

    public boolean validateObject(String base64Object, String xmlReferencedStandard, String xmlMetadata) {

        //return validateObject(base64Object, xmlReferencedStandard, xmlMetadata, NcpSide.NCP_B);
        return false;
    }
}
