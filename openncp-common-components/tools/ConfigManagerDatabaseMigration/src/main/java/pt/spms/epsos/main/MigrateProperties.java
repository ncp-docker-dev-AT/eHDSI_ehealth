/*
 * This file is part of epSOS OpenNCP implementation
 * Copyright (C) 2012  SPMS (Serviços Partilhados do Ministério da Saúde - Portugal)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contact email: epsos@iuz.pt
 */
package pt.spms.epsos.main;

import eu.epsos.configmanager.database.HibernateUtil;
import eu.epsos.configmanager.database.model.Property;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * MigrateProperties main class, that includes the main operation methods.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public final class MigrateProperties {

    /**
     * Epsos properties file name
     */
    private final static String EPSOS_PROPS = "epsos.properties";
    /**
     * Epsos SRDC properties file name
     */
    private final static String SRDC_PROPS = "epsos-srdc.properties";
    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrateProperties.class);

    /**
     * Private constructor to disable instantiation.
     */
    private MigrateProperties() {
    }

    /**
     * Main Runnable method.
     *
     * @param args the program arguments
     * @throws ConfigurationException
     */
    public static void main(final String[] args) throws ConfigurationException, IOException {

        if (args.length != 2) {
            LOGGER.warn("USAGE: java ConfigManagerMigrationTool.java -f < srdc / epsos >");
            return;
        }

        String epsosPropsFile;

        switch (args[1]) {
            case "epsos":
                epsosPropsFile = getPropertiesPath(EPSOS_PROPS);
                break;
            case "srdc":
                epsosPropsFile = getPropertiesPath(SRDC_PROPS);
                break;
            default:
                LOGGER.warn("USAGE: java ConfigManagerMigrationTool.java -f < srdc / epsos >");
                return;
        }

        LOGGER.info("STARTING MIGRATION PROCESS...");
        if (StringUtils.isBlank(epsosPropsFile)) {
            return;
        }
        processProperties(epsosPropsFile);
        LOGGER.info("MIGRATION PROCESS SUCCESSFULLY ENDED!");
    }

    /**
     * This method will process the properties and insert them in the database,
     * based on the specified properties file name.
     *
     * @param epsosPropsFile the properties file name.
     * @throws ConfigurationException if the properties file reading wen wrong.
     */
    private static void processProperties(final String epsosPropsFile) throws ConfigurationException, IOException {

        LOGGER.info("READING CONFIGURATION FILE FROM: '{}'", epsosPropsFile);
        File propsFile = new File(epsosPropsFile);
        processCommaProperties(propsFile, epsosPropsFile);

        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.setReloadingStrategy(new FileChangedReloadingStrategy());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            LOGGER.info("INSERTING PROPERTIES INTO DATABASE...");
            session.beginTransaction();

            final Iterator it = config.getKeys();

            while (it.hasNext()) {
                final String key = (String) it.next();
                final String value = config.getString(key);

                LOGGER.info("INSERTING: { KEY: '{}', VALUE: '{}'}", key, value);

                final Property p = new Property(key, value);
                session.save(p);
            }
            session.getTransaction().commit();
            session.close();
        }
    }

    /**
     * This method will build the properties path, based on the EPSOS_PROPS_PATH
     * configuration.
     *
     * @param fileName the properties file name.
     * @return the properties file with full path.
     */
    private static String getPropertiesPath(final String fileName) {

        final String envKey = getEnvKey("EPSOS_PROPS_PATH");

        if (envKey.isEmpty()) {
            LOGGER.error("EPSOS PROPERTIES PATH NOT FOUND!");
            return null;
        }

        return getEnvKey("EPSOS_PROPS_PATH") + fileName;
    }

    /**
     * This method returns the value of an operating system variable
     *
     * @param key the key name
     * @return the string value of the variable
     */
    private static String getEnvKey(final String key) {
        String value = "";
        final Map map = System.getenv();
        final Set keys = map.keySet();
        for (Object key2 : keys) {
            final String key1 = (String) key2;
            if (key1.equals(key)) {
                value = (String) map.get(key1);
                break;
            }
        }
        return value;
    }

    /**
     * This method will replace the commas with "\," to improve the processing by Apache-Commons
     *
     * @param propertiesFile
     * @param filePath
     * @throws IOException
     */
    private static void processCommaProperties(File propertiesFile, String filePath) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(propertiesFile)); FileWriter writer = new FileWriter(filePath)) {

            String line;
            StringBuilder oldtext = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                oldtext.append(line).append("\r\n");
            }
            reader.close();
            // replace a word in a file
            String newtext = oldtext.toString().replaceAll(",", "\\,");

            writer.write(newtext);
            writer.close();

        } catch (IOException ioe) {
            LOGGER.error("IOException: '{}'", ioe.getMessage(), ioe);
        }
    }
}
