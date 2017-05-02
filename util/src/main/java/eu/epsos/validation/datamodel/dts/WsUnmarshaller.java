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
package eu.epsos.validation.datamodel.dts;

import eu.epsos.validation.datamodel.common.DetailedResult;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;


/**
 * This class provides data transfer services in the form of unmarshall
 * operations. It allows the conversion of a XML web service response to the
 * correspondent set of java objects.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class WsUnmarshaller {

    private static final Logger logger = LoggerFactory.getLogger(WsUnmarshaller.class);

    /**
     * Private constructor to avoid instantiation.
     */
    private WsUnmarshaller() {
    }

    /**
     * This method performs an unmarshall operation with the provided XML
     * response.
     *
     * @param xmlDetails the web-service response in the form of XML String.
     * @return a filled DetailedResult object.
     */
    public static DetailedResult unmarshal(String xmlDetails) {

        DetailedResult result = null;

        if (StringUtils.isBlank(xmlDetails)) {
            logger.error("The provided XML String object to unmarshall is empty.");
        } else {
            InputStream is = new ByteArrayInputStream(xmlDetails.getBytes());

            try {

                JAXBContext jc = JAXBContext.newInstance(DetailedResult.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                result = (DetailedResult) unmarshaller.unmarshal(is);

            } catch (JAXBException ex) {
                logger.error("JAXBException: '{}'", ex.getMessage(), ex);
            }
        }
        return result;
    }

    /**
     * This method performs marshall operation with the provided object.
     *
     * @param xmlDetails the web-service response in the form of XML String.
     * @return a filled DetailedResult object.
     */
    public static String marshal(DetailedResult detailedResult) {

        String result = "";

        if (detailedResult == null) {
            logger.error("The provided object to marshall is null.");
        }

        try {
            StringWriter writer = new StringWriter();

            JAXBContext context = JAXBContext.newInstance(DetailedResult.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(detailedResult, writer);

            result = writer.toString();

        } catch (PropertyException ex) {
            logger.error(null, ex);
        } catch (JAXBException e) {
            logger.error("JAXBException: '{}'", e.getMessage(), e);
        }

        return result;
    }
}
