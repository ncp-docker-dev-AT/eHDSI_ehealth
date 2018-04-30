package com.gnomon.epsos;

import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import epsos.ccd.posam.tm.service.ITransformationService;
import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class MyServletContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyServletContextListener.class);
    private static String runningMode;
    private static String encryptionKey;
    private static ITransformationService tService;
    private static ClientConnectorConsumer clientConnectorConsumer;
    private ServletContext context = null;


    public MyServletContextListener() {
        // Public constructor is required by servlet spec
    }

    public static ITransformationService getTransformationService() {
        return tService;
    }

    public static ClientConnectorConsumer getClientConnectorConsumer() {
        return clientConnectorConsumer;
    }

    public static String getRunningMode() {
        return runningMode;
    }

    public static void setRunningMode(String runningMode) {
        MyServletContextListener.runningMode = runningMode;
    }

    public static String getEncryptionKey() {
        return encryptionKey;
    }

    public static void setEncryptionKey(String encryptionKey) {
        MyServletContextListener.encryptionKey = encryptionKey;
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        LOGGER.info("Initiating OpenNCP Portal");

        try {
            runningMode = GetterUtil.get(PropsUtil.get("running.mode"), "live");
            encryptionKey = PropsUtil.get("ehealthpass.encryption.key");
            LOGGER.info("Context Listener --> Initialized");

        } catch (Exception e) {
            runningMode = "live";
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        try {
            ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("ctx_tm.xml");
            tService = (ITransformationService) applicationContext.getBean(ITransformationService.class.getName());
            LOGGER.info("Transformation Manager --> Initialized");
        } catch (Exception e) {
            LOGGER.error("#### ERROR INITIALIZING TM ####", e);
        }
        try {
            String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);
            clientConnectorConsumer = new ClientConnectorConsumer(serviceUrl);
            LOGGER.info("ClientConnector --> Initialized to: '{}'", serviceUrl);
        } catch (Exception e) {
            LOGGER.error("ERROR INITIALIZING CLIENT CONNECTOR PROXY - '{}'", e.getMessage(), e);
        }

        LOGGER.info("[Portal] Running Mode: '{}'", runningMode);
    }

    public void contextDestroyed(ServletContextEvent sce) {

        this.context = null;
    }
}
