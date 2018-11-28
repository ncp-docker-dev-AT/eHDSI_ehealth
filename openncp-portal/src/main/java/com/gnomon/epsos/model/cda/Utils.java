package com.gnomon.epsos.model.cda;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipInputStream;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final int BUFFER_SIZE = 4096;

    private Utils() {
    }

    public static String checkString(Object s) {

        String refStr = " ";
        try {
            refStr = s.toString();
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return refStr;
    }

    private static String escapeHTML(String s) {

        StringBuilder sb = new StringBuilder();
        try {

            int n = s.length();
            for (int i = 0; i < n; i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '<':
                        sb.append("&lt;");
                        break;
                    case '>':
                        sb.append("&gt;");
                        break;
                    case '&':
                        sb.append("&amp;");
                        break;
                    case '"':
                        sb.append("&quot;");
                        break;
                    case 'à':
                        sb.append("&agrave;");
                        break;
                    case 'À':
                        sb.append("&Agrave;");
                        break;
                    case 'â':
                        sb.append("&acirc;");
                        break;
                    case 'Â':
                        sb.append("&Acirc;");
                        break;
                    case 'ä':
                        sb.append("&auml;");
                        break;
                    case 'Ä':
                        sb.append("&Auml;");
                        break;
                    case 'å':
                        sb.append("&aring;");
                        break;
                    case 'Å':
                        sb.append("&Aring;");
                        break;
                    case 'æ':
                        sb.append("&aelig;");
                        break;
                    case 'Æ':
                        sb.append("&AElig;");
                        break;
                    case 'ç':
                        sb.append("&ccedil;");
                        break;
                    case 'Ç':
                        sb.append("&Ccedil;");
                        break;
                    case 'é':
                        sb.append("&eacute;");
                        break;
                    case 'É':
                        sb.append("&Eacute;");
                        break;
                    case 'è':
                        sb.append("&egrave;");
                        break;
                    case 'È':
                        sb.append("&Egrave;");
                        break;
                    case 'ê':
                        sb.append("&ecirc;");
                        break;
                    case 'Ê':
                        sb.append("&Ecirc;");
                        break;
                    case 'ë':
                        sb.append("&euml;");
                        break;
                    case 'Ë':
                        sb.append("&Euml;");
                        break;
                    case 'ï':
                        sb.append("&iuml;");
                        break;
                    case 'Ï':
                        sb.append("&Iuml;");
                        break;
                    case 'ô':
                        sb.append("&ocirc;");
                        break;
                    case 'Ô':
                        sb.append("&Ocirc;");
                        break;
                    case 'ö':
                        sb.append("&ouml;");
                        break;
                    case 'Ö':
                        sb.append("&Ouml;");
                        break;
                    case 'ø':
                        sb.append("&oslash;");
                        break;
                    case 'Ø':
                        sb.append("&Oslash;");
                        break;
                    case 'ß':
                        sb.append("&szlig;");
                        break;
                    case 'ù':
                        sb.append("&ugrave;");
                        break;
                    case 'Ù':
                        sb.append("&Ugrave;");
                        break;
                    case 'û':
                        sb.append("&ucirc;");
                        break;
                    case 'Û':
                        sb.append("&Ucirc;");
                        break;
                    case 'ü':
                        sb.append("&uuml;");
                        break;
                    case 'Ü':
                        sb.append("&Uuml;");
                        break;
                    case '®':
                        sb.append("&reg;");
                        break;
                    case '©':
                        sb.append("&copy;");
                        break;
                    case '€':
                        sb.append("&euro;");
                        break;
                    default:
                        sb.append(c);
                        break;
                }

            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return sb.toString();
    }

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
            // we want to pretty format the XML output
            // note : this is broken in jdk1.5 beta!
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            java.io.StringWriter sw = new java.io.StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            resp = sw.toString();
        } catch (Exception e) {
            LOGGER.error("Problem getting xml as dom");
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return resp;
    }

    public static String getDocumentAsXml(org.w3c.dom.Document doc) {

        String resp = "";
        if (doc != null) {
            resp = getDocumentAsXml(doc, true);
        }
        return resp;

    }

    public static String nodeToString(Node node) {

        StringWriter sw = new StringWriter();
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer t = transformerFactory.newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException e) {
            LOGGER.debug("nodeToString Transformer Exception: '{}'", e.getMessage(), e);
        }
        return sw.toString();
    }

    public static org.w3c.dom.Document resultSetToXML(java.sql.ResultSet rs) {

        org.w3c.dom.Document doc = null;
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            org.w3c.dom.Element results = doc.createElement("NewDataSet");
            doc.appendChild(results);

            int rowCount = 0;
            while (rs.next()) {
                org.w3c.dom.Element row = doc.createElement("Table");
                rowCount++;
                results.appendChild(row);
                for (int i = 1; i <= colCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    Object value = rs.getObject(i);
                    org.w3c.dom.Element node = doc.createElement(columnName);
                    String valueStr = "";
                    try {
                        valueStr = escapeHTML(value.toString());
                    } catch (Exception e) {
                        LOGGER.error("Error getting content for column: '{}'. Error: '{}'", columnName, e.getMessage(), e);
                    }
                    node.appendChild(doc.createTextNode(valueStr));
                    row.appendChild(node);
                }
            }
            if (rowCount < 1) {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Problem converting sql to xml '{}'", e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return doc;
    }

    public static InputStream stringToStream(String text) {

        InputStream is = null;
        try {
            is = new ByteArrayInputStream(text.getBytes());
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return is;
    }

    public static Document createDomFromString(String inputFile) {

        Document doc = null;
        // Instantiate the document to be signed
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            doc = dbFactory.newDocumentBuilder().parse(stringToStream(inputFile));

        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return doc;
    }

    /*
     *
     * <CodeSystem oid="2" displayName="EOF">
     <ValueSet oid="2" displayName="EOF">
     <Entry code="125890301" displayName="LEXOTANIL"/>
     <Entry code="196340101" displayName="ZIRTEK"/>
     <Entry code="076130401" displayName="FLAGYL"/>
     </ValueSet>
     </CodeSystem>
     *
     */
    public static org.w3c.dom.Document resultSetToXMLWithAttr(Document doc, Element elem, java.sql.ResultSet rs,
                                                              String oid, String displayName) throws SQLException {

        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount = rsmd.getColumnCount();

        org.w3c.dom.Element results = doc.createElement("CodeSystem");
        results.setAttribute("oid", oid);
        results.setAttribute("displayName", displayName);
        elem.appendChild(results);
        org.w3c.dom.Element row = doc.createElement("ValueSet");
        row.setAttribute("oid", oid);
        row.setAttribute("displayName", displayName);

        while (rs.next()) {

            results.appendChild(row);
            org.w3c.dom.Element node;
            node = doc.createElement("Entry");
            for (int i = 1; i <= colCount; i++) {
                String columnName = rsmd.getColumnName(i);
                Object value = rs.getObject(i);
                String valueStr;
                valueStr = escapeHTML(value.toString());
                node.setAttribute(columnName, valueStr);
            }
            row.appendChild(node);
        }
        return doc;
    }

    public static void writeXMLToFile(String xml, String filename) {

        try (FileWriter fstream = new FileWriter(filename)) {
            // Create file
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(xml);
            //Close the output stream
            out.close();

        } catch (Exception e) {//Catch exception if any
            LOGGER.error("Error: '{}'", e.getMessage(), e);
        }
    }

    /**
     * To convert the InputStream to String we use the Reader.read(char[] buffer) method.
     * We iterate until the Reader return -1 which means there's no more data to read.
     * We use the StringWriter class to produce the string.
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static String convertStreamToString(InputStream is) throws IOException {

        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try (Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    public static String getCDA(String base64String) throws IOException {

        ByteArrayOutputStream bazip = new ByteArrayOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        InputStream input = new Base64InputStream(new ByteArrayInputStream(base64String.getBytes()));
        int n = input.read(buffer, 0, BUFFER_SIZE);
        while (n >= 0) {
            bazip.write(buffer, 0, n);
            n = input.read(buffer, 0, BUFFER_SIZE);
        }
        input.close();
        // read zip file
        ByteArrayOutputStream contents = new ByteArrayOutputStream();
        try {
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bazip.toByteArray()));
            while ((zis.getNextEntry()) != null) {

                byte[] buf = new byte[4096];
                int len;

                while ((len = zis.read(buf)) > 0) {
                    contents.write(buf, 0, len);
                }

            }
            zis.close();

        } catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return contents.toString();

    }

    public static String getZipPostData(ServletRequest req) throws IOException {

        try (InputStream inputStream = req.getInputStream()) {

            OutputStream out = new FileOutputStream(new File("/home/newfile.zip"));
            IOUtils.copy(inputStream, out);
            ByteArrayOutputStream contents = new ByteArrayOutputStream();
            byte[] buf1 = new byte[1024];
            int letti;

            while ((letti = inputStream.read(buf1)) > 0) {
                contents.write(buf1, 0, letti);
            }

            try {
                ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(contents.toByteArray()));
                while ((zis.getNextEntry()) != null) {

                    byte[] buf = new byte[4096];
                    int len;

                    while ((len = zis.read(buf)) > 0) {
                        contents.write(buf, 0, len);
                    }
                }//while

                zis.close();

            } catch (IOException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
            return contents.toString();
        }
    }

    public static String getPostData(ServletRequest req) {

        String workString = "";
        try {
            BufferedReader b = new BufferedReader(req.getReader());
            StringBuilder workBuffer = new StringBuilder();
            while ((workString = b.readLine()) != null) {
                workBuffer.append(workString);
            }
            workString = workBuffer.toString();

        } catch (IOException e1) {
            LOGGER.error(ExceptionUtils.getStackTrace(e1));
        }
        return workString;
    }

    public static InetAddress remoteIp(final HttpServletRequest request) throws UnknownHostException {

        if (request.getHeader("x-forwarded-for") != null) {
            return InetAddress.getByName(request.getHeader("x-forwarded-for"));
        }

        return InetAddress.getByName(request.getRemoteAddr());
    }

    public static String encodePDF(byte[] file) {

        String encodedBytes = "";
        try {
            encodedBytes = Base64.encodeBase64String(file);
            LOGGER.warn("encodedBytes '{}'", encodedBytes);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return encodedBytes;
    }

    public static void decodePDF(String file) {

        byte[] decodedBytes;
        try (FileOutputStream fos = new FileOutputStream("/home/karkaletsis/Documents/test.pdf");) {
            decodedBytes = Base64.decodeBase64(file);
            fos.write(decodedBytes);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public static String formatDateHL7(Date date) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return formatter.format(date);
    }

    public static String convertDateWithPattern(String dateStr, String format, String pattern) {

        String newstring = "";
        Date date;
        try {
            date = new SimpleDateFormat(pattern).parse(dateStr);
            newstring = new SimpleDateFormat(format).format(date);
        } catch (ParseException e) {
            LOGGER.error("Error convertng date " + dateStr + " with format " + format + " and pattern " + pattern);
        }
        return newstring;
    }

    public static String convertDate(String dateStr, String format) {

        String retDate = "19700101";
        String pattern = "yyyyMMdd";
        try {
            retDate = convertDateWithPattern(dateStr, format, pattern);
        } catch (Exception e) {
            LOGGER.error("Error converting date '{}'", dateStr, e);
        }
        return retDate;
    }

    public static byte[] readFully(InputStream stream) throws IOException {

        byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    public static byte[] loadFile(String sourcePath) throws IOException {

        try (InputStream inputStream = new FileInputStream(sourcePath)) {

            return readFully(inputStream);
        }
    }
}
