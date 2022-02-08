package tr.com.srdc.epsos.data.model;

public enum SubstitutionCode {
    G("Generic"),
    N("none"),
    TE("therapeutic alternative");
    private final String displayName;

    SubstitutionCode(String displayName) {
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
