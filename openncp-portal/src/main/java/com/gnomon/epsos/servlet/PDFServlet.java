package com.gnomon.epsos.servlet;

import com.gnomon.epsos.model.EpsosDocument;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import epsos.openncp.protocolterminator.clientconnector.DocumentId;
import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.GenericDocumentCode;
import eu.epsos.util.IheConstants;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

public class PDFServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(PDFServlet.class);

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) {

        LOGGER.info("Getting PDF document");
        byte[] pdf;

        try {
            String uuid = req.getParameter("uuid");
            String repositoryId = req.getParameter("repositoryid");
            String hcid = req.getParameter("hcid");

            LOGGER.debug("Retrieving PDF document");
            LOGGER.debug("uuid: '{}'", uuid);
            LOGGER.debug("repositoryId: '{}'", repositoryId);
            LOGGER.debug("hcid: '{}'", hcid);

            EpsosDocument selectedEpsosDocument = new EpsosDocument();
            String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);

            ClientConnectorConsumer clientConnectorConsumer = new ClientConnectorConsumer(serviceUrl);

            HttpSession session = req.getSession();

            Assertion hcpAssertion = (Assertion) session.getAttribute("hcpAssertion");
            Assertion trcAssertion = (Assertion) session.getAttribute("trcAssertion");
            String selectedCountry = (String) session.getAttribute("selectedCountry");

            DocumentId documentId = DocumentId.Factory.newInstance();
            documentId.setDocumentUniqueId(uuid);
            documentId.setRepositoryUniqueId(repositoryId);

            GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();
            String docType = req.getParameter("docType");

            if ("ep".equals(docType)) {
                classCode.setNodeRepresentation(Constants.EP_CLASSCODE);
                classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                classCode.setValue(Constants.EP_TITLE);
            }

            if ("ps".equals(docType)) {
                classCode.setNodeRepresentation(Constants.PS_CLASSCODE);
                classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                classCode.setValue(Constants.PS_TITLE);
            }

            if ("mro".equals(docType)) {
                classCode.setNodeRepresentation(Constants.MRO_CLASSCODE);
                classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                classCode.setValue(Constants.MRO_TITLE);
            }

            User user = (User) session.getAttribute("user");
            String lang;
            String userLanguage = "";
            String ltrlang = ParamUtil.getString(req, "lang");

            if (Validator.isNull(ltrlang)) {
                userLanguage = user.getLanguageId();
                lang = userLanguage;
            } else {
                lang = ltrlang;
            }

            LOGGER.info("User Language: '{}' - Parameter Request Language: '{}' - Translated Language: {}", userLanguage, ltrlang, lang);

            String lang1 = lang.replace("_", "-");
            lang1 = lang1.replace("en-US", "en-GB");
            lang1 = lang1.replace("en_US", "en-GB");

            LOGGER.info("Portal language is : '{} - {}'", lang, lang1);

            EpsosDocument1 eps = clientConnectorConsumer.retrieveDocument(hcpAssertion, trcAssertion, selectedCountry,
                    documentId, hcid, classCode, lang1);

            selectedEpsosDocument.setAuthor(eps.getAuthor() + "");
            selectedEpsosDocument.setCreationDate(eps.getCreationDate());
            selectedEpsosDocument.setDescription(eps.getDescription());
            selectedEpsosDocument.setTitle(eps.getTitle());

            String xmlfile = new String(eps.getBase64Binary(), "UTF-8");
            LOGGER.debug("The requested XML-PDF file for '{}':\n'{}", uuid, xmlfile);

            pdf = EpsosHelperService.extractPdfPartOfDocument(eps.getBase64Binary());
            writeOutputstream(res, pdf);
        } catch (Exception ex) {
            LOGGER.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    private void writeOutputstream(HttpServletResponse response, byte[] bytes) {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=cdapdf.pdf");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "No-cache");

        try (OutputStream stream = response.getOutputStream()) {

            LOGGER.info("##########3 Serve pdf file");
            stream.write(bytes);
            stream.flush();
        } catch (IOException e) {
            LOGGER.error("IOException: '{}'", e.getMessage(), e);
            response.setContentType("text/html");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Pragma", "No-cache");
            try (OutputStream stream = response.getOutputStream()) {

                stream.write(e.getMessage().getBytes());
            } catch (IOException ex) {
                LOGGER.error("IOException: '{}'", ex.getMessage(), ex);
            }
        }
    }
}
