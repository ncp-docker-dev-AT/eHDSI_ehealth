/*
 *  Copyright 2010 Jerry Dimitriou <jerouris at netsmart.gr>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package epsos.ccd.netsmart.securitymanager;

import epsos.ccd.netsmart.securitymanager.key.KeyStoreManager;
import epsos.ccd.netsmart.securitymanager.key.impl.DefaultKeyStoreManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * @author Jerry Dimitriou <jerouris at netsmart.gr>
 */
public class ConfigManTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManTest.class);

    public ConfigManTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws IOException {

    }

    @After
    public void tearDown() {
    }

    @Test
    public void configManTests() throws IOException {

        ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();
        assertNotNull(configurationManager);

        // Constants Initialization
        String KEYSTORE_LOCATION = configurationManager.getProperty("NCP_SIG_KEYSTORE_PATH");
        LOGGER.info("Keystore Location: '{}'", KEYSTORE_LOCATION);

        String TRUSTSTORE_LOCATION = configurationManager.getProperty("TRUSTSTORE_PATH");
        LOGGER.info("Truststore Location: '{}'", TRUSTSTORE_LOCATION);

        String KEYSTORE_PASSWORD = configurationManager.getProperty("NCP_SIG_KEYSTORE_PASSWORD");
        String TRUSTSTORE_PASSWORD = configurationManager.getProperty("TRUSTSTORE_PASSWORD");

        String PRIVATEKEY_ALIAS = configurationManager.getProperty("NCP_SIG_PRIVATEKEY_ALIAS");
        String PRIVATEKEY_PASSWORD = configurationManager.getProperty("NCP_SIG_PRIVATEKEY_PASSWORD");

        assertNotNull(KEYSTORE_LOCATION);
        assertNotNull(TRUSTSTORE_LOCATION);

        assertNotNull(KEYSTORE_PASSWORD);
        assertNotNull(TRUSTSTORE_PASSWORD);

        assertNotNull(PRIVATEKEY_ALIAS);
        assertNotNull(PRIVATEKEY_PASSWORD);

        KeyStoreManager km = new DefaultKeyStoreManager();
        assertNotNull(km);
    }
}
