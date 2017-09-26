package com.gnomon.epsos.servlet;

import com.gnomon.epsos.MyServletContextListener;
import com.gnomon.epsos.model.Patient;
import com.gnomon.epsos.model.ViewResult;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import epsos.ccd.posam.tm.util.XmlUtil;
import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.GenericDocumentCode;
import epsos.openncp.protocolterminator.clientconnector.SubmitDocumentResponse;
import eu.epsos.util.IheConstants;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.w3c.dom.Document;

public class DispenseServlet extends HttpServlet {

    private static final long serialVersionUID = -4879064073530149994L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DispenseServlet.class);

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        SubmitDocumentResponse resp = null;
        LOGGER.info("dispensing ...");

        HttpSession session = req.getSession();
        String selectedCountry = (String) session.getAttribute("selectedCountry");
        Patient patient = (Patient) session.getAttribute("patient");
        Assertion hcpAssertion = (Assertion) session.getAttribute("hcpAssertion");
        Assertion trcAssertion = (Assertion) session.getAttribute("trcAssertion");
        User user = (User) session.getAttribute("user");

        byte[] edBytes = null;
        try {
            byte[] epBytes = (byte[]) session.getAttribute("epBytes");

            List<ViewResult> lines = EpsosHelperService.parsePrescriptionDocumentForPrescriptionLines(epBytes);

            String[] dispensedIds = new String[lines.size()];

            for (int i = 0; i < lines.size(); i++) {
                dispensedIds[i] = req.getParameter("dispensationid_" + i);
            }

            String language = ParamUtil.getString(req, "language");
            String fullname = "EPSOS PORTAL";

            try {
                fullname = user.getFullName();
            } catch (Exception e1) {
                LOGGER.error(ExceptionUtils.getStackTrace(e1));
            }

            if (dispensedIds != null) {

                ArrayList<ViewResult> dispensedLines = new ArrayList<>();

                for (ViewResult line : lines) {
                    int id = line.getMainid();

                    String measures_id = req.getParameter("measures_" + id);
                    String dispensed_id = req.getParameter("dispensationid_" + id); //field1
                    String dispensedProduct = req.getParameter("dispensedProductValue_" + id);

                    if (Validator.isNull(dispensedProduct)) {
                        dispensedProduct = line.getField1() + "";
                    }

                    String dispensed_substitute = req.getParameter("dispense_" + id); // field3
                    boolean substitute = GetterUtil.getBoolean(dispensed_substitute, false);

                    String dispensed_quantity = req.getParameter("dispensedPackageSize_" + id); // field7 //lathos

                    if (Validator.isNull(dispensed_quantity)) {
                        dispensed_quantity = line.getField21() + "";
                    }

                    String dispensed_name = dispensedProduct;
                    String dispensed_strength = line.getField3() + "";
                    String dispensed_form = line.getField4() + "";
                    String dispensed_package = line.getField4() + ""; //request.getParameter("packaging2_"+id); // field6
                    String dispensed_nrOfPacks = line.getField8().toString();
                    String prescriptionid = line.getField14() + ""; // field9 //lathos
                    String materialid = line.getField19() + ""; // field10
                    String activeIngredient = line.getField2().toString();

                    ViewResult d_line = new ViewResult(id, dispensed_id, dispensed_name, substitute, dispensed_strength,
                            dispensed_form, dispensed_package, dispensed_quantity, dispensed_nrOfPacks, prescriptionid,
                            materialid, activeIngredient, measures_id);

                    dispensedLines.add(d_line);
                }

                String eDuuid = java.util.UUID.randomUUID().toString().replaceAll("-", "");
                String edOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_DISPENSATION_OID);
                if (!dispensedLines.isEmpty()) {
                    edBytes = EpsosHelperService.generateDispensationDocumentFromPrescription2(epBytes, dispensedLines, user, eDuuid);
                }

                if (Validator.isNotNull(edBytes)) {

                    ClientConnectorConsumer proxy = MyServletContextListener.getClientConnectorConsumer();
                    GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();
                    classCode.setNodeRepresentation(Constants.ED_CLASSCODE);
                    classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                    classCode.setValue(Constants.ED_TITLE);

                    GenericDocumentCode formatCode = GenericDocumentCode.Factory.newInstance();
                    formatCode.setSchema(IheConstants.DISPENSATION_FORMATCODE_CODINGSCHEMA);
                    formatCode.setNodeRepresentation(IheConstants.DISPENSATION_FORMATCODE_NODEREPRESENTATION);
                    formatCode.setValue(IheConstants.DISPENSATION_FORMATCODE_DISPLAYNAME);

                    EpsosDocument1 document = EpsosDocument1.Factory.newInstance();
                    document.setAuthor(user.getFullName());
                    Calendar cal = Calendar.getInstance();
                    document.setCreationDate(cal);
                    document.setDescription(Constants.ED_TITLE);
                    document.setTitle(Constants.ED_TITLE);
                    document.setUuid(edOid + "^" + eDuuid);
                    document.setSubmissionSetId(EpsosHelperService.getUniqueId()); 
                    document.setClassCode(classCode);
                    document.setFormatCode(formatCode);
                    document.setBase64Binary(edBytes);

                    resp = proxy.submitDocument(hcpAssertion, trcAssertion, selectedCountry, document, patient.getPatientDemographics());

                    res.setContentType("text/html");
                    String message = "Dispensation successful";
                    res.setHeader("Cache-Control", "no-cache");
                    res.setDateHeader("Expires", 0);
                    res.setHeader("Pragma", "No-cache");

                    OutputStream OutStream = res.getOutputStream();
                    OutStream.write(message.getBytes());
                    OutStream.flush();
                    OutStream.close();
                } else {
                    LOGGER.error("UPLOAD DISP DOC RESPONSE ERROR");
                    res.setContentType("text/html");
                    String message = resp.toString();
                    res.setHeader("Cache-Control", "no-cache");
                    res.setDateHeader("Expires", 0);
                    res.setHeader("Pragma", "No-cache");

                    OutputStream OutStream = res.getOutputStream();
                    OutStream.write(message.getBytes());
                    OutStream.flush();
                    OutStream.close();
                    req.setAttribute("exception", "UPLOAD DISP DOC RESPONSE ERROR");
                }
            }
        } catch (Exception ex) {

            LOGGER.error("UPLOAD DISP DOC RESPONSE ERROR: '{}'", ex.getMessage(), ex);
            res.setContentType("text/html");
            String message;
            if (Validator.isNotNull(resp)) {
                message = resp.toString();
            } else {
                message = ex.getLocalizedMessage();
            }
            res.setHeader("Cache-Control", "no-cache");
            res.setDateHeader("Expires", 0);
            res.setHeader("Pragma", "No-cache");

            try (OutputStream outputStream = res.getOutputStream()) {
                outputStream.write(message.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                LOGGER.error("IOException: '{}'", e.getMessage(), e);
            }
        }
    }
}
