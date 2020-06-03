package epsos.ccd.gnomon.xslt;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
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

public class CdaXSLTransformer {

    private static final String MAIN_XSLT = "/resources/cda.xsl";
    private static final String STANDARD_XSLT = "/resources/def_cda.xsl";
    private static final Path PATH = Paths.get(System.getenv("EPSOS_PROPS_PATH"), "EpsosRepository");
    private static final Logger LOGGER = LoggerFactory.getLogger(CdaXSLTransformer.class);
    private static CdaXSLTransformer instance = null;

    private CdaXSLTransformer() {
        // Private constructor of the singleton.
    }

    public static synchronized CdaXSLTransformer getInstance() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Getting Instance of CdaXSLTransformer...");
        }
        if (instance == null) {
            checkLanguageFiles();
            instance = new CdaXSLTransformer();
        }
        return instance;
    }

    /**
     * Util method checking if the eHDSI Translation Repository has been initialized and the number of files available.
     */
    private static void checkLanguageFiles() {

        try {
            File file = new File(PATH.toUri());
            if (file.exists() && file.isDirectory()) {

                String[] files = file.list();
                if (files == null || files.length == 0) {
                    throw new TerminologyFileNotFoundException("Files containing translations are not available into the folder: " + PATH.toUri());
                } else {
                    int count = 0;
                    for (String fileName : files) {

                        if (new File(Paths.get(PATH.toString(), fileName).toUri()).exists()) {
                            count++;
                        }
                    }
                    LOGGER.info("eHDSI Translation repository initialized with '{}' files", count);
                }
            } else {
                throw new TerminologyFileNotFoundException("Folder " + PATH.toString() + " doesn't exists");
            }
        } catch (Exception e) {
            LOGGER.error("FATAL ERROR: '{}'", e.getMessage(), e);
        }
    }

    /**
     * @param xml cda xml
     * @return
     */
    public String transformUsingStandardCDAXsl(String xml) {
        return transform(xml, "en-US", null, PATH, true, false, STANDARD_XSLT);
    }

    /**
     * @param xml
     * @param lang
     * @param actionPath
     * @param path
     * @param export
     * @param showNarrative
     * @param xsl
     * @return
     */
    private String transform(String xml, String lang, String actionPath, Path path, boolean export,
                             boolean showNarrative, String xsl) {

        String output = "";
        LOGGER.info("Trying to transform XML using action path for dispensation '{}' and repository path '{}' to language {}",
                actionPath, path, lang);

        try {
            URL xslUrl = this.getClass().getResource(xsl);
            InputStream xslStream = getClass().getClassLoader().getResourceAsStream("classpath*:" + xsl);

            String systemId = xslUrl.toExternalForm();
            System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("XSL: '{}'", xsl);
                LOGGER.info("Main XSL: '{}'", xslUrl);
                LOGGER.info("SystemID: '{}'", systemId);
                LOGGER.info("Path: '{}'", path);
                LOGGER.info("Lang: '{}'", lang);
                LOGGER.info("Show Narrative: '{}'", showNarrative);
            }

            StreamSource xmlSource = new StreamSource(new StringReader(xml));
            StreamSource xslSource = new StreamSource(xslStream);
            xslSource.setSystemId(systemId);

            TransformerFactory transformerFactory = net.sf.saxon.TransformerFactoryImpl.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer(xslSource);
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setParameter("epsosLangDir", path);
            transformer.setParameter("userLang", lang);
            transformer.setParameter("shownarrative", String.valueOf(showNarrative));

            if (StringUtils.isNotBlank(actionPath)) {
                transformer.setParameter("actionpath", actionPath);
                transformer.setParameter("allowDispense", "true");
            } else {
                transformer.setParameter("allowDispense", "false");
            }

            File resultFile = File.createTempFile("Streams", ".html");
            Result result = new StreamResult(resultFile);
            LOGGER.info("Temp file goes to : '{}'", resultFile.getAbsolutePath());
            transformer.transform(xmlSource, result);
            output = readFile(resultFile.getAbsolutePath());
            if (!export) {
                Files.delete(Paths.get(resultFile.getCanonicalPath()));
                LOGGER.debug("Deleting temp file '{}' successfully", resultFile.getAbsolutePath());
            }
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return output;
    }

    /**
     * @param xml        the source cda xml file
     * @param lang       the language you want the labels, value set to be displayed
     * @param actionPath the url that you want to post the dispensation form. Leave it empty to not allow dispensation
     * @param path       the path of the repository containing files including translations.
     * @param export     whether to export file to temp folder or not
     * @return the cda document in html format
     */
    private String transform(String xml, String lang, String actionPath, Path path, boolean export) {
        return transform(xml, lang, actionPath, path, export, true, MAIN_XSLT);
    }

    /**
     * @param xml            the source cda xml file
     * @param lang           the language you want the labels, value set to be displayed
     * @param actionPath     the url that you want to post the dispensation form
     * @param repositoryPath the path of the epsos repository files
     * @return the cda document in html format
     */
    public String transform(String xml, String lang, String actionPath, Path repositoryPath) {
        return transform(xml, lang, actionPath, repositoryPath, false);
    }

    /**
     * This method uses the epsos repository files from user home directory
     *
     * @param xml        the source cda xml file
     * @param lang       the language you want the labels, value set to be displayed
     * @param actionPath the url that you want to post the dispensation form
     * @return the cda document in html format
     */
    public String transform(String xml, String lang, String actionPath) {
        return transform(xml, lang, actionPath, PATH, false);
    }

    /* hides links that exist in html */
    public String transformForPDF(String xml, String lang, boolean export) {
        return transform(xml, lang, "", PATH, export, false, MAIN_XSLT);
    }

    /**
     * This method uses the epsos repository files from user home directory and outputs the transformed xml to the temp
     * file without deleting it.
     *
     * @param xml        the source cda xml file
     * @param lang       the language you want the labels, value set to be displayed
     * @param actionPath the url that you want to post the dispensation form
     * @return the cda document in html format
     */
    public String transformWithOutputAndUserHomePath(String xml, String lang, String actionPath) {
        return transform(xml, lang, actionPath, PATH, true);
    }

    /**
     * This method uses the epsos repository files from user home directory and outputs the transformed xml to the temp
     * file without deleting it
     *
     * @param xml            the source cda xml file
     * @param lang           the language you want the labels, value set to be displayed
     * @param actionPath     the url that you want to post the dispensation form
     * @param repositoryPath the path of the epsos repository files
     * @return the cda document in html format
     */
    public String transformWithOutputAndDefinedPath(String xml, String lang, String actionPath, Path repositoryPath) {
        return transform(xml, lang, actionPath, repositoryPath, true);
    }

    public String readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(file.toURI())));
    }
}
