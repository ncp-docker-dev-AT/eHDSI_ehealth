package com.gnomon.epsos.servlet;

import com.gnomon.epsos.MyServletContextListener;
import com.gnomon.epsos.model.Patient;
import com.gnomon.epsos.model.ViewResult;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.GenericDocumentCode;
import epsos.openncp.protocolterminator.clientconnector.SubmitDocumentResponse;
import eu.epsos.util.IheConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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

/**
 * HTTP Servler responsible for handling the eDispense Workflow.
 */
public class DispenseServlet extends HttpServlet {

    private static final long serialVersionUID = -4879064073530149994L;
    private static final String TEXT_HTML = "text/html";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String NO_CACHE = "No-Cache";
    private static final String EXPIRES = "Expires";
    private static final String PRAGMA = "Pragma";
    private final Logger logger = LoggerFactory.getLogger(DispenseServlet.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        logger.info("[OpenNCP Portal] eDispense Servlet...");
        SubmitDocumentResponse submitDocumentResponse = null;
        Boolean substitute = Boolean.FALSE;

        try (OutputStream outputStream = response.getOutputStream()) {

            HttpSession session = request.getSession();
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
                dispensedIds[i] = request.getParameter("dispensationid_" + i);
            }

            if (dispensedIds != null) {

                ArrayList<ViewResult> dispensedLines = new ArrayList<>();

                for (ViewResult line : lines) {

                    int id = line.getMainid();
                    String substituted = request.getParameter("substituted_" + id);
                    String measuresId = request.getParameter("measures_" + id);
                    String dispensedId = request.getParameter("dispensationid_" + id); //field1
                    String dispensedProduct = request.getParameter("dispensedProductValue_" + id);

                    if (Validator.isNull(dispensedProduct)) {
                        dispensedProduct = line.getField1() + "";
                    }

                    // TODO: Workaround related to substitute field and CDA DisplayTool.
                    String dispensedSubstitute = request.getParameter("dispense_" + id); // field3
                    if (StringUtils.isNotBlank(substituted)) {
                        substitute = BooleanUtils.toBooleanObject(substituted);
                    }
                    //  dispenseQuantity replaced by dispensedPackageSize
                    String dispensedPackageSize = request.getParameter("dispensedPackageSize_" + id); // field7
                    if (Validator.isNull(dispensedPackageSize)) {
                        dispensedPackageSize = line.getField21() + "";
                    }

                    String dispensedNumberOfPacks = request.getParameter("dispensedNumberOfPackages_" + id);
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

                    if (loggerClinical.isInfoEnabled()) {
                        loggerClinical.info("[Portal] Medication dispensed: '{}'", line.toString());
                        loggerClinical.info("dispensationid_: {}'", dispensedId);
                        loggerClinical.info("substituted_: {}'", substituted);
                        loggerClinical.info("measures_: {}'", measuresId);
                        loggerClinical.info("dispensedProductValue_: {}'", dispensedProduct);
                        loggerClinical.info("dispensedNumberOfPackages_: {}'", dispensedNumberOfPacks);
                        loggerClinical.info("dispensedPackageSize_: {}'", dispensedPackageSize);
                    }

                    ViewResult dispensedResult = new ViewResult(id, dispensedId, dispensedName, substitute, dispensedStrength,
                            dispensedForm, dispensedPackage, dispensedPackageSize, dispensedNumberOfPacks, prescriptionId,
                            materialId, activeIngredient, measuresId);
                    dispensedLines.add(dispensedResult);
                }

                String eDUid = generateIdentifierExtension();
                String edOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_DISPENSATION_OID);
                if (!dispensedLines.isEmpty()) {
                    edBytes = EpsosHelperService.generateDispensationDocumentFromPrescription2(epBytes, dispensedLines, user, eDUid);
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
                    Calendar calendar = Calendar.getInstance();
                    document.setCreationDate(calendar);
                    document.setDescription(Constants.ED_TITLE);
                    document.setTitle(Constants.ED_TITLE);
                    document.setUuid(edOid + "^" + eDUid);
                    document.setSubmissionSetId(EpsosHelperService.getUniqueId());
                    document.setClassCode(classCode);
                    document.setFormatCode(formatCode);
                    document.setBase64Binary(edBytes);

                    submitDocumentResponse = proxy.submitDocument(hcpAssertion, trcAssertion, selectedCountry, document, patient.getPatientDemographics());

                    response.setContentType(TEXT_HTML);
                    String message = "Dispensation successful";
                    response.setHeader(CACHE_CONTROL, NO_CACHE);
                    response.setDateHeader(EXPIRES, 0);
                    response.setHeader(PRAGMA, NO_CACHE);

                    outputStream.write(message.getBytes());

                } else {
                    logger.error("[Portal] Upload of eDispense Document response ERROR");
                    response.setContentType(TEXT_HTML);
                    String message = "Cannot upload Dispense message";
                    response.setHeader(CACHE_CONTROL, NO_CACHE);
                    response.setDateHeader(EXPIRES, 0);
                    response.setHeader(PRAGMA, NO_CACHE);

                    outputStream.write(message.getBytes());
                    request.setAttribute("exception", "Upload of eDispense Document response ERROR");
                }
            }
        } catch (Exception ex) {

            logger.error("[Portal] Exception during the dispense process: '{}'", ex.getMessage(), ex);
            response.setContentType(TEXT_HTML);
            String message;
            if (submitDocumentResponse != null) {
                message = submitDocumentResponse.toString();
            } else {
                message = ex.getLocalizedMessage();
            }
            response.setHeader(CACHE_CONTROL, NO_CACHE);
            response.setDateHeader(EXPIRES, 0);
            response.setHeader(PRAGMA, NO_CACHE);

            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(message.getBytes());

            } catch (IOException e) {
                logger.error("IOException: '{}'", e.getMessage(), e);
            }
        }
    }

    /**
     * Generates UID extension required by the eDispense.
     *
     * @return a String formatted UID required by CDA eD ID extension.
     */
    private String generateIdentifierExtension() {

        Random r = new SecureRandom();
        byte[] b = new byte[16];
        r.nextBytes(b);
        String s = Base64.encodeBase64String(b);
        return s.substring(0, 16);
    }
}
