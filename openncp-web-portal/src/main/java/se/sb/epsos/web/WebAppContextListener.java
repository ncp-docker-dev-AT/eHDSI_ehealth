package se.sb.epsos.web;

import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebAppContextListener implements ServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(WebAppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        if (logger.isDebugEnabled()) {
            logger.debug("Web Application Initialization");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        if (logger.isDebugEnabled()) {
            logger.info("Web Application Destroyed");
        }
        AuditServiceFactory.stopAuditService();
    }
}

