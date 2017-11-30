package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.europa.ec.sante.ehdsi.gazelle.validation.CdaValidator;
import net.ihe.gazelle.jaxb.cda.ValidateBase64Document;
import net.ihe.gazelle.jaxb.cda.ValidateBase64DocumentResponse;
import net.ihe.gazelle.jaxb.cda.ValidateDocument;
import net.ihe.gazelle.jaxb.cda.ValidateDocumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

public class CdaValidatorImpl extends AbstractValidator implements CdaValidator {

    private final Logger logger = LoggerFactory.getLogger(CdaValidatorImpl.class);

    CdaValidatorImpl(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    @Override
    public boolean validateDocument(String document, String validator) {
        ValidateDocument request = new ValidateDocument();
        request.setDocument(document);
        request.setValidator(validator);

        try {
            ValidateDocumentResponse response = (ValidateDocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            return StringUtils.hasText(response.getDetailedResult());
        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the CdaValidator. Please check the stack trace for more details.", e);
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
            return StringUtils.hasText(response.getDetailedResult());
        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the CdaValidator. Please check the stack trace for more details.", e);
            return false;
        }
    }
}
