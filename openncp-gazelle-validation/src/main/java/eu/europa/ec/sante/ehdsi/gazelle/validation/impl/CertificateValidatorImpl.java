package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.europa.ec.sante.ehdsi.gazelle.validation.CertificateValidator;
import net.ihe.gazelle.jaxb.certificate.sante.Validate;
import net.ihe.gazelle.jaxb.certificate.sante.ValidateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

public class CertificateValidatorImpl extends AbstractValidator implements CertificateValidator {

    private final Logger logger = LoggerFactory.getLogger(CertificateValidatorImpl.class);

    CertificateValidatorImpl(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    @Override
    public boolean validate(String certificate, String type, boolean checkRevocation) {
        Validate request = new Validate();
        request.setCertificatesInPEMFormat(certificate);
        request.setType(type);
        request.setCheckRevocation(checkRevocation);

        try {
            ValidateResponse response = (ValidateResponse) webServiceTemplate.marshalSendAndReceive(request);
            return response.getDetailedResult() != null;
        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the CertificateValidator. Please check the stack trace for more details.", e);
            return false;
        }
    }
}
