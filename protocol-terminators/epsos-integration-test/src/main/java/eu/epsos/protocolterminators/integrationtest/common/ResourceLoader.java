package eu.epsos.protocolterminators.integrationtest.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Helper class for loading file resources from the classpath.
 *
 * @author gareth
 */
public class ResourceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoader.class);

    /**
     * Locates and loads file for the specified filename.  Returns contents as a String.
     *
     * @param resourceName Filename (without path)
     * @return
     */
    public String getResource(String resourceName) {
        LOGGER.debug("Loading resource with resourceName: '{}'", resourceName);
        String resourceStr = "";

        ClassLoader cl = getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(resourceName);

        if (is != null) {
            resourceStr = streamToString(is);
            LOGGER.debug("Successfully loaded '{}': '{}'", resourceName, resourceStr);
        }

        if (resourceStr.isEmpty()) {
            LOGGER.info("ERROR - Failed to load resource with name '{}'", resourceName);
        }

        return resourceStr;
    }

    private String streamToString(InputStream in) {
        StringBuilder out = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                out.append(line);
            }
            br.close();
            return out.toString();
        } catch (Exception e) {
            LOGGER.info("Exception: '{}'", e.getMessage(), e);
        }
        return out.toString();
    }
}
