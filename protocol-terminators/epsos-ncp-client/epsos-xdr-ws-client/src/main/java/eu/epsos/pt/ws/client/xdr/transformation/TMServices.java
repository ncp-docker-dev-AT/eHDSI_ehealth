package eu.epsos.pt.ws.client.xdr.transformation;

import epsos.ccd.posam.tm.response.TMResponseStructure;
import epsos.ccd.posam.tm.service.ITransformationService;
import eu.europa.ec.sante.ehdsi.constant.error.ITMTSAMEror;
import eu.epsos.exceptions.DocumentTransformationException;
import eu.europa.ec.sante.ehdsi.constant.error.OpenncpErrorCode;
import org.apache.axis2.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Encapsulates all the usage of Transformation Manager for transcoding or translation processes.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public final class TMServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(TMServices.class);

    private TMServices() {
    }

    /**
     * Encapsulates the TM usage, by accepting the document to translate and transcode to the pivot format.
     *
     * @param document the "friendly" document to translate/transcode, in a byte array form.
     * @return pivot document.
     * @throws DocumentTransformationException
     */
    public static byte[] transformDocument(byte[] document) throws DocumentTransformationException {

        ITransformationService transformationService;
        Document resultDoc;
        TMResponseStructure tmResponse;
        byte[] result;

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("ctx_tm.xml");
        transformationService = (ITransformationService) applicationContext.getBean(ITransformationService.class.getName());

        resultDoc = byteToDocument(document);

        LOGGER.debug("STARTING TRANSCODING DOCUMENT TO PIVOT.");

        tmResponse = transformationService.toEpSOSPivot(resultDoc); //Perform the translation into pivot.

        if (!tmResponse.isStatusSuccess()) {
            processErrors(tmResponse.getErrors());
            //If the transcoding process fails, an exception is thrown.
            throw new DocumentTransformationException(OpenncpErrorCode.ERROR_ED_MISSING_EXPECTED_MAPPING, OpenncpErrorCode.ERROR_ED_MISSING_EXPECTED_MAPPING.getDescription(), "DOCUMENT TRANSCODING FAILED.");
        }
        try {
            // Obtain the translated document in the Document type format, only if translation succeeds.
            resultDoc = tmResponse.getResponseCDA();
            //Obtains a byte array from the translation result.
            result = XMLUtils.toOM(resultDoc.getDocumentElement()).toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new DocumentTransformationException(OpenncpErrorCode.ERROR_GENERIC, ex.getMessage(), ex.getMessage());
        }

        LOGGER.debug("TRANSCODING SUCCESSFULLY ENDED.");
        //  Return the Document as a byte array.
        return result;
    }

    public static Document byteToDocument(byte[] document) throws DocumentTransformationException {

        Document resultDoc;

        //Convert document byte array into a String.
        String docString = new String(document, StandardCharsets.UTF_8);

        try {
            //Parse the String into a Document object.
            resultDoc = XMLUtil.parseContent(docString);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new DocumentTransformationException(OpenncpErrorCode.ERROR_GENERIC, ex.getMessage(), ex.getMessage());
        }

        return resultDoc;
    }

    /**
     * Handles errors resulted from the translation process.
     * It currently only prints them to LOG, later to be replaced with portal answer.
     *
     * @param errors
     */
    private static void processErrors(List<ITMTSAMEror> errors) {

        LOGGER.debug("TRANSLATION PROCESS ERRORS:/n");

        for (ITMTSAMEror error : errors) {
            LOGGER.info("Error: (Code: " + error.getCode() + ", Description: " + error.getDescription());
        }
    }
}
