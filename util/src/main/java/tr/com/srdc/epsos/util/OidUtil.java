package tr.com.srdc.epsos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author DG-Sante A4
 */
public class OidUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidUtil.class);
    // This configuration service is also responsible for accessing country code
    // <-> OID mappings.
    private static final String PN_OID_FILE_NAME = "pn-oid.xml";
    private static HashMap<String, String> oid2CountryCodeMap;

    static {
        readCountryOid2CodeMappingFile();
    }

    private OidUtil() {
    }

    /**
     * @param countryOid foreign Home Community Id
     * @return 2-letter ISO code of the country, such as tr, pt, at.
     */
    public static String getCountryCode(String countryOid) {
        return oid2CountryCodeMap.get(countryOid);
    }

    /**
     * Converts a country code into a HomeCommunityId
     *
     * @param countryCode 2-letter ISO code of the country, such as tr, pt, at.
     * @return foreign HomeCommunityId
     */
    public static String getHomeCommunityId(String countryCode) {
        for (Map.Entry<String, String> entry : oid2CountryCodeMap.entrySet()) {
            if (entry.getValue().equals(countryCode)) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * @param countryOid
     * @return 2-letter ISO code of the country, but in uppercase, such as TR,
     * PT, AT.
     */
    public static String getCountryCodeUpperCase(String countryOid) {

        String countryCode = getCountryCode(countryOid);
        return countryCode.toUpperCase(Locale.ENGLISH);
    }

    /**
     *
     */
    private static void readCountryOid2CodeMappingFile() {

        DocumentBuilder dBuilder;
        Document doc;

        oid2CountryCodeMap = new HashMap<>();
        String mapFilePath = Constants.EPSOS_PROPS_PATH + PN_OID_FILE_NAME;

        File mapFile = new File(mapFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(mapFile);
            doc.getDocumentElement().normalize();
            Node mappings = doc.getDocumentElement();

            NodeList nodeList = mappings.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Node mapping = nodeList.item(i);

                    String countryOid = mapping.getAttributes().getNamedItem("domainId").getNodeValue().trim();
                    String countryCode = mapping.getAttributes().getNamedItem("country").getNodeValue().trim();

                    oid2CountryCodeMap.put(countryOid, countryCode);
                }
            }
        } catch (ParserConfigurationException e) {
            LOGGER.error("ParserConfigurationException: '{}'", e.getMessage(), e);
        } catch (SAXException e) {
            LOGGER.error("SAXException: '{}'", e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error("IOException: '{}'", e.getMessage(), e);
        }
    }

    /**
     * @param uuid
     * @return
     */
    public static String convertUuidToOid(String uuid) {

        String identifier = uuid.replaceAll("-", "");
        BigInteger integer = new BigInteger(identifier, 16);
        return "2.25." + integer.toString();
    }
}
