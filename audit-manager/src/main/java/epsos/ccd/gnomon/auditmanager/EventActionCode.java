package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventActionCode of the AuditMessage.
 * C:create; R:{Read,View,Print,Query}; U:Update; D:Delete; E:Execute.
 *
 * @author Kostas Karkaletsis
 */
public enum EventActionCode {

    CREATE("C"),
    READ("R"),
    VIEW("R"),
    PRINT("R"),
    QUERY("R"),
    UPDATE("U"),
    DELETE("D"),
    EXECUTE("E");

    private final String code;

    EventActionCode(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
