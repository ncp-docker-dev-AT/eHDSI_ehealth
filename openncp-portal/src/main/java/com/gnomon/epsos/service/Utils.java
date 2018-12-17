package com.gnomon.epsos.service;

import com.gnomon.epsos.MyServletContextListener;
import com.gnomon.epsos.model.Ticket;
import com.gnomon.epsos.rest.EpsosRestService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.security.Key;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {

    private static final String DATE_TIME_FORMAT_FULL = "ddMMyyyyHHmmss";
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[]{'T', 'h', '1', 'B', 'e', 's', 't', 'S', '1', '@', 'r', 'e', '$', 'K', '2', 'y'};
    private static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm";

    public static String getDocumentAsXml(org.w3c.dom.Document doc, boolean header) {

        String resp = "";
        try {
            DOMSource domSource = new DOMSource(doc);
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = tf.newTransformer();
            String omit;
            if (header) {
                omit = "no";
            } else {
                omit = "yes";
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omit);
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            java.io.StringWriter sw = new java.io.StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            resp = sw.toString();
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return resp;
    }

    public static boolean verifyTicket(String ticket) {

        String encryptionKey = MyServletContextListener.getEncryptionKey();
        LOGGER.info("Encryption KEY: '{}'", encryptionKey);
        boolean verified = false;
        try {
            LOGGER.info("PRE  String to be decrypted is '{}'", ticket);
            ticket = URLDecoder.decode(ticket, CharEncoding.UTF_8);
            LOGGER.info("POST String to be decrypted is '{}'", ticket);
            String decrypted = decrypt(ticket, encryptionKey);

            LOGGER.info("### Decrypted String is '{}'", decrypted);
            if (Validator.isNotNull(decrypted)) {
                Ticket ticket1 = stringToTicket(URLDecoder.decode(decrypted, CharEncoding.UTF_8));
                LOGGER.info("Date from ticket: " + ticket1.getCreatedDate());
                LOGGER.info("Email from ticket: " + ticket1.getEmailAddress());
                DateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT_FULL);
                Date createdDate = format.parse(ticket1.getCreatedDate() + "");
                DateTime cd = new DateTime(createdDate);
                DateTime now = new DateTime();
                long msec = Seconds.secondsBetween(cd, now).getSeconds();
                LOGGER.info("Seconds between '{}' and '{}' are '{}'", now, cd, msec);
                if (msec < 100) {
                    verified = true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Problem validating ticket", e);
            LOGGER.error("Error verifying ticket");
        }
        return verified;
    }

    private static Key generateKey() {

        return new SecretKeySpec(keyValue, ALGO);
    }

    public static String encrypt(String data) throws Exception {

        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data.getBytes());
        return java.util.Base64.getEncoder().encodeToString(encVal);
    }

    private static Key generateKeyWithKey(byte[] encryptionKey) {
        return new SecretKeySpec(encryptionKey, ALGO);
    }

    public static String decrypt(String encryptedData, String encryptionKey) {

        Key key;
        if (Validator.isNotNull(encryptionKey)) {
            byte[] bytes = encryptionKey.getBytes();
            key = generateKeyWithKey(bytes);
        } else {
            key = generateKey();
        }
        String decryptedValue = "";
        try {
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedValue = Hex.decodeHex(encryptedData.toCharArray());
            byte[] decValue = c.doFinal(decodedValue);
            decryptedValue = new String(decValue);
        } catch (Exception e) {
            LOGGER.error("Error decrypting: '{}' because of '{}'", encryptedData, e.getMessage(), e);
        }
        return decryptedValue;
    }

    public static String decrypt(String encryptedData) {

        String decryptedValue = "";
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decordedValue = java.util.Base64.getDecoder().decode(encryptedData);
            byte[] decValue = c.doFinal(decordedValue);
            decryptedValue = new String(decValue);
        } catch (Exception e) {
            LOGGER.error("Error decrypting: " + encryptedData + " " + e.getMessage());
        }
        return decryptedValue;
    }

    public static Ticket stringToTicket(String key) {

        String[] val = key.split("@@@");
        Ticket ticket = new Ticket();
        ticket.setEmailAddress(val[0]);
        ticket.setCreatedDate(val[1]);
        return ticket;
    }

    public static Ticket createTicket(long userId) throws PortalException, SystemException {

        Ticket ticket = new Ticket();
        Date d = new Date();
        String date = formatDate(d);
        User user = UserLocalServiceUtil.getUser(userId);
        String tick = userId + "@@@" + user.getEmailAddress() + "@@@" + date;

        LOGGER.info("String to be encrypted is '{}'", tick);
        String encrypted = "";
        try {
            encrypted = encrypt(tick);
        } catch (Exception ex) {
            LOGGER.error(null, ex);
        }
        LOGGER.info("Encrypted String is '{}'", encrypted);
        ticket.setCreatedDate(formatDate(new Date()));
        ticket.setEmailAddress(user.getEmailAddress());
        ticket.setTicket(encrypted);
        ticket.setUserId(userId);
        return ticket;
    }

    public static long getUserFromTicket(String ticket) {

        LOGGER.info("String to be decrypted is '{}'", ticket);
        String decrypted = "";
        try {
            decrypted = decrypt(ticket);
        } catch (Exception ex) {
            LOGGER.error(ExceptionUtils.getStackTrace(ex));
            LOGGER.error(null, ex);
        }
        LOGGER.info("Decrypted String is '{}'", decrypted);
        Ticket ticket1 = stringToTicket(decrypted);

        LOGGER.info("Username is '{}' and date is '{}'", ticket1.getUserId(), ticket1.getCreatedDate());
        return ticket1.getUserId();
    }

    public static boolean verifyTicket(String ticket, String username) {

        LOGGER.info("String to be decrypted is '{}'", ticket);
        String decrypted = "";
        try {
            decrypted = decrypt(ticket);
        } catch (Exception ex) {
            LOGGER.error(null, ex);
        }
        LOGGER.info("Decrypted String is '{}'", decrypted);
        Ticket ticket1 = stringToTicket(decrypted);

        long userId = ticket1.getUserId();
        LOGGER.info("USER FROM TICKET IS '{}' and user wants to be verified is '{}'", userId, username);
        boolean ret = false;
        try {
            User user = UserLocalServiceUtil.getUser(userId);
            LOGGER.info("USER FROM TICKET IS '{}'", user.getScreenName());
            if (user.getScreenName().equalsIgnoreCase(username)) {
                ret = true;
            }
        } catch (Exception e) {
            LOGGER.error("Error finding user for user inside ticket '{}'", e.getMessage(), e);
        }
        return ret;
    }

    public static String formatDate(Date date) {

        String formatted = "";
        if (date != null) {
            SimpleDateFormat dt1 = new SimpleDateFormat(DATE_TIME_FORMAT);
            dt1.setTimeZone(TimeZone.getTimeZone("EET"));

            formatted = dt1.format(date);
        }
        return formatted;
    }

    public static String transformDomToString(Document doc) throws TransformerException {

        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }

    public static Document readXml(StreamSource is) throws SAXException, IOException, ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(true);

        DocumentBuilder db;
        db = dbf.newDocumentBuilder();
        db.setEntityResolver(new NullResolver());

        InputSource is2 = new InputSource();
        is2.setSystemId(is.getSystemId());
        is2.setByteStream(is.getInputStream());
        is2.setCharacterStream(is.getReader());

        return db.parse(is2);
    }

    public boolean isValidUser(String user) throws Exception {

        // Not used method: <code>URLDecoder.decode(user, "UTF-8")</code>
        int userId = Integer.parseInt(Utils.decrypt(user));
        LOGGER.info("#### Encrypted userid '{}'", user);
        LOGGER.info("#### Decrypted userid '{}'", userId);
        return Validator.isNotNull(UserLocalServiceUtil.getUser(userId));
    }

    public User getLiferayUserUnEncrypted(String user) throws Exception {

        User liferayUser = UserLocalServiceUtil.getUserByScreenName(EpsosRestService.COMPANY_ID, user);
        if (Validator.isNotNull(liferayUser)) {
            return liferayUser;
        }
        return null;
    }

    public User getLiferayUser(String user) throws Exception {

        int userId = Integer.parseInt(Utils.decrypt(user));
        User liferayUser = UserLocalServiceUtil.getUser(userId);
        if (Validator.isNotNull(liferayUser)) {
            return liferayUser;
        }
        return null;
    }

    public String generateSalt() {

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return new String(bytes);
    }
}

class NullResolver implements EntityResolver {

    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new StringReader(""));
    }
}
