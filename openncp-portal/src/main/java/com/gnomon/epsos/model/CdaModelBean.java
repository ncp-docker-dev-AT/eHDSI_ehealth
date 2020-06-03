package com.gnomon.epsos.model;

import com.gnomon.LiferayUtils;
import com.gnomon.epsos.service.EpsosHelperService;
import epsos.ccd.gnomon.xslt.CdaXSLTransformer;
import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import epsos.openncp.protocolterminator.clientconnector.DocumentId;
import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.GenericDocumentCode;
import eu.epsos.util.IheConstants;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@ManagedBean
@RequestScoped
public class CdaModelBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(CdaModelBean.class);
    private String cda;

    public CdaModelBean() {
        logger.info("CDA MODEL BEAN requesting");
        cda = null;
    }

    public String getCda() {
        return cda;
    }

    public void setCda(String cda) {
        this.cda = cda;
    }

    public String getClinicalDocument(PatientDocument document) {

        try {
            EpsosDocument selectedEpsosDocument = new EpsosDocument();
            String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);
            ClientConnectorConsumer clientConectorConsumer = new ClientConnectorConsumer(serviceUrl);

            Assertion hcpAssertion = (Assertion) LiferayUtils.getFromSession("hcpAssertion");
            Assertion trcAssertion = (Assertion) LiferayUtils.getFromSession("trcAssertion");
            String selectedCountry = (String) LiferayUtils.getFromSession("selectedCountry");

            String uuid = document.getUuid();
            String repositoryId = document.getRepositoryId();
            String hcid = document.getHcid();

            logger.info("Retrieving CDA document");
            logger.info("UUID: '{}'", uuid);
            logger.info("repositoryId: '{}'", repositoryId);
            logger.info("hcid: '{}'", hcid);
            DocumentId documentId = DocumentId.Factory.newInstance();
            documentId.setDocumentUniqueId(uuid);
            documentId.setRepositoryUniqueId(repositoryId);

            GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();
            String docType = document.getDocType();
            if (docType.equals("ep")) {
                classCode.setNodeRepresentation(Constants.EP_CLASSCODE);
                classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                classCode.setValue(Constants.EP_TITLE);
            }
            if (docType.equals("ps")) {
                classCode.setNodeRepresentation(Constants.PS_CLASSCODE);
                classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                classCode.setValue(Constants.PS_TITLE);
            }

            EpsosDocument1 eps = clientConectorConsumer.retrieveDocument(hcpAssertion, trcAssertion, selectedCountry,
                    documentId, hcid, classCode);
            selectedEpsosDocument.setAuthor(eps.getAuthor() + "");
            selectedEpsosDocument.setCreationDate(eps.getCreationDate());
            selectedEpsosDocument.setDescription(eps.getDescription());
            selectedEpsosDocument.setTitle(eps.getTitle());

            String xmlfile = new String(eps.getBase64Binary(), StandardCharsets.UTF_8);
            String lang1 = LiferayUtils.getPortalLanguage().replace("_", "-");
            String actionURL = "someurl";
            cda = CdaXSLTransformer.getInstance().transform(xmlfile, lang1, actionURL);

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            logger.error(ExceptionUtils.getStackTrace(ex));
        }

        return "cda";
    }
}
