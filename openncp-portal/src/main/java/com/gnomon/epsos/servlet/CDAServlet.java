package com.gnomon.epsos.servlet;

import com.gnomon.epsos.MyServletContextListener;
import com.gnomon.epsos.model.EpsosDocument;
import com.gnomon.epsos.model.cda.Utils;
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
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import tr.com.srdc.epsos.util.Constants;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CDAServlet extends HttpServlet {

    private static final long serialVersionUID = -8267246289076570225L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CDAServlet.class);

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) {

        try {
            LOGGER.info("Rendering CDA Display view");
            String exportType = ParamUtil.getString(req, "exportType");
            String cda;
            byte[] output;

            String uuid = req.getParameter("uuid");
            String repositoryId = req.getParameter("repositoryid");
            String hcid = req.getParameter("hcid");

            LOGGER.info("Retrieving XML document");
            LOGGER.info("uuid: '{}'", uuid);
            LOGGER.info("repositoryId: '{}'", repositoryId);
            LOGGER.info("hcid: '{}'", hcid);

            EpsosDocument selectedEpsosDocument = new EpsosDocument();
            String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);
            ClientConnectorConsumer clientConnectorConsumer = MyServletContextListener.getClientConnectorConsumer();

            HttpSession session = req.getSession();
            LOGGER.info("Getting assertions from session");
            Assertion hcpAssertion = (Assertion) session.getAttribute("hcpAssertion");
            Assertion trcAssertion = (Assertion) session.getAttribute("trcAssertion");
            String selectedCountry = (String) session.getAttribute("selectedCountry");
            LOGGER.info("HCP ASS: '{}'", hcpAssertion.getID());
            LOGGER.info("TRCA ASS: '{}'", trcAssertion.getID());
            LOGGER.info("SELECTED COUNTRY: '{}'", selectedCountry);

            User user = (User) session.getAttribute("user");
            if (Validator.isNotNull(user)) {
                LOGGER.info("USER IS: '{}'", user.getScreenName());
            }
            LOGGER.info("try to set the document going to be retrieved");
            DocumentId documentId = DocumentId.Factory.newInstance();
            LOGGER.info("Setting DocumenUniqueID '{}'", uuid);
            documentId.setDocumentUniqueId(uuid);
            LOGGER.info("Setting RepositoryUniqueId '{}'", repositoryId);
            documentId.setRepositoryUniqueId(repositoryId);

            String docType = req.getParameter("docType");
            GenericDocumentCode classCode = generateDocumentCode(docType);
            LOGGER.info("Document : '{}' is {}", uuid, docType);
            LOGGER.info("selectedCountry: '{}'", selectedCountry);
            LOGGER.info("classCode: '{}", classCode);

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
            LOGGER.debug("#### CDA XML Start");
            LOGGER.debug(xmlfile);
            LOGGER.debug("#### CDA XML End");

            boolean isCDA;
            Document doc1 = Utils.createDomFromString(xmlfile);
            isCDA = EpsosHelperService.isCDA(doc1);
            LOGGER.info("### Document created");
            LOGGER.info("########## IS CDA '{}'", isCDA);

            String actionURL = "dispenseServlet";
            String convertedCda;
            if (isCDA) {
                LOGGER.info("The document is EPSOS CDA");
                // display it using cda display tool
                convertedCda = EpsosHelperService.styleDoc(xmlfile, lang1, false, actionURL);
            } else {
                LOGGER.info(("The document is CCD"));
                convertedCda = EpsosHelperService.styleDoc(xmlfile, lang1, true, "");
            }

            session.setAttribute("epBytes", xmlfile.getBytes());
            cda = convertedCda;

            if (StringUtils.equals(exportType, "xml")) {
                output = xmlfile.getBytes();
            } else {
                output = cda.getBytes();
            }

            ByteArrayOutputStream baos;

            if (StringUtils.equals(exportType, "pdf")) {
                String fontpath = getServletContext().getRealPath("/") + "/WEB-INF/fonts/";
                baos = EpsosHelperService.convertHTMLtoPDF(convertedCda, serviceUrl, fontpath);
                output = baos.toByteArray();
                res.setContentType("application/pdf");
                res.setHeader("Content-Disposition", "attachment; filename=cda.pdf");

            } else if (StringUtils.equals(exportType, "xml")) {
                res.setHeader("Content-Disposition", "attachment; filename=cda.xml");
                res.setContentType("text/xml");

            } else {
                res.setContentType("text/html");
            }

            res.setHeader("Cache-Control", "no-cache");
            res.setDateHeader("Expires", 0);
            res.setHeader("Pragma", "No-cache");
            OutputStream stream = res.getOutputStream();
            stream.write(output);
            stream.flush();
            stream.close();

        } catch (Exception ex) {
            LOGGER.error("{}: '{}'", ex.getClass(), ex.getMessage(), ex);
            res.setContentType("text/html");
            res.setHeader("Cache-Control", "no-cache");
            res.setDateHeader("Expires", 0);
            res.setHeader("Pragma", "No-cache");
            try (OutputStream stream = res.getOutputStream()) {
                stream.write(ex.getMessage().getBytes());
                stream.flush();
            } catch (IOException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
            LOGGER.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    private GenericDocumentCode generateDocumentCode(String docType) {

        LOGGER.debug("generateDocumentCode('{}')", docType);
        GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();

        switch (docType) {
            case "ep":
                classCode.setNodeRepresentation(Constants.EP_CLASSCODE);
                classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                classCode.setValue(Constants.EP_TITLE);
                break;
            case "ps":
                classCode.setNodeRepresentation(Constants.PS_CLASSCODE);
                classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                classCode.setValue(Constants.PS_TITLE);
                break;
            case "mro":
                classCode.setNodeRepresentation(Constants.MRO_CLASSCODE);
                classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                classCode.setValue(Constants.MRO_TITLE);
                break;
        }
        return classCode;
    }
}
