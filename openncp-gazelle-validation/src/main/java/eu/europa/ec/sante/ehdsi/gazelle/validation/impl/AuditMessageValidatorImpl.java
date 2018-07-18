package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.europa.ec.sante.ehdsi.gazelle.validation.AuditMessageValidator;
import net.ihe.gazelle.jaxb.audit.sante.ValidateBase64Document;
import net.ihe.gazelle.jaxb.audit.sante.ValidateBase64DocumentResponse;
import net.ihe.gazelle.jaxb.audit.sante.ValidateDocument;
import net.ihe.gazelle.jaxb.audit.sante.ValidateDocumentResponse;
import net.ihe.gazelle.jaxb.result.sante.DetailedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class AuditMessageValidatorImpl extends AbstractValidator implements AuditMessageValidator {

    private final Logger logger = LoggerFactory.getLogger(AuditMessageValidatorImpl.class);

    AuditMessageValidatorImpl(WebServiceTemplate webServiceTemplate) {
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
//            ReportBuilder.build(validator, ObjectType.AUDIT.toString(), document,
//                    unmarshal(response.getDetailedResult()), response.getDetailedResult(), ncpSide);
//            return StringUtils.hasText(response.getDetailedResult());
        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the AuditMessageValidator. Please check the stack trace for more details.", e);
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
            //return StringUtils.hasText(response.getDetailedResult());
        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the AuditMessageValidator. Please check the stack trace for more details.", e);
            return "N/A";
        }
    }

    private DetailedResult unmarshal(String xmlDetails) {

        DetailedResult result = null;

        if (org.apache.commons.lang3.StringUtils.isBlank(xmlDetails)) {
            logger.error("The provided XML String object to unmarshall is empty.");
        } else {
            InputStream is = new ByteArrayInputStream(xmlDetails.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            try {

                JAXBContext jc = JAXBContext.newInstance(net.ihe.gazelle.jaxb.result.sante.DetailedResult.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                result = (DetailedResult) unmarshaller.unmarshal(is);

            } catch (JAXBException ex) {
                logger.error("JAXBException: '{}'", ex.getMessage(), ex);
            }
        }
        return result;
    }
}
