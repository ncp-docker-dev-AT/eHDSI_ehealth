package com.gnomon.epsos.servlet;

import com.gnomon.epsos.MyServletContextListener;
import com.gnomon.epsos.model.Patient;
import com.gnomon.epsos.service.EpsosHelperService;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

public class DiscardDispenseServlet extends HttpServlet {

    private static final long serialVersionUID = 7738274887962003848L;
    private final Logger logger = LoggerFactory.getLogger(DiscardDispenseServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        logger.info("[Portal] Discard Dispense Operation");
        String documentId = request.getParameter("documentId");
        File file = loadMedication(documentId);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        Patient patient = (Patient) session.getAttribute("patient");
        String selectedCountry = (String) session.getAttribute("selectedCountry");
        //  Checking validity of the assertions (HCP and TRC)
        Assertion hcpAssertion = (Assertion) session.getAttribute("hcpAssertion");
        Assertion trcAssertion = (Assertion) session.getAttribute("trcAssertion");
        String eDUid = generateIdentifierExtension();
        String edOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_DISPENSATION_OID);
        EpsosDocument1 document = buildDispenseDocument(user, edOid, eDUid, fileContent);
        ClientConnectorConsumer proxy = MyServletContextListener.getClientConnectorConsumer();
        logger.info("Discard Operation Debug: '{}', '{}', '{}', '{}', '{}'", hcpAssertion.getID(), trcAssertion.getID(), selectedCountry, document.getRepositoryId(), patient.getRoot());
        SubmitDocumentResponse submitDocumentResponse = proxy.submitDocument(hcpAssertion, trcAssertion, selectedCountry, document, patient.getPatientDemographics());
        if (logger.isInfoEnabled()) {
            logger.info("[Portal] Discard operation: '{}'", submitDocumentResponse.toString());
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

    /**
     * @param dispenseRoot
     * @param dispenseExtension
     * @param dispense
     * @return
     */
    private EpsosDocument1 buildDispenseDocument(User user, String dispenseRoot, String dispenseExtension, byte[] dispense) {

        GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();
        classCode.setNodeRepresentation("DISCARD-" + Constants.ED_CLASSCODE);
        classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
        classCode.setValue(Constants.ED_TITLE);

        //<formatCode>
        //                  <nodeRepresentation>urn:epsos:ep:dis:2010</nodeRepresentation>
        //                  <schema>epSOS formatCodes</schema>
        //                  <value>epSOS coded eDispensation</value>
        //               </formatCode>

        //         public static final String DISPLAY_NAME = "eHDSI coded eDispensation Discard";
        //                    public static final String NODE_REPRESENTATION = "urn:eHDSI:ed:discard:2020";
        //                    public static final String CODING_SCHEME = "eHDSI formatCodes";
        GenericDocumentCode formatCode = GenericDocumentCode.Factory.newInstance();
        formatCode.setSchema("eHDSI formatCodes");
        formatCode.setNodeRepresentation("urn:eHDSI:ed:discard:2020");
        formatCode.setValue("eHDSI coded eDispensation Discard");

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

    private File loadMedication(String documentId) {

        String directoryName = Constants.EPSOS_PROPS_PATH + "integration/" +
                Constants.HOME_COMM_ID + "/medication/" + documentId;
        File medication = new File(directoryName);
//        File[] listOfFiles = folder.listFiles();
//
//        List<String> medicationList = new ArrayList<>();
//        if (listOfFiles != null) {
//            for (File file : listOfFiles) {
//                if (file.isFile()) {
//                    medicationList.add(file.getName());
//                }
//            }
//        }
        return medication;
    }
}
