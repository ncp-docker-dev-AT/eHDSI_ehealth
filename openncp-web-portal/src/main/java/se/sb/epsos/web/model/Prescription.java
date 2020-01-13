package se.sb.epsos.web.model;

import se.sb.epsos.shelob.ws.client.jaxws.EpsosDocument;
import se.sb.epsos.web.service.MetaDocument;
import se.sb.epsos.web.util.CdaHelper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Prescription extends CdaDocument implements Serializable {

    private static final long serialVersionUID = 4296393437480963323L;

    private List<PrescriptionRow> rows;

    private String performer;
    private String profession;
    private String facility;
    private String address;
    private String contact1;
    private String contact2;
    private String country;

    private Date createDate;

    public Prescription(MetaDocument metaDoc) {
        super(metaDoc);
    }

    public Prescription(MetaDocument metaDoc, byte[] bytes, EpsosDocument epsosDocument) {
        super(metaDoc, bytes, epsosDocument);
        CdaHelper cdaHelper = new CdaHelper();
        cdaHelper.parsePrescriptionFromDocument(this);
    }

    public List<PrescriptionRow> getRows() {
        return this.rows;
    }

    public void setRows(List<PrescriptionRow> rows) {
        this.rows = rows;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getContact1() {
        return contact1;
    }

    public void setContact1(String contact1) {
        this.contact1 = contact1;
    }

    public String getContact2() {
        return contact2;
    }

    public void setContact2(String contact2) {
        this.contact2 = contact2;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
