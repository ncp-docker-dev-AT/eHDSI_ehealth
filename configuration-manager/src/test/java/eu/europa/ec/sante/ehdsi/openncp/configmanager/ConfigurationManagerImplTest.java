package eu.europa.ec.sante.ehdsi.openncp.configmanager;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.domain.Property;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ConfigurationManagerImplTest {

    private static final String COUNTRY_VALUE = "FR";

    private static final String EMAIL_VALUE = "openncp.test@ec.europa.eu";

    private SessionFactory sessionFactory;

    @Before
    public void before() {
        Configuration configuration = new Configuration()
                .addAnnotatedClass(Property.class)
                .setProperty(Environment.URL, "jdbc:h2:mem:test")
                .setProperty(Environment.USER, "sa")
                .setProperty(Environment.PASS, "")
                .setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread")
                .setProperty(Environment.HBM2DDL_AUTO, "create-drop");

        try {
            sessionFactory = configuration.buildSessionFactory();
        } catch (HibernateException e) {
            throw new ExceptionInInitializerError(e);
        }

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.save(new Property(StandardProperties.NCP_COUNTRY, COUNTRY_VALUE));
        transaction.commit();
    }

    @After
    public void after() {
        sessionFactory.close();
    }

    @Test(expected = PropertyNotFoundException.class)
    public void testGetInvalidProperty() {
        ConfigurationManager configurationManager = new ConfigurationManagerImpl(sessionFactory);
//        ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();
        configurationManager.getProperty("INVALID_OPENNCP_PROPERTY");
    }

    @Test
    public void testGetProperty() {
        ConfigurationManager configurationManager = new ConfigurationManagerImpl(sessionFactory);
        String value = configurationManager.getProperty(StandardProperties.NCP_COUNTRY);

        Assert.assertEquals(COUNTRY_VALUE, value);
    }

    @Test
    @SuppressWarnings("JpaQlInspection")
    public void testSetProperty() {
        ConfigurationManager configurationManager = new ConfigurationManagerImpl(sessionFactory);
        configurationManager.setProperty(StandardProperties.NCP_EMAIL, EMAIL_VALUE);

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        //TypedQuery<Long> query = session.createQuery("select count(p.key) from Property p", Long.class);
        Query query = session.createQuery("select count(p.key) from Property p");
        List result = query.list();

        Property property = session.get(Property.class, StandardProperties.NCP_EMAIL);
        transaction.commit();

        Assert.assertEquals(2L, result.get(0));
        Assert.assertEquals(EMAIL_VALUE, property.getValue());
    }
}
