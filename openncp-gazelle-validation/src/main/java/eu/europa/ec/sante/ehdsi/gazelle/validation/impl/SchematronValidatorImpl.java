package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.europa.ec.sante.ehdsi.gazelle.validation.SchematronValidator;
import net.ihe.gazelle.jaxb.schematron.sante.ValidateObject;
import net.ihe.gazelle.jaxb.schematron.sante.ValidateObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

public class SchematronValidatorImpl extends AbstractValidator implements SchematronValidator {

    private final Logger logger = LoggerFactory.getLogger(SchematronValidatorImpl.class);

    public SchematronValidatorImpl(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    @Override
    public boolean validateObject(String base64Object, String xmlReferencedStandard, String xmlMetadata) {

        ValidateObject request = new ValidateObject();
        request.setBase64ObjectToValidate(base64Object);
        request.setXmlReferencedStandard(xmlReferencedStandard);
        request.setXmlMetadata(xmlMetadata);

        try {
            ValidateObjectResponse response = (ValidateObjectResponse) webServiceTemplate.marshalSendAndReceive(request);
            return StringUtils.isEmpty(response.getValidationResult());
        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the SchematronValidator. Please check the stack trace for more details.", e);
            return false;
        }
    }
}
