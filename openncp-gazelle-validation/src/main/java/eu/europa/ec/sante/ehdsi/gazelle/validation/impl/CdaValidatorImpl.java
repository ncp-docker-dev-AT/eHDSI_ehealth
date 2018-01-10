package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.epsos.validation.datamodel.cda.CdaModel;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.CdaValidator;
import eu.europa.ec.sante.ehdsi.gazelle.validation.reporting.ReportBuilder;
import net.ihe.gazelle.jaxb.cda.sante.ValidateBase64Document;
import net.ihe.gazelle.jaxb.cda.sante.ValidateBase64DocumentResponse;
import net.ihe.gazelle.jaxb.cda.sante.ValidateDocument;
import net.ihe.gazelle.jaxb.cda.sante.ValidateDocumentResponse;
import net.ihe.gazelle.jaxb.result.sante.DetailedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringSource;

import java.io.IOException;

public class CdaValidatorImpl extends AbstractValidator implements CdaValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdaValidatorImpl.class);

    public CdaValidatorImpl(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    @Override
    public boolean validateDocument(String document, String validator, NcpSide ncpSide) {

        ValidateDocument request = new ValidateDocument();
        request.setDocument(document);
        request.setValidator(validator);

        try {
            ValidateDocumentResponse response = (ValidateDocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            DetailedResult detailedResult = (DetailedResult) webServiceTemplate.getUnmarshaller().unmarshal(new StringSource(response.getDetailedResult()));

            LOGGER.info("Gazelle Report:\n{}", detailedResult.getValidationResultsOverview().getValidationServiceName());
            LOGGER.info("Gazelle Report:\n{}", detailedResult.getValidationResultsOverview().getValidationTestResult());
            LOGGER.info("Gazelle Report:\n{}", response.getDetailedResult());
            LOGGER.info("Gazelle Result:\n{}", detailedResult.getMDAValidation().getResult());

            //return ReportBuilder.build(validator, CdaModel.checkModel(validator).getObjectType().toString(), document, detailedResult, response.getDetailedResult(), ncpSide);
            return true;
        } catch (WebServiceClientException | IOException e) {
            LOGGER.error("An error occurred during validation process of the CdaValidator. Please check the stack trace for more details.", e);
            return ReportBuilder.build(validator, CdaModel.checkModel(validator).getObjectType().toString(), document, null, null, ncpSide);
        }
    }

    @Override
    public boolean validateBase64Document(String base64Document, String validator, NcpSide ncpSide) {
        ValidateBase64Document request = new ValidateBase64Document();
        request.setBase64Document(base64Document);
        request.setValidator(validator);

        try {
            ValidateBase64DocumentResponse response = (ValidateBase64DocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            DetailedResult detailedResult = (DetailedResult) webServiceTemplate.getUnmarshaller().unmarshal(new StringSource(response.getDetailedResult()));
            return ReportBuilder.build(validator, CdaModel.checkModel(validator).getObjectType().toString(), base64Document, detailedResult, response.getDetailedResult(), ncpSide);
        } catch (WebServiceClientException | IOException e) {
            LOGGER.error("An error occurred during validation process of the CdaValidator. Please check the stack trace for more details.", e);
            return ReportBuilder.build(validator, CdaModel.checkModel(validator).getObjectType().toString(), base64Document, null, null, ncpSide);
        }
    }
}
