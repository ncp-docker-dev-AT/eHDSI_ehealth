package org.openhealthtools.openatna.all.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuditLoggerPluginManagerImpl implements AuditLoggerPluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLoggerPluginManagerImpl.class);
    private List<AuditLogger> auditLoggers = new ArrayList<>();
    private String loggers = null;
    private String splitChar = ",";
    private String notDefinedLoggersValue = "${openATNA.auditLoggers}";

    public void start() throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        LOGGER.info("Starting AuditLoggerPluginManager");

        if (loggers == null || loggers.length() == 0 || notDefinedLoggersValue.equals(loggers)) {
            LOGGER.info("No auditloggers defined. Using DefaultAuditLoggerImpl.");
            auditLoggers.add(new DefaultAuditLoggerImpl());
        } else {
            String[] classes = loggers.split(splitChar);
            for (String clazz : classes) {
                LOGGER.info("Initializing auditlogger: '{}'", clazz);
                Class<AuditLogger> c = (Class<AuditLogger>) Class.forName(clazz);
                AuditLogger logger = c.newInstance();
                auditLoggers.add(logger);
            }
        }

        for (AuditLogger al : auditLoggers) {
            LOGGER.info("Starting auditlogger " + al.getClass().getName() + ".");
            al.start();
        }
    }

    public void destroy() {

        for (AuditLogger al : auditLoggers) {
            try {
                LOGGER.info("Destroying auditlogger: '{}'", al.getClass().getName() + ".");
                al.destroy();
            } catch (Exception e) {
                LOGGER.error("Unable to destroy AuditLogger!", e);
            }
        }
    }

    public void handleAuditEvent(HttpServletRequest request, Map<String, String> queryParameters, List<Long> messageEntityIds) {

        for (AuditLogger al : auditLoggers) {
            LOGGER.info("Forwarding auditEvent for auditlogger " + al.getClass().getName() + ".");
            al.logViewRequest(request, queryParameters, messageEntityIds);
        }
    }

    public String getSplitChar() {
        return splitChar;
    }

    public void setSplitChar(String splitChar) {
        this.splitChar = splitChar;
    }

    public String getLoggers() {
        return loggers;
    }

    public void setLoggers(String loggers) {
        this.loggers = loggers;
    }
}
