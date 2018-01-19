package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.europa.ec.sante.ehdsi.gazelle.validation.AssertionValidator;
import net.ihe.gazelle.jaxb.assertion.sante.ValidateBase64Document;
import net.ihe.gazelle.jaxb.assertion.sante.ValidateBase64DocumentResponse;
import net.ihe.gazelle.jaxb.assertion.sante.ValidateDocument;
import net.ihe.gazelle.jaxb.assertion.sante.ValidateDocumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

public class AssertionValidatorImpl extends AbstractValidator implements AssertionValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionValidatorImpl.class);

    public AssertionValidatorImpl(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    @Override
    public boolean validateDocument(String document, String validator) {

        ValidateDocument request = new ValidateDocument();
        request.setDocument(document);
        request.setValidator(validator);

        try {
            ValidateDocumentResponse response = (ValidateDocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            LOGGER.info("Gazelle Result:\n{}", response.getDetailedResult());
            return StringUtils.hasText(response.getDetailedResult());
        } catch (WebServiceClientException e) {
            LOGGER.error("An error occurred during validation process of the AssertionValidator. Please check the stack trace for more details.", e);
            return false;
        }
    }

    @Override
    public boolean validateBase64Document(String base64Document, String validator) {

        ValidateBase64Document request = new ValidateBase64Document();
        request.setBase64Document(base64Document);
        request.setValidator(validator);

        try {
            ValidateBase64DocumentResponse response = (ValidateBase64DocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            LOGGER.info("Gazelle Result:\n{}", response.getDetailedResult());

            return StringUtils.hasText(response.getDetailedResult());
        } catch (WebServiceClientException e) {
            LOGGER.error("An error occurred during validation process of the AssertionValidator. Please check the stack trace for more details.", e);
            return false;
        }
    }
}
