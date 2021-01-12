package tr.com.srdc.epsos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
import java.util.regex.Pattern;

/**
 * @author DG-Sante A4
 */
public class OidUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidUtil.class);
    // This configuration service is also responsible for accessing country code <-> OID mappings.
    private static final String PN_OID_FILE_NAME = "pn-oid.xml";
    private static final Pattern OID_PATTERN = Pattern.compile("[1-9][0-9]*(\\.(0|([1-9][0-9]*)))+");
    private static HashMap<String, String> oid2CountryCodeMap;

    static {
        loadHomeCommunityConfigurationFile();
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
     * @param countryCode 2-letter ISO code of the country, such as pt, at.
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

    public static boolean isValidHomeCommunityId(String homeCommunityId) {

        return OID_PATTERN.matcher(homeCommunityId).matches();
    }

    /**
     * @param countryOid
     * @return 2-letter ISO code of the country, but in uppercase, such as PT, AT.
     */
    public static String getCountryCodeUpperCase(String countryOid) {

        String countryCode = getCountryCode(countryOid);
        return countryCode.toUpperCase(Locale.ENGLISH);
    }

    /**
     * Loading the $OPENNCP_ROOT/pn-oid.xml configuration file containing the Member State Home Community ID.
     */
    private static void loadHomeCommunityConfigurationFile() {

        try {
            String mapFilePath = Constants.EPSOS_PROPS_PATH + PN_OID_FILE_NAME;
            oid2CountryCodeMap = new HashMap<>();
            File mapFile = new File(mapFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(mapFile);
            document.getDocumentElement().normalize();
            Element root = document.getDocumentElement();
            NodeList nodeList = root.getElementsByTagName("mapping");
            for (int i = 0; i < nodeList.getLength(); i++) {

                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {

                    Node mapping = nodeList.item(i);
                    String countryOid = mapping.getAttributes().getNamedItem("domainId").getNodeValue().trim();
                    String countryCode = mapping.getAttributes().getNamedItem("country").getNodeValue().trim();
                    oid2CountryCodeMap.put(countryOid, countryCode);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
    }

    /**
     * @param uuid
     * @return
     */
    public static String convertUuidToOid(String uuid) {

        String identifier = uuid.replace("-", "");
        BigInteger integer = new BigInteger(identifier, 16);
        return "2.25." + integer.toString();
    }
}
