package fi.kela.se.epsos.data.model;

public enum SimpleConfidentiality {
    NORMAL("N");

    private final String code;

    public String code() {
        return code;
    }

    public static SimpleConfidentiality findByCode(String code){
        for(SimpleConfidentiality v : values()){
            if( v.code().equals(code)){
                return v;
            }
        }
        return NORMAL;
    }

    private SimpleConfidentiality(String code) {
        this.code = code;
    }
}
