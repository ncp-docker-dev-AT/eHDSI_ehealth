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
 * This class provides data transfer services in the form of unmarshall operations. It allows the conversion of a XML
 * web service response to the correspondent set of java objects.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 * @deprecated
 */
@Deprecated
public class WsUnmarshaller {

    private static final Logger logger = LoggerFactory.getLogger(WsUnmarshaller.class);
    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(DetailedResult.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

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

                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
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
     * @param detailedResult the web-service response in the form of XML String.
     * @return a filled DetailedResult object.
     */
    public static String marshal(DetailedResult detailedResult) {

        String result = "";

        if (detailedResult == null) {
            logger.error("The provided object to marshall is null.");
        }

        try {
            StringWriter writer = new StringWriter();

            Marshaller m = jaxbContext.createMarshaller();
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
