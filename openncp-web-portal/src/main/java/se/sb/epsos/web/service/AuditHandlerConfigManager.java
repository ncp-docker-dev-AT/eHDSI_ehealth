package se.sb.epsos.web.service;


import se.sb.epsos.web.util.MasterConfigManager;

import java.io.Serializable;

public class AuditHandlerConfigManager implements Serializable {

    public static final String CONFIG_PREFIX = "AuditHandlerConfigManager.";
    public static final String NCP_IP = "NcpIp";
    public static final String SeCountryCode = "SECountryCode";
    private static final long serialVersionUID = -968106064536729654L;

    public static String getNcpIp() {
        return MasterConfigManager.get(CONFIG_PREFIX + NCP_IP);
    }

    public static String getSeCountryCode() {
        return MasterConfigManager.get(CONFIG_PREFIX + SeCountryCode);
    }
}
