package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain;

public class ReferenceCollection {

    private String reference;
    private String smpType;
    private String smpUri;
    private int id;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSmpType() {
        return smpType;
    }

    public void setSmpType(String smpType) {
        this.smpType = smpType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSmpUri() {
        return smpUri;
    }

    public void setSmpUri(String smpUri) {
        this.smpUri = smpUri;
    }
}
