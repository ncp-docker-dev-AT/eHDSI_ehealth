/*
 * This file is part of epSOS OpenNCP implementation
 * Copyright (C) 2012  SPMS (Serviços Partilhados do Ministério da Saúde - Portugal)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contact email: epsos@iuz.pt
 */
package eu.epsos.pt.ws.client.xdr.transformation;

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
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Encapsulates all the usage of Transformation Manager for transcoding or
 * translation processes.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public final class TMServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(TMServices.class);

    private TMServices() {
    }

    /**
     * Encapsulates the TM usage, by accepting the document to translate and
     * transcode to the pivot format.
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

        LOGGER.debug("STARTING TRANSLATING DOCUMENT TO PIVOT.");

        tmResponse = transformationService.toEpSOSPivot(resultDoc); //Perform the translation into pivot.

        if (!tmResponse.isStatusSuccess()) {
            processErrors(tmResponse.getErrors());
            throw new DocumentTransformationException("DOCUMENT TRANSLATION FAILED."); //If the translation process fails, an exception is thrown.
        }
        try {
            // Obtain the translated document in the Document type format, only if translation succeeds.
            resultDoc = tmResponse.getResponseCDA();
            //Obtains a byte array from the translation result.
            result = XMLUtils.toOM(resultDoc.getDocumentElement()).toString().getBytes("UTF-8");
        } catch (Exception ex) {
            throw new DocumentTransformationException(ex);
        }

        LOGGER.debug("TRANSLATION SUCCESSFULLY ENDED.");

        return result; //Return the Document as a byte array.
    }

    public static Document byteToDocument(byte[] document) throws DocumentTransformationException {

        String docString;
        Document resultDoc = null;

        try {
            docString = new String(document, "UTF-8"); //Convert document byte array into a String.
        } catch (UnsupportedEncodingException ex) {
            throw new DocumentTransformationException(ex);
        }

        try {
            resultDoc = XMLUtil.parseContent(docString); //Parse the String into a Document object.
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new DocumentTransformationException(ex);
        }

        return resultDoc;
    }

    /**
     * Handles errors resulted from the translation process. It currently only
     * prints them to LOG, later to be replaced with portal anwser.
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
