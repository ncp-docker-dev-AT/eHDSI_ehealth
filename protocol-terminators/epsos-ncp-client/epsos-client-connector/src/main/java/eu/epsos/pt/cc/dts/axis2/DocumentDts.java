package eu.epsos.pt.cc.dts.axis2;

import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;
import tr.com.srdc.epsos.data.model.xds.XDSDocument;
import tr.com.srdc.epsos.data.model.xds.XDSDocumentAssociation;
import tr.com.srdc.epsos.util.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This is an Data Transformation Service providing functions to transform data into a Document object.
 *
 * @author Marcelo Fonseca - <marcelo.fonseca@iuz.pt>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class DocumentDts {

    /**
     * Private constructor to disable class instantiation.
     */
    private DocumentDts() {
    }

    /**
     * Converts a XDSDocument into a Document.
     *
     * @param document the document to be converted.
     * @return the result of the conversion, as a Document.
     */
    public static EpsosDocument1 newInstance(XDSDocument document) {

        if (document == null) {
            return null;
        }

        final EpsosDocument1 result = EpsosDocument1.Factory.newInstance();
        result.setUuid(document.getDocumentUniqueId());
        result.setDescription(document.getDescription());
        result.setCreationDate(convertDate(document.getCreationTime()));
        result.setClassCode(GenericDocumentCodeDts.newInstance(document.getClassCode()));
        result.setFormatCode(GenericDocumentCodeDts.newInstance(document.getFormatCode()));
        result.setRepositoryId(document.getRepositoryUniqueId());
        result.setHcid(document.getHcid());
        result.setAuthor(document.getAuthorPerson());

        if (result.getClassCode() != null && !result.getClassCode().getNodeRepresentation().isEmpty()) {
            switch (result.getClassCode().getNodeRepresentation()) {
                case Constants.PS_CLASSCODE:
                    result.setTitle(Constants.PS_TITLE);
                    break;
                case Constants.EP_CLASSCODE:
                    result.setTitle(Constants.EP_TITLE);
                    break;
                case Constants.ED_CLASSCODE:
                    result.setTitle(Constants.ED_TITLE);
                    break;
                case Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE:
                    result.setTitle(Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_TITLE);
                    break;
                case Constants.ORCD_LABORATORY_RESULTS_CLASSCODE:_CLASSCODE:
                    result.setTitle(Constants.ORCD_LABORATORY_RESULTS_TITLE);
                    break;
                case Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE:
                    result.setTitle(Constants.ORCD_MEDICAL_IMAGING_REPORTS_TITLE);
                    break;
                case Constants.ORCD_MEDICAL_IMAGES_CLASSCODE:
                    result.setTitle(Constants.ORCD_MEDICAL_IMAGES_TITLE);
                    break;
                default:
                    // Document Type not supported
                    result.setTitle(Constants.UNKNOWN_TITLE);
                    break;
            }
        }

        return result;
    }

    /**
     * Converts a list of XDSDocument to a list of Document.
     *
     * @param documentAssociation the list of XDSDocument.
     * @return the result of the conversion, as a list of Document.
     */
    public static EpsosDocument1[] newInstance(List<XDSDocumentAssociation> documentAssociation) {

        if (documentAssociation == null || documentAssociation.isEmpty()) {
            return new EpsosDocument1[0];
        }

        List<EpsosDocument1> resultList = new ArrayList<>();

        for (XDSDocumentAssociation doc : documentAssociation) {
            EpsosDocument1 xmlDoc = DocumentDts.newInstance(doc.getCdaXML());
            EpsosDocument1 pdfDoc = DocumentDts.newInstance(doc.getCdaPDF());

            //  If CDA L1 and L3 are existing then we shall create an association between the 2 documents.
            if (xmlDoc != null && pdfDoc != null) {
                pdfDoc.setAssociatedDocumentsArray(new EpsosDocument1[]{xmlDoc});
                xmlDoc.setAssociatedDocumentsArray(new EpsosDocument1[]{pdfDoc});
            }

            // Adding the reference to the L1 CDA document
            if (pdfDoc != null) {
                resultList.add(pdfDoc);
            }

            // Adding the reference to the L3 CDA document
            if (xmlDoc != null) {
                resultList.add(xmlDoc);
            }
        }
        return resultList.toArray(new EpsosDocument1[resultList.size()]);
    }

    /**
     * Converts a DocumentResponse into a Document new instance.
     *
     * @param documentResponse the document to be converted.
     * @return the result of the conversion, as a Document.
     */
    public static EpsosDocument1 newInstance(DocumentResponse documentResponse) {

        if (documentResponse == null) {
            return null;
        }
        final EpsosDocument1 result = EpsosDocument1.Factory.newInstance();
        result.setHcid(documentResponse.getHomeCommunityId());
        result.setUuid(documentResponse.getDocumentUniqueId());
        result.setMimeType(documentResponse.getMimeType());
        result.setRepositoryId(documentResponse.getRepositoryUniqueId());
        result.setBase64Binary(documentResponse.getDocument());

        return result;
    }

    /**
     * Converts a string containing a date in the yyyyMMddHHmmss format to a Calendar instance.
     *
     * @param dateString a String representation of the Date.
     * @return a Calendar instance, with the given String values.
     */
    private static Calendar convertDate(String dateString) {

        String pattern1 = "yyyyMMddHHmmss";
        String pattern2 = "yyyyMMdd";
        String selectedPattern;

        if (dateString.length() == pattern1.length()) {
            selectedPattern = pattern1;
        } else if (dateString.length() == pattern2.length()) {
            selectedPattern = pattern2;
        } else {
            return null;
        }

        DateFormat formatter;
        Date date;
        Calendar cal;

        formatter = new SimpleDateFormat(selectedPattern);
        try {
            date = formatter.parse(dateString);
            cal = Calendar.getInstance();
            cal.setTime(date);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }

        return cal;
    }
}
