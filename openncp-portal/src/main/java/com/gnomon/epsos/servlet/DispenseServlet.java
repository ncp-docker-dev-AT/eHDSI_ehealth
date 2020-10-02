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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 * HTTP Servlet responsible for handling the eDispense Workflow.
 */
public class DispenseServlet extends HttpServlet {

    private static final long serialVersionUID = -4879064073530149994L;
    private static final String TEXT_HTML = "text/html";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String NO_CACHE = "No-Cache";
    private static final String EXPIRES = "Expires";
    private static final String PRAGMA = "Pragma";
    private final Logger logger = LoggerFactory.getLogger(DispenseServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {

        logger.info("[OpenNCP Portal] eDispense Servlet...");


        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        SubmitDocumentResponse submitDocumentResponse = null;
        Boolean substitute = Boolean.FALSE;

        try (OutputStream outputStream = response.getOutputStream()) {

            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            byte[] epBytes = (byte[]) session.getAttribute("epBytes");
            Patient patient = (Patient) session.getAttribute("patient");
            String selectedCountry = (String) session.getAttribute("selectedCountry");
            //  Checking validity of the assertions (HCP and TRC)
            Assertion hcpAssertion = (Assertion) session.getAttribute("hcpAssertion");
            Assertion trcAssertion = (Assertion) session.getAttribute("trcAssertion");

            // eHDSI Wave 3 specification: only 1 item is expected from the ePrescription.
            List<ViewResult> ePrescriptionLines = EpsosHelperService.parsePrescriptionDocumentForPrescriptionLines(epBytes);
            List<ViewResult> dispensedLines = new ArrayList<>();

            for (ViewResult line : ePrescriptionLines) {

                int id = line.getMainid();
                String substituted = request.getParameter("substituted_" + id);
                String measuresId = request.getParameter("measures_" + id);
                String dispensedId = request.getParameter("dispensationid_" + id);
                String dispensedProduct = request.getParameter("dispensedProductValue_" + id);

                if (Validator.isNull(dispensedProduct)) {
                    dispensedProduct = line.getField1() + "";
                }

                // TODO: Workaround related to substitute field and CDA DisplayTool.
                String dispensedSubstitute = request.getParameter("dispense_" + id);
                if (StringUtils.isNotBlank(substituted)) {
                    substitute = BooleanUtils.toBooleanObject(substituted);
                }
                //  dispenseQuantity replaced by dispensedPackageSize
                String dispensedPackageSize = request.getParameter("dispensedPackageSize_" + id);
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
                String dispensedPackage = line.getField4() + "";
                String prescriptionId = line.getField14() + "";
                String materialId = line.getField19() + "";
                String activeIngredient = line.getField2().toString();

                if (logger.isInfoEnabled()) {
                    logger.info("[Portal] Medication dispensed: '{}'", line.toString());
                    logger.info("dispensationId_: {}'", dispensedId);
                    logger.info("substituted_: {}'", substituted);
                    logger.info("measures_: {}'", measuresId);
                    logger.info("dispensedProductValue_: {}'", dispensedProduct);
                    logger.info("dispensedNumberOfPackages_: {}'", dispensedNumberOfPacks);
                    logger.info("dispensedPackageSize_: {}'", dispensedPackageSize);
                }

                ViewResult dispensedResult = new ViewResult(id, dispensedId, dispensedName, substitute, dispensedStrength,
                        dispensedForm, dispensedPackage, dispensedPackageSize, dispensedNumberOfPacks, prescriptionId,
                        materialId, activeIngredient, measuresId);
                dispensedLines.add(dispensedResult);
            }

            String eDUid = generateIdentifierExtension();
            String edOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_DISPENSATION_OID);
            if (!dispensedLines.isEmpty()) {
                byte[] edBytes = EpsosHelperService.generateDispensationDocumentFromPrescription(epBytes, dispensedLines, user, eDUid);
                if (edBytes.length == 0) {
                    throw new Exception();
                }
                EpsosDocument1 document = buildDispenseDocument(user, edOid, eDUid, edBytes);
                ClientConnectorConsumer proxy = MyServletContextListener.getClientConnectorConsumer();
                submitDocumentResponse = proxy.submitDocument(hcpAssertion, trcAssertion, selectedCountry, document, patient.getPatientDemographics());
                setResponseHeaders(response);
                String message = "Dispensation successful";
                outputStream.write(message.getBytes());
                //  Serialize medication dispense as XML
                writeFile(eDUid, document.getBase64Binary());
            }
        } catch (Exception e) {

            logger.error("[Portal] Exception during the dispense process: '{}'", e.getMessage(), e);
            String message;
            if (submitDocumentResponse != null) {
                message = submitDocumentResponse.toString();
            } else {
                message = e.getLocalizedMessage();
            }
            setResponseHeaders(response);

            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(message.getBytes());

            } catch (IOException ex) {
                logger.error("IOException: '{}'", ex.getMessage(), ex);
            }
        }
    }

    /**
     * @param dispenseRoot
     * @param dispenseExtension
     * @param dispense
     * @return
     */
    private EpsosDocument1 buildDispenseDocument(User user, String dispenseRoot, String dispenseExtension, byte[] dispense) {

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
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        document.setCreationDate(calendar);
        document.setDescription(Constants.ED_TITLE);
        document.setTitle(Constants.ED_TITLE);
        document.setUuid(dispenseRoot + "^" + dispenseExtension);
        document.setSubmissionSetId(EpsosHelperService.getUniqueId());
        document.setClassCode(classCode);
        document.setFormatCode(formatCode);
        document.setBase64Binary(dispense);

        return document;
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

    /**
     * @param response
     */
    private void setResponseHeaders(HttpServletResponse response) {

        response.setContentType(TEXT_HTML);
        response.setHeader(CACHE_CONTROL, NO_CACHE);
        response.setDateHeader(EXPIRES, 0);
        response.setHeader(PRAGMA, NO_CACHE);
    }

    private void writeFile(String dispenseId, byte[] document) {

        StringBuilder patientFile = new StringBuilder();
        patientFile.append(Constants.EPSOS_PROPS_PATH).append("integration/");
        String directoryName = patientFile.append(Constants.HOME_COMM_ID).append("/medication").toString();
        //String fileName = dispenseId + ".xml";
        String fileName = UUID.randomUUID().toString() + ".xml";

        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(directoryName + "/" + fileName);
        String dispense = new String(document, StandardCharsets.UTF_8);
        try (FileWriter fileWriter = new FileWriter(file.getAbsoluteFile())) {
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(dispense);
            bufferedWriter.close();
        } catch (IOException e) {
            logger.error("IOException: '{}'", e.getMessage(), e);
        }
    }
}
