package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.europa.ec.sante.ehdsi.gazelle.validation.SchematronValidator;
import net.ihe.gazelle.jaxb.schematron.sante.ValidateBase64Document;
import net.ihe.gazelle.jaxb.schematron.sante.ValidateBase64DocumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

public class SchematronValidatorImpl extends AbstractValidator implements SchematronValidator {

    private final Logger logger = LoggerFactory.getLogger(SchematronValidatorImpl.class);

    public SchematronValidatorImpl(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    public String validateObject(String base64Object, String xmlReferencedStandard, String xmlMetadata) {

        logger.info("Schematron Validator: validateObject('{}','{}')", xmlReferencedStandard, xmlMetadata);

        ValidateBase64Document request = new ValidateBase64Document();
        request.setBase64Document(base64Object);
        request.setValidator(xmlReferencedStandard);
        request.setValidator(xmlMetadata);

        try {
            ValidateBase64DocumentResponse response = (ValidateBase64DocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            return response.getReturn();

        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the SchematronValidator. " +
                    "Please check the stack trace for more details.", e);
            //throw new GazelleValidationException(e.getMessage(), e);
            return "";
        }
    }
}
