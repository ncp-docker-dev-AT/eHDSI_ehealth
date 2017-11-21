package com.gnomon.epsos.service;

import com.gnomon.epsos.model.ViewResult;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class EpsosHelperServiceTest {

    @Test
    public void testParsePrescriptionDocumentForPrescriptionLinesForIngredient() throws URISyntaxException, IOException {
        java.net.URL url = getClass().getClassLoader().getResource("ePrescription.xml");
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        byte[] prescriptionDocument = java.nio.file.Files.readAllBytes(resPath);
        List<ViewResult> viewResults = EpsosHelperService.parsePrescriptionDocumentForPrescriptionLines(prescriptionDocument);
        Assert.assertNotNull(viewResults);
        Assert.assertEquals(1, viewResults.size());
        ViewResult viewResult = viewResults.get(0);
        Assert.assertEquals("", String.valueOf(viewResult.getField2()));
    }
}
