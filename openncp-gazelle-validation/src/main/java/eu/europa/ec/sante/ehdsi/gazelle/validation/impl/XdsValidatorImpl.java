package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.xd.XdModel;
import eu.europa.ec.sante.ehdsi.gazelle.validation.XdsValidator;
import eu.europa.ec.sante.ehdsi.gazelle.validation.reporting.ReportBuilder;
import net.ihe.gazelle.jaxb.result.sante.DetailedResult;
import net.ihe.gazelle.jaxb.xds.sante.ValidateBase64Document;
import net.ihe.gazelle.jaxb.xds.sante.ValidateBase64DocumentResponse;
import net.ihe.gazelle.jaxb.xds.sante.ValidateDocument;
import net.ihe.gazelle.jaxb.xds.sante.ValidateDocumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class XdsValidatorImpl extends AbstractValidator implements XdsValidator {

    private static final String MSG_ERROR_XDR_VALIDATION = "An error occurred during validation process of the XdsValidator. Please check the stack trace for more details.";
    private final Logger logger = LoggerFactory.getLogger(XdsValidatorImpl.class);

    XdsValidatorImpl(WebServiceTemplate webServiceTemplate) {
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
            logger.error(MSG_ERROR_XDR_VALIDATION, e);
            return false;
        }
    }

    public boolean validateDocument(String document, String validator, NcpSide ncpSide) {

        logger.info("validateDocument: '{}', '{}'\n'{}", validator, ncpSide.getName(), document);

        ValidateDocument request = new ValidateDocument();
        request.setDocument(document);
        request.setValidator(validator);

        try {
            ValidateDocumentResponse response = (ValidateDocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            if (!org.apache.commons.lang3.StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
                logger.info("Response:\n'{}'", response.getDetailedResult());
            }

            DetailedResult detailedResult = unmarshal(response.getDetailedResult());
            if (detailedResult != null) {
                logger.info("DetailedResult: '{}'", org.apache.commons.lang3.StringUtils.isNotBlank(detailedResult.getValidationResultsOverview().getValidationServiceName()) ? detailedResult.getValidationResultsOverview().getValidationServiceName() : "Detailed Result Empty");
            }
            String model = XdModel.checkModel(validator).getObjectType().toString();
            logger.info("XDS Object Type: '{}'", model);
            ReportBuilder.build(validator, model, document, detailedResult, response.getDetailedResult(), ncpSide);
            return StringUtils.hasText(response.getDetailedResult());
        } catch (WebServiceClientException e) {
            logger.error(MSG_ERROR_XDR_VALIDATION, e);
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
            logger.error(MSG_ERROR_XDR_VALIDATION, e);
            return false;
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
