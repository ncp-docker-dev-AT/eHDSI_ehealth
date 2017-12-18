package eu.epsos.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ValidationTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationTestBase.class);

    protected String getResource(String filename) {

        ClassLoader loader = getClass().getClassLoader();
        InputStream inputStream = loader.getResourceAsStream(filename);
        StringBuilder builder = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line);
            }
            reader.close();
            inputStream.close();
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return builder.toString();
    }
}
