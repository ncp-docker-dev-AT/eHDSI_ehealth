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
package eu.epsos.configmanager.test;

import epsos.ccd.gnomon.configmanager.ConfigurationManagerService;
import eu.epsos.configmanager.database.HibernateConfigFile;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
@Ignore // TODO Fix tests
public class DatabaseAccessTest {

    private final Logger logger = LoggerFactory.getLogger(DatabaseAccessTest.class);

    @BeforeClass
    public static void setUpClass() {
        HibernateConfigFile.name = "src/test/resources/configmanager.hibernate.xml";
    }

    @Test
    public void testWrite() {
        logger.info("START: Writing Properties");

        try {
            ConfigurationManagerService.getInstance().updateProperty("TEST", "TEST");
        } catch (RuntimeException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

        logger.info("END: Writing Properties");
    }

    @Test
    public void testRead() {
        logger.info("START: Read Properties");

        String value = null;

        try {
            value = ConfigurationManagerService.getInstance().getProperty("TEST");
        } catch (RuntimeException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
        assertEquals("TEST", value);

        logger.info("END: Read Properties");
    }

    @Test
    public void testUpdate() {
        logger.info("START: Update Property");

        try {
            ConfigurationManagerService.getInstance().updateProperty("TEST", "TEST1");
        } catch (RuntimeException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

        String value = null;

        try {
            value = ConfigurationManagerService.getInstance().getProperty("TEST");
        } catch (RuntimeException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
        assertEquals("TEST1", value);

        logger.info("END: Update Property");
    }

    @Test
    public void testCommaSeparatedProperties() {
        logger.info("START: Store Properties With Comma");

        String value = "";

        try {
            ConfigurationManagerService.getInstance().updateProperty("COMMAPROP", "at,cz,dk,ee,fi,fr,de,gr,ih,it,pt,sk,es,se,ch,tr");
        } catch (RuntimeException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

        try {
            value = ConfigurationManagerService.getInstance().getProperty("COMMAPROP");
        } catch (RuntimeException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
        assertEquals("at,cz,dk,ee,fi,fr,de,gr,ih,it,pt,sk,es,se,ch,tr", value);

        logger.info("END: Store Properties With Comma");
    }
}
