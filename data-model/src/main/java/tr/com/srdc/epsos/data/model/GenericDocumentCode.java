package tr.com.srdc.epsos.data.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("schema", schema)
                .toString();
    }
}
