package tr.com.srdc.epsos.data.model;

/**
 * This class represents a Generic Document Code, which holds a code Value and a code Schema.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class GenericDocumentCode {

    private String value;
    private String schema;

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the extension
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @param schema the extension to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }
}
