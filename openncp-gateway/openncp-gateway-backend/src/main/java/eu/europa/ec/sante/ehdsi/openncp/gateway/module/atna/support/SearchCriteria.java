package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.support;

public class SearchCriteria {

    private Class<?> rootClass;
    private String key;
    private String operation;
    private Object value;

    public SearchCriteria(Class<?> rootClass, String key, String operation, Object value) {
        this.rootClass = rootClass;
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Class<?> getRootClass() {
        return rootClass;
    }

    public void setRootClass(Class<?> rootClass) {
        this.rootClass = rootClass;
    }
}
