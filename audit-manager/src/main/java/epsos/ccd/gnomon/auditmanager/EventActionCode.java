package epsos.ccd.gnomon.auditmanager;

/**
 * Enumeration for populating the EventActionCode of the AuditMessage
 * C:create, R:Read,View,Print,Query, U:Update, D:Delete, E:Execute
 *
 * @author Kostas Karkaletsis
 * @author Organization: Gnomon
 * @author mail:k.karkaletsis@gnomon.com.gr
 * @version 1.0, 2010, 30 Jun
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

    private String code;

    EventActionCode(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }
}
