package com.gnomon.epsos.servlet;

import com.gnomon.epsos.model.EpsosDocument;
import com.gnomon.epsos.model.cda.Utils;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import epsos.ccd.gnomon.xslt.EpsosXSLTransformer;
import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import epsos.openncp.protocolterminator.clientconnector.DocumentId;
import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.GenericDocumentCode;
import eu.epsos.util.IheConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import tr.com.srdc.epsos.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.OutputStream;

public class CCDServlet extends HttpServlet {

    private static final long serialVersionUID = 1974995783413475611L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CCDServlet.class);

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        String exportType = ParamUtil.getString(req, "exportType");

        String cda;
        LOGGER.info("getting html document");
        try (OutputStream outputStream = res.getOutputStream()) {
            String uuid = req.getParameter("uuid");
            String repositoryId = req.getParameter("repositoryid");
            String hcid = req.getParameter("hcid");

            LOGGER.debug("Retrieving XML document");
            LOGGER.debug("UUID: '{}'", uuid);
            LOGGER.debug("repositoryId: '{}'", repositoryId);
            LOGGER.debug("hcid: '{}", hcid);

            EpsosDocument selectedEpsosDocument = new EpsosDocument();
            String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);
            ClientConnectorConsumer clientConectorConsumer = new ClientConnectorConsumer(serviceUrl);

            HttpSession session = req.getSession();

            Assertion hcpAssertion = (Assertion) session.getAttribute("hcpAssertion");
            Assertion trcAssertion = (Assertion) session.getAttribute("trcAssertion");
            String selectedCountry = (String) session.getAttribute("selectedCountry");
            User user = (User) session.getAttribute("user");

            DocumentId documentId = DocumentId.Factory.newInstance();
            documentId.setDocumentUniqueId(uuid);
            documentId.setRepositoryUniqueId(repositoryId);

            GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();
            String docType = req.getParameter("docType");
            LOGGER.debug("Document : '{}' is '{}'", uuid, docType);
            if (StringUtils.equals(docType, "ps")) {
                classCode.setNodeRepresentation(Constants.PS_CLASSCODE);
                classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                classCode.setValue(Constants.PS_TITLE);
            }

            LOGGER.debug("selectedCountry: '{}'", selectedCountry);
            LOGGER.debug("classCode: '{}'", classCode);

            String lang;
            String ltrlang = ParamUtil.getString(req, "lang");
            if (Validator.isNull(ltrlang)) {
                lang = user.getLanguageId();
            } else {
                lang = ltrlang;
            }

            String lang1 = lang.replace("_", "-");
            lang1 = lang1.replace("en-US", "en");

            LOGGER.info("Portal language is : '{}'-'{}'", lang, lang1);

            EpsosDocument1 eps = clientConectorConsumer.retrieveDocument(hcpAssertion, trcAssertion, selectedCountry,
                    documentId, hcid, classCode, lang1);

            selectedEpsosDocument.setAuthor(eps.getAuthor() + "");
            selectedEpsosDocument.setCreationDate(eps.getCreationDate());
            selectedEpsosDocument.setDescription(eps.getDescription());
            selectedEpsosDocument.setTitle(eps.getTitle());

            String xmlFile = new String(eps.getBase64Binary(), "UTF-8");
            LOGGER.info("#### CDA XML Start: \n '{}' \n #### CDA XML End", xmlFile);

            boolean isCDA;
            Document doc1 = Utils.createDomFromString(xmlFile);
            isCDA = EpsosHelperService.isCDA(doc1);
            LOGGER.info("### Document created is CDA: '{}'", isCDA);

            // Transform to CCD
            String mayoTransformed = "";
            EpsosXSLTransformer xlsClass = new EpsosXSLTransformer();

            if (isCDA) {
                LOGGER.info("########### Styling the document that is CDA: '{}' using standard xsl", true);
                cda = xlsClass.transformUsingStandardCDAXsl(mayoTransformed);
            } else {
                LOGGER.info("########### Styling the document that is CDA: '{}' using EPSOS xsl", false);
                mayoTransformed = xmlFile;
                cda = xlsClass.transform(mayoTransformed, lang1, "");
            }

            // Visualize as HTML using standard stylesheet
            LOGGER.info("EXPORT TYPE: '{}'", exportType);
            byte[] output;
            if (StringUtils.equals(exportType, "xml")) {
                output = mayoTransformed.getBytes();
            } else {
                output = cda.getBytes();
            }

            if (StringUtils.equals(exportType, "xml")) {
                res.setHeader("Content-Disposition", "attachment; filename=cda.xml");
                res.setContentType("text/xml");
            } else {
                res.setContentType("text/html");
            }

            res.setHeader("Cache-Control", "no-cache");
            res.setDateHeader("Expires", 0);
            res.setHeader("Pragma", "No-cache");

            outputStream.write(output);
            outputStream.flush();
            outputStream.close();

        } catch (IOException ex) {
            LOGGER.error(ExceptionUtils.getStackTrace(ex));
            res.setContentType("text/html");
            res.setHeader("Cache-Control", "no-cache");
            res.setDateHeader("Expires", 0);
            res.setHeader("Pragma", "No-cache");

            try (OutputStream outputStream = res.getOutputStream()) {
                outputStream.write(ex.getMessage().getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        } catch (XPathExpressionException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
