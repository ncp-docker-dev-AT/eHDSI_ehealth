package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.europa.ec.sante.ehdsi.gazelle.validation.XdsValidator;
import net.ihe.gazelle.jaxb.xds.sante.ValidateBase64Document;
import net.ihe.gazelle.jaxb.xds.sante.ValidateBase64DocumentResponse;
import net.ihe.gazelle.jaxb.xds.sante.ValidateDocument;
import net.ihe.gazelle.jaxb.xds.sante.ValidateDocumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

public class XdsValidatorImpl extends AbstractValidator implements XdsValidator {

    private static final String MSG_ERROR_XDR_VALIDATION = "An error occurred during validation process of the XdsValidator. Please check the stack trace for more details.";
    private final Logger logger = LoggerFactory.getLogger(XdsValidatorImpl.class);

    XdsValidatorImpl(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    @Override
    public String validateDocument(String document, String validator) {

        ValidateDocument request = new ValidateDocument();
        request.setDocument(document);
        request.setValidator(validator);

        try {
            ValidateDocumentResponse response = (ValidateDocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            return response.getDetailedResult();

        } catch (WebServiceClientException e) {
            logger.error(MSG_ERROR_XDR_VALIDATION, e);
            return "N/A";
        }
    }


    @Override
    public String validateBase64Document(String base64Document, String validator) {

        ValidateBase64Document request = new ValidateBase64Document();
        request.setBase64Document(base64Document);
        request.setValidator(validator);

        try {
            ValidateBase64DocumentResponse response = (ValidateBase64DocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            return response.getDetailedResult();

        } catch (WebServiceClientException e) {
            logger.error(MSG_ERROR_XDR_VALIDATION, e);
            return "N/A";
        }
    }
}
