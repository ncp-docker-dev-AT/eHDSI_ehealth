package eu.europa.ec.sante.ehdsi.gazelle.validation.util;

import net.ihe.gazelle.jaxb.result.sante.DetailedResult;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

public class DetailedResultUnMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(DetailedResultUnMarshaller.class);

    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(DetailedResult.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private DetailedResultUnMarshaller() {
    }

    /**
     * This method performs an unmarshall operation with the provided XML response.
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
