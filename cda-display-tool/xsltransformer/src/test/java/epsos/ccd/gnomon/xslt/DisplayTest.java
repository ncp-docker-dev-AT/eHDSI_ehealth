package epsos.ccd.gnomon.xslt;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author karkaletsis
 */
public class DisplayTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisplayTest.class);

    //	@Test
    private void fileTest(String input, TRANSFORMATION type) throws IOException {

        EpsosXSLTransformer xlsClass = new EpsosXSLTransformer();
        LOGGER.info("Transforming file: " + input);

        String cda = "";
        try {
            cda = xlsClass.readFile(input);
        } catch (Exception e) {
            LOGGER.error("File not found");
        }
        String out = "";
        switch (type) {
            case ForPDF:
                out = xlsClass.transformForPDF(cda, "fr-BE", false);
                break;
            case UsingStandardCDAXsl:
                out = xlsClass.transformUsingStandardCDAXsl(cda);
            case WithOutputAndDefinedPath:
                out = xlsClass.transformWithOutputAndDefinedPath(cda, "fr-BE", "", Paths.get(System.getenv("EPSOS_PROPS_PATH"), "EpsosRepository"));
            case WithOutputAndUserHomePath:
                out = xlsClass.transformWithOutputAndUserHomePath(cda, "fr-BE", "");

        }
        String filename = Paths.get(input).getFileName().toString();
        String stripExt = filename.substring(0, filename.lastIndexOf("."));
        String pt = Paths.get(System.getenv("EPSOS_PROPS_PATH"), "EpsosRepository", "out", stripExt + ".html")
                .toString();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pt), "utf-8"))) {
            writer.write(out);
        }

        // xlsClass.transformForPDF(cda, "el-GR",true);
    }

    //	@Test
    private void folderTest(String input, final TRANSFORMATION type) {
        Path path = Paths.get(input);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isDirectory()) {
                        fileTest(file.toString(), type);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
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
        //fileTest("samples/multiingredient.xml", TRANSFORMATION.WithOutputAndUserHomePath);
        //fileTest("samples/2-4567.xml", TRANSFORMATION.WithOutputAndUserHomePath);
        fileTest("samples/1-5678.xml", TRANSFORMATION.WithOutputAndUserHomePath);

    }

    @Test
    public void readFile() throws Exception {
        EpsosXSLTransformer xlsClass = new EpsosXSLTransformer();
        //String out = xlsClass.readFile("samples/2-4567.xml");
        String out = xlsClass.readFile("samples/1-5678.xml");
        String pt = Paths.get(System.getenv("EPSOS_PROPS_PATH"), "EpsosRepository", "out", "readfile.txt")
                .toString();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pt), "utf-8"))) {
            writer.write(out);
        }
    }

    private enum TRANSFORMATION {
        WithOutputAndUserHomePath, ForPDF, UsingStandardCDAXsl, WithOutputAndDefinedPath
    }

//	@Test
//	public void runFolder() throws Exception {
//		folderTest("samples", TRANSFORMATION.WithOutputAndUserHomePath);
//	}
}
