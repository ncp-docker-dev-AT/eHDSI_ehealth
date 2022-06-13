package tr.com.srdc.epsos.data.model;

public enum SimpleConfidentialityEnum {
    N("normal"),
    R("restricted"),
    V("very restricted");

    private final String displayName;

    SimpleConfidentialityEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static SimpleConfidentialityEnum findByCode(String code){
        for(SimpleConfidentialityEnum v : values()){
            if( v.name().equals(code)){
                return v;
            }
        }
        return N;
    }
}
