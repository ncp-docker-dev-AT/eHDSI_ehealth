package eu.epsos.protocolterminators.ws.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * List resources available from the classpath.
 *
 * @author stoughto http://forums.devx.com/showthread.php?153784-how-to-list-resources-in-a-package
 */
public class ResourceList {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceList.class);

    private ResourceList() {
    }

    /**
     * Gets all resources.
     * For all elements of java.class.path get a Collection of resources Pattern pattern = Pattern.compile(".*").
     *
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
    public static Collection<String> getResources(Pattern pattern) {

        ArrayList<String> resources = new ArrayList<>();
        URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        URL[] classPathElements = classLoader.getURLs();
        for (URL element : classPathElements) {
            try {
                if (!element.toString().contains("mock")) continue;
                LOGGER.debug("Found URL '{}'", element);
                resources.addAll(getResources(element.toURI(), pattern));
            } catch (URISyntaxException e) {
                LOGGER.debug("Could not convert URL " + element + " into URI");
            }
        }
        return resources;
    }

    private static Collection<String> getResources(URI element, Pattern pattern) {

        ArrayList<String> resources = new ArrayList<>();
        File file = new File(element);
        if (file.isDirectory()) {
            resources.addAll(getResourcesFromDirectory(file, pattern));
        } else {
            resources.addAll(getResourcesFromJarFile(file, pattern));
        }
        return resources;
    }

    private static Collection<String> getResourcesFromJarFile(File file, Pattern pattern) {

        ArrayList<String> resources = new ArrayList<>();
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            throw new Error(e);
        }
        Enumeration e = zipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) e.nextElement();
            String fileName = ze.getName();
            boolean accept = pattern.matcher(fileName).matches();
            if (accept) {
                resources.add(fileName);
            }
        }
        try {
            zipFile.close();
        } catch (IOException e1) {
            throw new Error(e1);
        }
        return resources;
    }

    private static Collection<String> getResourcesFromDirectory(File directory, Pattern pattern) {

        ArrayList<String> resources = new ArrayList<>();
        File[] fileList = directory.listFiles();
        for (File file : fileList) {
            if (file.isDirectory()) {
                resources.addAll(getResourcesFromDirectory(file, pattern));
            } else {
                try {
                    String fileName = file.getCanonicalPath();
                    boolean accept = pattern.matcher(fileName).matches();
                    if (accept) {
                        resources.add(fileName);
                    }
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        }
        return resources;
    }
}
