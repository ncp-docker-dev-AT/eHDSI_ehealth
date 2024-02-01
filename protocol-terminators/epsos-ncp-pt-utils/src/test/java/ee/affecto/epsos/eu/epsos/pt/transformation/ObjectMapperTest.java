package ee.affecto.epsos.eu.epsos.pt.transformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.sante.ehdsi.openncp.tm.domain.TMResponseStructure;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ObjectMapperTest {

    @Test
    public void testObjectMapper() throws Exception {
        var mapper = new ObjectMapper();
        var json = Files.readString(Paths.get(getClass().getClassLoader().getResource("jsonResponse.json").toURI()));
        var tmResponse = mapper.readValue(json, TMResponseStructure.class);
        Assert.assertNotNull(tmResponse);
    }
}

