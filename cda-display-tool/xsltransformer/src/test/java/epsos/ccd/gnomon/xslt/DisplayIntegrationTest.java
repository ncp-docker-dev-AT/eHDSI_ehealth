package epsos.ccd.gnomon.xslt;

import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@RunWith(JUnit4.class)
@Ignore("Test to revise - Exclude unit test from test execution")
public class DisplayIntegrationTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisplayTest.class);
    private static final String EPSOS_PROPS_ENV_PROPERTY = "EPSOS_PROPS_PATH";

    @BeforeClass
    public static void init() {

        String envVar = System.getenv(EPSOS_PROPS_ENV_PROPERTY);
        assertNotNull("Environment variable '" + EPSOS_PROPS_ENV_PROPERTY + "' required to exist to run the tests", envVar);
        assertTrue("Environment variable '" + EPSOS_PROPS_ENV_PROPERTY + "' required to have a value to run the tests", envVar.trim().length() > 0);
    }


    @Test
    public void runFolder() {

        Path path = Paths.get("samples");
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isDirectory()) {
                        String input = file.toString();
                        LOGGER.info("Transforming file: " + input);

                        String cda = "";
                        try {
                            cda = CdaXSLTransformer.getInstance().readFile(input);
                        } catch (Exception e) {
                            LOGGER.error("File not found");
                        }
                        String out = "";
                        try {
                        switch (TRANSFORMATION.WithOutputAndUserHomePath) {
                            case ForPDF:
                                out = CdaXSLTransformer.getInstance().transformForPDF(cda, "el_GR", false);
                                break;
                            case UsingStandardCDAXsl:
                                out = CdaXSLTransformer.getInstance().transformUsingStandardCDAXsl(cda);
                                break;
                            case WithOutputAndDefinedPath:
                                out = CdaXSLTransformer.getInstance().transformWithOutputAndDefinedPath(cda, "el_GR", "",
                                        Paths.get(System.getenv(EPSOS_PROPS_ENV_PROPERTY), "EpsosRepository"));
                                break;
                            case WithOutputAndUserHomePath:
                                out = CdaXSLTransformer.getInstance().transformWithOutputAndUserHomePath(cda, "el_GR", "");
                                break;
                        }
                        } catch (Exception e) {
                            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
                            fail("IOException while executing the folderTest");
                        }
                        String filename = Paths.get(input).getFileName().toString();
                        String stripExt = filename.substring(0, filename.lastIndexOf("."));
                        String pt = Paths.get(System.getenv(EPSOS_PROPS_ENV_PROPERTY), "EpsosRepository", "out", stripExt + ".html")
                                .toString();
                        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pt), StandardCharsets.UTF_8))) {
                            writer.write(out);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
            fail("IOException while executing the folderTest");
        }
    }

    @Test
    public void runFile() throws Exception {

        // Vaccination
        // fileTest("samples/cda_xml_157.xml");
        // fileTest("samples/epSOS_MRO_test_full.xml");
        // fileTest("samples/epSOS_RTD_PS_EU_Pivot_CDA_Paolo.xml");
        // fileTest("samples/es_ps_pivot.xml");

        // Frequency
        String input = "samples/multiingredient.xml";
        LOGGER.info("Transforming file: " + input);

        String cda = "";
        try {
            cda = CdaXSLTransformer.getInstance().readFile(input);
        } catch (Exception e) {
            LOGGER.error("File not found");
        }
        String out = "";
        switch (TRANSFORMATION.WithOutputAndUserHomePath) {
            case ForPDF:
                out = CdaXSLTransformer.getInstance().transformForPDF(cda, "el_GR", false);
                break;
            case UsingStandardCDAXsl:
                out = CdaXSLTransformer.getInstance().transformUsingStandardCDAXsl(cda);
                break;
            case WithOutputAndDefinedPath:
                out = CdaXSLTransformer.getInstance().transformWithOutputAndDefinedPath(cda, "el_GR", "",
                        Paths.get(System.getenv(EPSOS_PROPS_ENV_PROPERTY), "EpsosRepository"));
                break;
            case WithOutputAndUserHomePath:
                out = CdaXSLTransformer.getInstance().transformWithOutputAndUserHomePath(cda, "el_GR", "");
                break;
        }
        String filename = Paths.get(input).getFileName().toString();
        String stripExt = filename.substring(0, filename.lastIndexOf("."));
        String pt = Paths.get(System.getenv(EPSOS_PROPS_ENV_PROPERTY), "EpsosRepository", "out", stripExt + ".html").toString();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pt), StandardCharsets.UTF_8))) {
            writer.write(out);
        }

        // xlsClass.transformForPDF(cda, "el-GR",true);
    }

    @Test
    public void readFile() throws Exception {

        String out = CdaXSLTransformer.getInstance().readFile("samples/multiingredient.xml");
        String pt = Paths.get(System.getenv(EPSOS_PROPS_ENV_PROPERTY), "EpsosRepository", "out", "readfile.txt").toString();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pt), StandardCharsets.UTF_8))) {
            writer.write(out);
        }
    }

    private enum TRANSFORMATION {

        WithOutputAndUserHomePath, ForPDF, UsingStandardCDAXsl, WithOutputAndDefinedPath
    }
}
