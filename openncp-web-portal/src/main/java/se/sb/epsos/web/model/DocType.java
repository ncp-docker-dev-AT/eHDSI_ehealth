package se.sb.epsos.web.model;

public enum DocType {

    EP("urn:epSOS:ep:pre:2010"),
    PS("urn:epSOS:ps:ps:2010"),
    ED("urn:epSOS:ep:dis:2010"),
    PDF("urn:ihe:iti:xds-sd:pdf:2008");

    private String formatCode;

    DocType(String classCode) {
        this.formatCode = classCode;
    }

    public static DocType getFromFormatCode(String formatCode) {
        for (DocType type : DocType.values()) {
            if (type.formatCode.equals(formatCode)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return formatCode;
    }
}
