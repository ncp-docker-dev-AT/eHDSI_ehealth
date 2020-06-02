package com.gnomon.epsos.service;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HtmlToPdfConverterTest {

    @Test
    public void testConversion() throws IOException {
        String html = readFile(new File("src/test/resources/ps_friendly.html"));
        ByteArrayOutputStream outputStream = HtmlToPdfConverter.createPdf(html);
        Assert.assertNotNull(outputStream);
        Assert.assertTrue(outputStream.toByteArray().length > 0);
    }

    private String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(file.toURI())));
    }
}
