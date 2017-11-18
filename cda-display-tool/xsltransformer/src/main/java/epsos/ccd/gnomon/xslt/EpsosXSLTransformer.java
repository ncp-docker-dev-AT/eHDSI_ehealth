package epsos.ccd.gnomon.xslt;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EpsosXSLTransformer {

    private static final String MAIN_XSLT = "/resources/cda.xsl";
    private static final String STANDARD_XSLT = "/resources/def_cda.xsl";
    private static final Logger logger = LoggerFactory.getLogger(EpsosXSLTransformer.class);
    private Path path = Paths.get(System.getenv("EPSOS_PROPS_PATH"), "EpsosRepository");

    /**
     * @param xml cda xml
     * @return
     */
    public String transformUsingStandardCDAXsl(String xml) {
        return transform(xml, "en-US", null, path, true, false, STANDARD_XSLT);
    }

    /**
     * @param xml
     * @param lang
     * @param actionpath
     * @param path
     * @param export
     * @param shownarrative
     * @param xsl
     * @return
     */
    private String transform(String xml, String lang, String actionpath, Path path, boolean export,
                             boolean shownarrative, String xsl) {

        String output = "";
        checkLanguageFiles();
        logger.info("Trying to transform XML using action path for dispensation '{}' and repository path '{}' to language {}",
                actionpath, path, lang);

        try {
            URL xslUrl = this.getClass().getResource(xsl);
            InputStream xslStream = getClass().getClassLoader().getResourceAsStream("classpath*:" + xsl);

            String systemId = xslUrl.toExternalForm();
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
            if (logger.isInfoEnabled()) {
                logger.info("XSL: '{}'", xsl);
                logger.info("Main XSL: '{}'", xslUrl);
                logger.info("SystemID: '{}'", systemId);
                logger.info("Path: '{}'", path);
                logger.info("Lang: '{}'", lang);
                logger.info("Show Narrative: '{}'", String.valueOf(shownarrative));
            }
            StreamSource xmlSource = new StreamSource(new StringReader(xml));
            StreamSource xslSource = new StreamSource(xslStream);

            xslSource.setSystemId(systemId);

            TransformerFactory transformerFactory = net.sf.saxon.TransformerFactoryImpl.newInstance();
            Transformer transformer = transformerFactory.newTransformer(xslSource);
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setParameter("epsosLangDir", path);
            transformer.setParameter("userLang", lang);
            transformer.setParameter("shownarrative", String.valueOf(shownarrative));

            if (StringUtils.isNotBlank(actionpath)) {
                transformer.setParameter("actionpath", actionpath);
                transformer.setParameter("allowDispense", "true");
            } else {
                transformer.setParameter("allowDispense", "false");
            }

            File resultFile = File.createTempFile("Streams", ".html");
            Result result = new StreamResult(resultFile);
            logger.info("Temp file goes to : '{}'", resultFile.getAbsolutePath());
            transformer.transform(xmlSource, result);
            output = readFile(resultFile.getAbsolutePath());
            if (!export) {
                Files.delete(Paths.get(output));
                logger.debug("Deleting temp file '{}' successfully", resultFile.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Exception: '{}'", e.getMessage(), e);
        }
        return output;
    }

    /**
     * @param xml        the source cda xml file
     * @param lang       the language you want the labels, value set to be displayed
     * @param actionpath the url that you want to post the dispensation form. Leave it
     *                   empty to not allow dispensation
     * @param path       the path of the epsos repository files
     * @param export     whether to export file to temp folder or not
     * @return the cda document in html format
     */
    private String transform(String xml, String lang, String actionpath, Path path, boolean export) {
        return transform(xml, lang, actionpath, path, export, true, MAIN_XSLT);
    }

    /**
     * @param xml            the source cda xml file
     * @param lang           the language you want the labels, value set to be displayed
     * @param actionpath     the url that you want to post the dispensation form
     * @param repositoryPath the path of the epsos repository files
     * @return the cda document in html format
     */
    public String transform(String xml, String lang, String actionpath, Path repositoryPath) {
        return transform(xml, lang, actionpath, repositoryPath, false);
    }

    /**
     * This method uses the epsos repository files from user home directory
     *
     * @param xml        the source cda xml file
     * @param lang       the language you want the labels, value set to be displayed
     * @param actionpath the url that you want to post the dispensation form
     * @return the cda document in html format
     */
    public String transform(String xml, String lang, String actionpath) {
        return transform(xml, lang, actionpath, path, false);
    }

    /* hides links that exist in html */
    public String transformForPDF(String xml, String lang, boolean export) {
        return transform(xml, lang, "", path, export, false, MAIN_XSLT);
    }

    /**
     * This method uses the epsos repository files from user home directory and
     * outputs the transformed xml to the temp file without deleting it
     *
     * @param xml        the source cda xml file
     * @param lang       the language you want the labels, value set to be displayed
     * @param actionpath the url that you want to post the dispensation form
     * @return the cda document in html format
     */
    public String transformWithOutputAndUserHomePath(String xml, String lang, String actionpath) {
        return transform(xml, lang, actionpath, path, true);
    }

    /**
     * This method uses the epsos repository files from user home directory and
     * outputs the transformed xml to the temp file without deleting it
     *
     * @param xml            the source cda xml file
     * @param lang           the language you want the labels, value set to be displayed
     * @param actionpath     the url that you want to post the dispensation form
     * @param repositoryPath the path of the epsos repository files
     * @return the cda document in html format
     */
    public String transformWithOutputAndDefinedPath(String xml, String lang, String actionpath, Path repositoryPath) {
        return transform(xml, lang, actionpath, repositoryPath, true);
    }

    public String readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(file.toURI())));
    }

    private void checkLanguageFiles() {

//        final String filesNeeded[] = {"epSOSDisplayLabels.xml", "NullFlavor.xml", "SNOMEDCT.xml",
//                "UCUMUnifiedCodeforUnitsofMeasure.xml"};
        final String filesNeeded[] = {"1.3.6.1.4.1.12559.11.10.1.3.1.42.17.xml", "1.3.6.1.4.1.12559.11.10.1.3.1.42.37.xml",
                "1.3.6.1.4.1.12559.11.10.1.3.1.42.46.xml", "1.3.6.1.4.1.12559.11.10.1.3.1.42.16.xml"};
        // get User Path
        try {
            if (new File(path.toUri()).exists())
                for (String aFilesNeeded : filesNeeded) {
                    if (!new File(Paths.get(path.toString(), aFilesNeeded).toUri()).exists())
                        throw new Exception("File " + aFilesNeeded + " doesn't exists");
                for (String aFilesNeeded : filesNeeded) {
                    if (!new File(Paths.get(path.toString(), aFilesNeeded).toUri()).exists())
                        throw new TerminologyFileNotFoundException("File " + aFilesNeeded + " doesn't exists");
                }
            else
                throw new TerminologyFileNotFoundException("Folder " + path.toString() + " doesn't exists");
        } catch (Exception e) {
            logger.error("FATAL ERROR: " + e.getMessage(), e);
        }
    }
}
