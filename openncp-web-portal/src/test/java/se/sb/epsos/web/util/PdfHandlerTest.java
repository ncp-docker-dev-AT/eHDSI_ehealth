package se.sb.epsos.web.util;

import com.itextpdf.text.DocumentException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PdfHandlerTest {

    private final Logger logger = LoggerFactory.getLogger(PdfHandlerTest.class);

    @Test
    public void testConvertStringToPdf() {

        byte[] bytes = null;
        try {
            String testHtml = FileUtils.readFileToString(new File("src/main/java/se/sb/epsos/web/pages/ViewDispensationPage.html"),
                    StandardCharsets.UTF_8);
            bytes = PdfHandler.convertStringToPdf(testHtml);
            File testResourceFolder = new File("src/test/resources/pdf");
            boolean folderCreated = testResourceFolder.mkdirs();
            logger.info("Test folder created: '{}'", folderCreated);
            File file = new File(testResourceFolder, "File.pdf");
            FileOutputStream pdfFileOs = new FileOutputStream(file);
            pdfFileOs.write(bytes);
            pdfFileOs.flush();
            pdfFileOs.close();

            assert (file.exists());
        } catch (IOException | DocumentException e) {
            logger.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
        assert (bytes != null);
    }
}
