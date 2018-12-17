package com.gnomon.epsos.servlet;

import com.gnomon.epsos.MyServletContextListener;
import com.gnomon.epsos.model.Patient;
import com.gnomon.epsos.model.ViewResult;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.GenericDocumentCode;
import epsos.openncp.protocolterminator.clientconnector.SubmitDocumentResponse;
import eu.epsos.util.IheConstants;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class DispenseServlet extends HttpServlet {

    private static final long serialVersionUID = -4879064073530149994L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DispenseServlet.class);

    private static final String TEXT_HTML = "text/html";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String NO_CACHE = "No-Cache";
    private static final String EXPIRES = "Expires";
    private static final String PRAGMA = "Pragma";

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) {

        SubmitDocumentResponse resp = null;
        LOGGER.info("DispenseServlet...");

        try (OutputStream outputStream = res.getOutputStream()) {

            HttpSession session = req.getSession();
            String selectedCountry = (String) session.getAttribute("selectedCountry");
            Patient patient = (Patient) session.getAttribute("patient");
            Assertion hcpAssertion = (Assertion) session.getAttribute("hcpAssertion");
            Assertion trcAssertion = (Assertion) session.getAttribute("trcAssertion");
            User user = (User) session.getAttribute("user");
            byte[] edBytes = null;

            byte[] epBytes = (byte[]) session.getAttribute("epBytes");
            List<ViewResult> lines = EpsosHelperService.parsePrescriptionDocumentForPrescriptionLines(epBytes);
            String[] dispensedIds = new String[lines.size()];

            for (int i = 0; i < lines.size(); i++) {
                dispensedIds[i] = req.getParameter("dispensationid_" + i);
            }

            if (dispensedIds != null) {

                ArrayList<ViewResult> dispensedLines = new ArrayList<>();

                for (ViewResult line : lines) {
                    int id = line.getMainid();

                    String measuresId = req.getParameter("measures_" + id);
                    String dispensedId = req.getParameter("dispensationid_" + id); //field1
                    String dispensedProduct = req.getParameter("dispensedProductValue_" + id);

                    if (Validator.isNull(dispensedProduct)) {
                        dispensedProduct = line.getField1() + "";
                    }

                    String dispensedSubstitute = req.getParameter("dispense_" + id); // field3
                    boolean substitute = GetterUtil.getBoolean(dispensedSubstitute, false);
                    //  dispenseQuantity replaced by dispensedPackageSize
                    String dispensedPackageSize = req.getParameter("dispensedPackageSize_" + id); // field7 //lathos
                    if (Validator.isNull(dispensedPackageSize)) {
                        dispensedPackageSize = line.getField21() + "";
                    }

                    String dispensedNumberOfPacks = req.getParameter("dispensedNumberOfPackages_" + id);
                    if (Validator.isNull(dispensedNumberOfPacks)) {
                        dispensedNumberOfPacks = line.getField8() + "";
                    }

                    String dispensedName = dispensedProduct;
                    String dispensedStrength = line.getField3() + "";
                    String dispensedForm = line.getField4() + "";
                    String dispensedPackage = line.getField4() + ""; // field6
                    String prescriptionId = line.getField14() + ""; // field9
                    String materialId = line.getField19() + ""; // field10
                    String activeIngredient = line.getField2().toString();

                    ViewResult dispensedResult = new ViewResult(id, dispensedId, dispensedName, substitute, dispensedStrength,
                            dispensedForm, dispensedPackage, dispensedPackageSize, dispensedNumberOfPacks, prescriptionId,
                            materialId, activeIngredient, measuresId);
                    dispensedLines.add(dispensedResult);
                }

                String eDUUID = generateIdentifierExtension();
                String edOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_DISPENSATION_OID);
                if (!dispensedLines.isEmpty()) {
                    edBytes = EpsosHelperService.generateDispensationDocumentFromPrescription2(epBytes, dispensedLines, user, eDUUID);
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
                    document.setUuid(edOid + "^" + eDUUID);
                    document.setSubmissionSetId(EpsosHelperService.getUniqueId());
                    document.setClassCode(classCode);
                    document.setFormatCode(formatCode);
                    document.setBase64Binary(edBytes);

                    resp = proxy.submitDocument(hcpAssertion, trcAssertion, selectedCountry, document, patient.getPatientDemographics());

                    res.setContentType(TEXT_HTML);
                    String message = "Dispensation successful";
                    res.setHeader(CACHE_CONTROL, NO_CACHE);
                    res.setDateHeader(EXPIRES, 0);
                    res.setHeader(PRAGMA, NO_CACHE);

                    outputStream.write(message.getBytes());

                } else {
                    LOGGER.error("UPLOAD DISP DOC RESPONSE ERROR");
                    res.setContentType(TEXT_HTML);
                    String message = "Cannot upload Dispense message";
                    res.setHeader(CACHE_CONTROL, NO_CACHE);
                    res.setDateHeader(EXPIRES, 0);
                    res.setHeader(PRAGMA, NO_CACHE);

                    outputStream.write(message.getBytes());
                    req.setAttribute("exception", "UPLOAD DISP DOC RESPONSE ERROR");
                }
            }
        } catch (Exception ex) {

            LOGGER.error("UPLOAD DISP DOC RESPONSE ERROR: '{}'", ex.getMessage(), ex);
            res.setContentType(TEXT_HTML);
            String message;
            if (resp != null) {
                message = resp.toString();
            } else {
                message = ex.getLocalizedMessage();
            }
            res.setHeader(CACHE_CONTROL, NO_CACHE);
            res.setDateHeader(EXPIRES, 0);
            res.setHeader(PRAGMA, NO_CACHE);

            try (OutputStream outputStream = res.getOutputStream()) {
                outputStream.write(message.getBytes());

            } catch (IOException e) {
                LOGGER.error("IOException: '{}'", e.getMessage(), e);
            }
        }
    }

    private String generateIdentifierExtension() {

        Random r = new SecureRandom();
        byte[] b = new byte[16];
        r.nextBytes(b);
        String s = Base64.encodeBase64String(b);
        return s.substring(0, 16);
    }
}
