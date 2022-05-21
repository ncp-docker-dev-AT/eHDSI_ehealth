package epsos.ccd.gnomon.xslt;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author karkaletsis
 */
public class DisplayTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisplayTest.class);

    //	@Test
    private void fileTest(String input, TRANSFORMATION type) throws IOException {

        LOGGER.info("Transforming file: " + input);

        String cda = "";
        try {
            cda = CdaXSLTransformer.getInstance().readFile(input);
        } catch (Exception e) {
            LOGGER.error("File not found");
        }
        String out = "";
        switch (type) {
            case ForPDF:
                out = CdaXSLTransformer.getInstance().transformForPDF(cda, "fr-BE", false);
                break;
            case UsingStandardCDAXsl:
                out = CdaXSLTransformer.getInstance().transformUsingStandardCDAXsl(cda);
            case WithOutputAndDefinedPath:
                out = CdaXSLTransformer.getInstance().transformWithOutputAndDefinedPath(cda, "fr-BE", "",
                        Paths.get(System.getenv("EPSOS_PROPS_PATH"), "EpsosRepository"));
            case WithOutputAndUserHomePath:
                out = CdaXSLTransformer.getInstance().transformWithOutputAndUserHomePath(cda, "fr-BE", "");

        }
        String filename = Paths.get(input).getFileName().toString();
        String stripExt = filename.substring(0, filename.lastIndexOf("."));
        String pt = Paths.get(System.getenv("EPSOS_PROPS_PATH"), "EpsosRepository", "out", stripExt + ".html").toString();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pt), StandardCharsets.UTF_8))) {
            writer.write(out);
        }
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
        fileTest("/Users/mathiasghys/Development/EC/ehealth/cda-display-tool/xsltransformer/samples/EHEALTH-8147.xml", TRANSFORMATION.WithOutputAndDefinedPath);
    }

    @Test
    public void readFile() throws Exception {

        String out = CdaXSLTransformer.getInstance().readFile("samples/1-5678.xml");
        String pt = Paths.get(System.getenv("EPSOS_PROPS_PATH"), "EpsosRepository", "out", "readfile.txt").toString();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pt), StandardCharsets.UTF_8))) {
            writer.write(out);
        }
    }

    private enum TRANSFORMATION {
        WithOutputAndUserHomePath, ForPDF, UsingStandardCDAXsl, WithOutputAndDefinedPath
    }
}
