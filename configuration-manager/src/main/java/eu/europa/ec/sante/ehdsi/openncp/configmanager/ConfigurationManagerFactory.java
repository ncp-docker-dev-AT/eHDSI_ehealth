package eu.europa.ec.sante.ehdsi.openncp.configmanager;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.util.Assert;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.File;

public class ConfigurationManagerFactory {

    private static ConfigurationManager configurationManager;
    private static SessionFactory sessionFactory;

    private ConfigurationManagerFactory() {
    }

    public static ConfigurationManager getConfigurationManager() {
        if (configurationManager == null) {
            if (sessionFactory == null) {
                configurationManager = new ConfigurationManagerImpl(buildSessionFactory());
            } else {
                configurationManager = new ConfigurationManagerImpl(sessionFactory);
            }
        }
        return configurationManager;
    }

    public static void setSessionFactory (SessionFactory sessionFactory) {
        ConfigurationManagerFactory.sessionFactory = sessionFactory;
    }

    private static SessionFactory buildSessionFactory() {
        try {
            String path = System.getenv("EPSOS_PROPS_PATH");
            Assert.notNull(path, "Environment variable 'EPSOS_PROPS_PATH' is not set!");
            return new Configuration().configure(new File(path, "configmanager.cfg.xml"))
                    .buildSessionFactory();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
