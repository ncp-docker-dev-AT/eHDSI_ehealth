package eu.europa.ec.sante.ehdsi.gazelle.validation.util;

public class ModelType {

    private String validator;
    private String objectType;

    public ModelType(String validator, String objectType) {
        this.validator = validator;
        this.objectType = objectType;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }
}
