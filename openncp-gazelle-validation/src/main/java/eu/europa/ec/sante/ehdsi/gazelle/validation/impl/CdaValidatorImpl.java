package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import eu.epsos.validation.datamodel.cda.CdaModel;
import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.common.ObjectType;
import eu.europa.ec.sante.ehdsi.gazelle.validation.CdaValidator;
import eu.europa.ec.sante.ehdsi.gazelle.validation.reporting.ReportBuilder;
import net.ihe.gazelle.jaxb.cda.sante.ValidateBase64Document;
import net.ihe.gazelle.jaxb.cda.sante.ValidateBase64DocumentResponse;
import net.ihe.gazelle.jaxb.cda.sante.ValidateDocument;
import net.ihe.gazelle.jaxb.cda.sante.ValidateDocumentResponse;
import net.ihe.gazelle.jaxb.result.sante.DetailedResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CdaValidatorImpl extends AbstractValidator implements CdaValidator {

    private static final Logger logger = LoggerFactory.getLogger(CdaValidatorImpl.class);
    private static Logger logger_clinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    public CdaValidatorImpl(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    private static DetailedResult unmarshal(String xmlDetails) {

        DetailedResult result = null;

        if (StringUtils.isBlank(xmlDetails)) {
            logger.error("The provided XML String object to unmarshall is empty.");
        } else {
            InputStream is = new ByteArrayInputStream(xmlDetails.getBytes(StandardCharsets.UTF_8));

            try {

                JAXBContext jc = JAXBContext.newInstance(DetailedResult.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                result = (DetailedResult) unmarshaller.unmarshal(is);

            } catch (JAXBException ex) {
                logger.error("JAXBException: '{}'", ex.getMessage(), ex);
            }
        }
        return result;
    }

    @Override
    public boolean validateDocument(String document, String validator, NcpSide ncpSide) {

        ValidateDocument request = new ValidateDocument();
        request.setDocument(document);
        request.setValidator(validator);

        try {

            ValidateDocumentResponse response = (ValidateDocumentResponse) webServiceTemplate.marshalSendAndReceive(request);
            //DetailedResult detailedResult = (DetailedResult) webServiceTemplate.getUnmarshaller().unmarshal(new StringSource(response.getDetailedResult()));
            //DetailedResult detailedResult = unmarshal(response.getDetailedResult());
            //ReportBuilder.build(validator, ObjectType.CDA.toString(), document, detailedResult, response.getDetailedResult(), ncpSide);
            //  return ReportBuilder.build(validator, CdaModel.checkModel(validator).getObjectType().toString(), document, detailedResult, response.getDetailedResult(), ncpSide);
            if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
                logger_clinical.info("Response:\n'{}'", response.getDetailedResult());
            }
            DetailedResult detailedResult = unmarshal(response.getDetailedResult());
            ReportBuilder.build(validator, ObjectType.CDA.toString(), document, detailedResult, response.getDetailedResult(), ncpSide);
            return StringUtils.isEmpty(response.getDetailedResult());
            //  return true;
        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the CdaValidator. Please check the stack trace for more details.", e);
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
            //DetailedResult detailedResult = (DetailedResult) webServiceTemplate.getUnmarshaller().unmarshal(new StringSource(response.getDetailedResult()));
            DetailedResult detailedResult = unmarshal(response.getDetailedResult());
            return ReportBuilder.build(validator, CdaModel.checkModel(validator).getObjectType().toString(), base64Document, detailedResult, response.getDetailedResult(), ncpSide);
        } catch (WebServiceClientException e) {
            logger.error("An error occurred during validation process of the CdaValidator. Please check the stack trace for more details.", e);
            return ReportBuilder.build(validator, CdaModel.checkModel(validator).getObjectType().toString(), base64Document, null, null, ncpSide);
        }
    }
}
