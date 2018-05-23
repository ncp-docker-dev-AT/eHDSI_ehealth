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
    public void testConvertion() throws IOException {
        String html = readFile(new File("openncp-portal/src/test/resources/ps_friendly.html"));
        ByteArrayOutputStream baos = HtmlToPdfConverter.createPdf(html);
        Assert.assertNotNull(baos);
        Assert.assertTrue(baos.toByteArray().length > 0);
//        String filename = "C:\\LocalData\\Share\\openncp-configuration\\EpsosRepository\\out\\output.pdf";
//        FileOutputStream output = new FileOutputStream(filename);
//        output.write(baos.toByteArray());
//        output.close();
    }

    private String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(file.toURI())));
    }
}
