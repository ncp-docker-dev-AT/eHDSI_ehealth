package com.gnomon.epsos.servlet;

import com.gnomon.epsos.MyServletContextListener;
import com.gnomon.epsos.model.EpsosDocument;
import com.gnomon.epsos.model.cda.Utils;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import epsos.ccd.gnomon.auditmanager.EventOutcomeIndicator;
import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import epsos.openncp.protocolterminator.clientconnector.DocumentId;
import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.GenericDocumentCode;
import eu.epsos.util.EvidenceUtils;
import eu.epsos.util.IheConstants;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CDAServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(CDAServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        try {
            log.info("Rendering CDA Diplay view");
            String exportType = ParamUtil.getString(req, "exportType");
            String cda;
            byte[] output;

            String uuid = req.getParameter("uuid");
            String repositoryId = req.getParameter("repositoryid");
            String hcid = req.getParameter("hcid");

            log.info("Retrieving XML document");
            log.info("uuid: '{}'", uuid);
            log.info("repositoryId: '{}'", repositoryId);
            log.info("hcid: '{}'", hcid);

            EpsosDocument selectedEpsosDocument = new EpsosDocument();
            String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);
            ClientConnectorConsumer clientConectorConsumer = MyServletContextListener.getClientConnectorConsumer();

            HttpSession session = req.getSession();
            log.info("Getting assertions from session");
            Assertion hcpAssertion = (Assertion) session.getAttribute("hcpAssertion");
            Assertion trcAssertion = (Assertion) session.getAttribute("trcAssertion");
            String selectedCountry = (String) session.getAttribute("selectedCountry");
            log.info("HCP ASS: '{}'", hcpAssertion.getID());
            log.info("TRCA ASS: " + trcAssertion.getID());
            log.info("SELECTED COUNTRY: '{}'", selectedCountry);

            User user = (User) session.getAttribute("user");
            if (Validator.isNotNull(user)) {
                log.info("USER IS: " + user.getScreenName());
            }
            log.info("try to set the document going to be retrieved");
            DocumentId documentId = DocumentId.Factory.newInstance();
            log.info("Setting DocumenUniqueID '{}'", uuid);
            documentId.setDocumentUniqueId(uuid);
            log.info("Setting RepositoryUniqueId '{}'", repositoryId);
            documentId.setRepositoryUniqueId(repositoryId);

            String docType = req.getParameter("docType");
            GenericDocumentCode classCode = generateDocumentCode(docType);
            log.info("Document : '{}' is {}", uuid, docType);

            //TODO: Fix languages management - or _.
            log.info("selectedCountry: '{}'", selectedCountry);
            log.info("classCode: '{}", classCode);

            String lang;
            String ltrlang = ParamUtil.getString(req, "lang");
            if (Validator.isNull(ltrlang)) {
                lang = user.getLanguageId();
            } else {
                lang = ltrlang;
            }

            String lang1 = lang.replace("_", "-");
            lang1 = lang1.replace("en-US", "en");

            log.info("Portal language is : '{}-{}'", lang, lang1);

            try {
                EvidenceUtils.createEvidenceREMNRO(classCode.toString(), "NI_DR" + classCode.getValue(),
                        new DateTime(), EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
                        "NI_DR_" + classCode.getValue() + "_REQ", trcAssertion.getID());
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }

            EpsosDocument1 eps = clientConectorConsumer.retrieveDocument(hcpAssertion, trcAssertion, selectedCountry,
                    documentId, hcid, classCode, lang1);

            createEvidenceREMNRR(eps, classCode, trcAssertion);

            selectedEpsosDocument.setAuthor(eps.getAuthor() + "");
            try {
                selectedEpsosDocument.setCreationDate(eps.getCreationDate());
            } catch (Exception ex) {
                log.error(ExceptionUtils.getStackTrace(ex));
            }
            selectedEpsosDocument.setDescription(eps.getDescription());
            selectedEpsosDocument.setTitle(eps.getTitle());

            String xmlfile = new String(eps.getBase64Binary(), "UTF-8");
            log.debug("#### CDA XML Start");
            log.debug(xmlfile);
            log.debug("#### CDA XML End");

            boolean isCDA = false;

            try {
                Document doc1 = Utils.createDomFromString(xmlfile);
                isCDA = EpsosHelperService.isCDA(doc1);
                log.info("### Document created");
                log.info("########## IS CDA '{}'", isCDA);
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
            String actionURL = "dispenseServlet";
            String convertedcda = "";
            if (isCDA) {
                log.info("The document is EPSOS CDA");
                log.info("########### Styling the document that is CDA: '{}' using EPSOS xsl", isCDA);
                // display it using cda display tool
                convertedcda = EpsosHelperService.styleDoc(xmlfile, lang1, false, actionURL);
            } else {
                log.info(("The document is CCD"));
                log.info("########### Styling the document that is CDA: '{}' using standard xsl", isCDA);
                convertedcda = EpsosHelperService.styleDoc(xmlfile, lang1, true, "");
            }

            session.setAttribute("epBytes", xmlfile.getBytes());
            cda = convertedcda;

            if (StringUtils.equals(exportType, "xml")) {
                output = xmlfile.getBytes();
            } else {
                output = cda.getBytes();
            }

            ByteArrayOutputStream baos;

            if (StringUtils.equals(exportType, "pdf")) {
                String fontpath = getServletContext().getRealPath("/") + "/WEB-INF/fonts/";
                baos = EpsosHelperService.ConvertHTMLtoPDF(convertedcda, serviceUrl, fontpath);
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
            res.setContentType("text/html");
            res.setHeader("Cache-Control", "no-cache");
            res.setDateHeader("Expires", 0);
            res.setHeader("Pragma", "No-cache");
            OutputStream stream = res.getOutputStream();
            stream.write(ex.getMessage().getBytes());
            stream.flush();
            stream.close();
            log.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    private void createEvidenceREMNRR(EpsosDocument1 eps, GenericDocumentCode classCode, Assertion trcAssertion) {
        if (Validator.isNotNull(eps)) {
            try {
                EvidenceUtils.createEvidenceREMNRR(classCode.toString(),
                        "NI_DR" + classCode.getValue(),
                        new DateTime(),
                        EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
                        "NI_DR_" + classCode.getValue() + "_RES_SUCC",
                        trcAssertion.getID());
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
        } else {
            try {
                EvidenceUtils.createEvidenceREMNRR(classCode.toString(),
                        "NI_DR" + classCode.getValue(),
                        new DateTime(),
                        EventOutcomeIndicator.TEMPORAL_FAILURE.getCode().toString(),
                        "NI_DR_" + classCode.getValue() + "_RES_FAIL",
                        trcAssertion.getID());
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }

        }
    }

    private GenericDocumentCode generateDocumentCode(String docType) {

        log.debug("generateDocumentCode('{}')", docType);
        GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();

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
        return classCode;
    }
}
