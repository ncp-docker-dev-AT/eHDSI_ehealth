package epsos.ccd.gnomon.utils;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final String EMPTY_VALUE = "";

    private Utils() {
    }

    public static String getProperty(String key) {
        return getProperty(key, EMPTY_VALUE);
    }

    public static String getProperty(String key, String defaultValue) {
        return getProperty(key, defaultValue, false);
    }

    public static synchronized String getProperty(String key, String defaultValue, boolean persistIfNotFound) {

        try {
            String value = ConfigurationManagerFactory.getConfigurationManager().getProperty(key);
            if (isEmpty(value)) {
                value = defaultValue;
                if (persistIfNotFound) {
                    ConfigurationManagerFactory.getConfigurationManager().setProperty(key, value);
                }
            }
            return value;
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching property: '{}'", e.getMessage(), e);
            return defaultValue;
        }
    }

    public static synchronized void writeXMLToFile(String am, String path) {

        LOGGER.debug("method writeXMLToFile({})", path);
        try (FileWriter writer = new FileWriter(path); BufferedWriter out = new BufferedWriter(writer)) {
            out.write(am);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Given a string containing a xml structure, it parses it and returns the DOM object.
     *
     * @param inputFile the xml string
     * @return org.w3c.dom.Document
     */
    public static Document createDomFromString(String inputFile) {

        Document doc = null;
        // Instantiate the document to be signed
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            doc = dbFactory.newDocumentBuilder().parse(Utils.stringToStream(inputFile));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return doc;
    }

    /**
     * Validates an xml file against XMLSchema
     *
     * @param xmlDocumentUrl is the path of the tsl file
     * @return true/false if the source xml is valid against the rfc3881 xsd
     */
    public static boolean validateSchema(String xmlDocumentUrl) throws SAXException, IOException {

        InputStream streamXsd = Utils.class.getClassLoader().getResourceAsStream("RFC3881_Unsigned.xsd");
        if (streamXsd == null || streamXsd.available() == 0) {
            LOGGER.error("Cannot load XSD resource: \"RFC3881_Unsigned.xsd\"");
            return false;
        }
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        StreamSource streamSource = new StreamSource(streamXsd);
        Schema schema;
        try {
            schema = factory.newSchema(streamSource);
        } catch (SAXParseException e) {
            LOGGER.error("XML Document not valid according XSD: '{}'", e.getMessage());
            return false;
        }
        javax.xml.validation.Validator validator = schema.newValidator();
        Source source = new StreamSource(stringToStream(xmlDocumentUrl));

        try {
            validator.validate(source);
            LOGGER.debug("XML Document is valid");
            return true;
        } catch (SAXException e) {
            LOGGER.warn("XML Document not valid according XSD: '{}'", e.getMessage());
            return false;
        }
    }

    /**
     * To convert the InputStream to String we use the BufferedReader.readLine() method.
     * We iterate until the BufferedReader return null which means there's no more data to read.
     * Each line will be appended to a StringBuilder and returned as String.
     *
     * @param is - Audit message as an InputStream.
     * @return Audit message as a UTF-8 String.
     */
    public static String convertStreamToString(InputStream is) {

        StringBuilder sb = new StringBuilder();
        try {
            String line;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return sb.toString();
    }

    /**
     * Convert String to InputString using ByteArrayInputStream class. This class constructor takes the string byte
     * array which can be done by calling the getBytes() method.
     *
     * @param text - Audit message as a String
     * @return the input stream
     */
    public static InputStream stringToStream(String text) {

        InputStream is = null;
        try {
            is = new ByteArrayInputStream(text.getBytes());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return is;
    }

    /**
     * <p>
     * Creates an InputStream from a file, and fills it with the complete file.
     * Thus, available() on the returned InputStream will return the full number of bytes the file contains
     * </p>
     *
     * @param fileName The filename
     * @return The filled InputStream
     * @throws IOException , if the Streams couldn't be created.
     **/
    public static InputStream fullStream(String fileName) throws IOException {

        ByteArrayInputStream inputStream = null;

        try (FileInputStream fileInputStream = new FileInputStream(fileName); DataInputStream dis = new DataInputStream(fileInputStream)) {

            byte[] bytes = new byte[dis.available()];
            dis.readFully(bytes);
            inputStream = new ByteArrayInputStream(bytes);
            return inputStream;
        } finally {
            close(inputStream);
        }
    }

    public static void close(Closeable closeable) {

        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close.", e);
        }
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.error("Sleep interrupted: '{}'", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
