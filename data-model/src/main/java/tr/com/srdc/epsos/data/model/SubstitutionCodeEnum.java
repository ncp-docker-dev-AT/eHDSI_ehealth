package tr.com.srdc.epsos.data.model;

public enum SubstitutionCodeEnum {

    G("generic"),
    N("none"),
    TE("therapeutic");

    private final String displayName;

    SubstitutionCodeEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
