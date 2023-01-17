package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.HL7v3Validator;
import net.ihe.gazelle.jaxb.hl7v3.sante.ValidateBase64Document;
import net.ihe.gazelle.jaxb.hl7v3.sante.ValidateBase64DocumentResponse;
import net.ihe.gazelle.jaxb.hl7v3.sante.ValidateDocument;
import net.ihe.gazelle.jaxb.hl7v3.sante.ValidateDocumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

public class HL7v3ValidatorImpl extends AbstractValidator implements HL7v3Validator {

    private final Logger logger = LoggerFactory.getLogger(HL7v3ValidatorImpl.class);

    public HL7v3ValidatorImpl(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    @Override
    public String validateDocument(String document, String validator, NcpSide ncpSide) {
        logger.info("HL7v3Validator validateDocument('{}', '{}')", validator, ncpSide.getName());
        ValidateDocument request = new ValidateDocument();
        request.setDocument(document);
        request.setValidator(validator);

        try {
            ValidateDocumentResponse response = (ValidateDocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            return response.getDetailedResult();

        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the CdaValidator. Please check the stack trace for more details.", e);
            return "N/A";
        }
    }

    @Override
    public String validateBase64Document(String base64Document, String validator, NcpSide ncpSide) {
        ValidateBase64Document request = new ValidateBase64Document();
        request.setBase64Document(base64Document);
        request.setValidator(validator);

        try {
            ValidateBase64DocumentResponse response = (ValidateBase64DocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            return response.getDetailedResult();

        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the CdaValidator. Please check the stack trace for more details.", e);
            return "N/A";
        }
    }
}
