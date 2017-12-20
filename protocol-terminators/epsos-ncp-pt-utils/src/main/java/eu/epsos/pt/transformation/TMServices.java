package eu.epsos.pt.transformation;

import epsos.ccd.posam.tm.response.TMResponseStructure;
import epsos.ccd.posam.tm.service.ITransformationService;
import epsos.ccd.posam.tsam.exception.ITMTSAMEror;
import eu.epsos.exceptions.DocumentTransformationException;
import org.apache.axis2.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * Encapsulates all the usage of Transformation Manager for transcoding or translation processes.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public final class TMServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(TMServices.class);
    private final static String REPOSITORY_INTERNAL_ERROR = "XDSRepositoryError";

    /**
     * Encapsulates the TM usage, by accepting the document to translate, and the selected language.
     *
     * @param document the document to translate, in a byte array form.
     * @param language the language of NPC-B, where the document is being translated.
     * @return a translated document.
     * @throws DocumentTransformationException If the transformation of the CDA document producing an error not listed
     *                                         into the known potential error codes.
     */
    public static byte[] transformDocument(byte[] document, String language) throws DocumentTransformationException {

        ITransformationService transformationService;
        Document resultDoc;
        TMResponseStructure tmResponse;
        byte[] result;

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("ctx_tm.xml");
        transformationService = (ITransformationService) applicationContext.getBean(ITransformationService.class.getName());

        resultDoc = byteToDocument(document);

        LOGGER.debug("STARTING TRANSLATING DOCUMENT TO: " + language + ".");
        //  Perform the translation, according to specified language.
        tmResponse = transformationService.translate(resultDoc, language);

        //  If the translation process fails, an exception is thrown.
        if (!tmResponse.isStatusSuccess()) {
            throw new DocumentTransformationException(REPOSITORY_INTERNAL_ERROR, Constants.SERVER_IP,
                    "An error has occurred during document translation, please check the following errors:\n"
                            + processErrors(tmResponse.getErrors()));
        }
        try {
            //  Obtain the translated document in the Document type format, only if translation succeeds.
            resultDoc = tmResponse.getResponseCDA();
            //  Obtains a byte array from the translation result.
            result = XMLUtils.toOM(resultDoc.getDocumentElement()).toString().getBytes("UTF-8");
        } catch (Exception ex) {
            throw new DocumentTransformationException(ex);
        }

        LOGGER.debug("TRANSLATION SUCCESSFULLY ENDED.");
        //  Return the Document as a byte array.
        return result;
    }

    /**
     * Encapsulates the TM usage, by accepting the document to translate and transcode to the pivot format.
     *
     * @param document the "friendly" document to translate/transcode, in a byte array form.
     * @return pivot document.
     * @throws DocumentTransformationException If the transformation of the CDA document producing an error not listed
     *                                         into the known potential error codes.
     */
    public static byte[] transformDocument(byte[] document) throws DocumentTransformationException {

        ITransformationService transformationService;
        Document resultDoc;
        TMResponseStructure tmResponse;
        byte[] result;

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("ctx_tm.xml");
        transformationService = (ITransformationService) applicationContext.getBean(ITransformationService.class.getName());

        resultDoc = byteToDocument(document);

        LOGGER.debug("STARTING TRANSLATING DOCUMENT TO PIVOT.");
        //  Perform the translation into pivot.
        tmResponse = transformationService.toEpSOSPivot(resultDoc);

        //  If the translation process fails, an exception is thrown.
        if (!tmResponse.isStatusSuccess()) {
            processErrors(tmResponse.getErrors());
            throw new DocumentTransformationException("DOCUMENT TRANSLATION FAILED.");
        }
        try {
            //  Obtain the translated document in the Document type format, only if translation succeeds.
            resultDoc = tmResponse.getResponseCDA();
            //  Obtains a byte array from the translation result.
            result = XMLUtils.toOM(resultDoc.getDocumentElement()).toString().getBytes("UTF-8");
        } catch (Exception ex) {
            throw new DocumentTransformationException(ex);
        }
        LOGGER.debug("TRANSLATION SUCCESSFULLY ENDED.");
        //  Return the Document as a byte array.
        return result;
    }

    /**
     * @param document CDA Document passed as byte[]
     * @return CDA Document in XML
     * @throws DocumentTransformationException If the transformation to Document failed.
     */
    public static Document byteToDocument(byte[] document) throws DocumentTransformationException {

        try {
            //Convert document byte array into a String.
            String docString = new String(document, "UTF-8");
            //Parse the String into a Document object.
            return XMLUtil.parseContent(docString);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new DocumentTransformationException(ex);
        }
    }

    /**
     * Handles errors resulted from the translation process. It currently only prints them to LOG,
     * later to be replaced with portal answer.
     *
     * @param errors List of CDA transformation errors/warnings.
     */
    private static String processErrors(List<ITMTSAMEror> errors) {

        StringBuilder sb = new StringBuilder();
        sb.append("List of errors: ");

        LOGGER.debug("TRANSLATION PROCESS ERRORS:");

        for (ITMTSAMEror error : errors) {
            LOGGER.info("Error: (Code: " + error.getCode() + ", Description: " + error.getDescription());
            sb.append("Error: (Code: ").append(error.getCode()).append(", Description: ").append(error.getDescription());
            sb.append("\n");
        }
        return sb.toString();
    }
}
