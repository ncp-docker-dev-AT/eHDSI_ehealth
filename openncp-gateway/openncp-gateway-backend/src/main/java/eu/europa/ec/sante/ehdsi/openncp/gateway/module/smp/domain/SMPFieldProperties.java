package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain;

/**
 * @author Inês Garganta
 */
public class SMPFieldProperties {

    private String name;
    private boolean enable;
    private boolean mandatory;
    private boolean display;
    private Object currValue;

    public SMPFieldProperties() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public Object getCurrValue() {
        return this.currValue;
    }

    public void setCurrValue(Object currValue) {
        this.currValue = currValue;
    }
}
