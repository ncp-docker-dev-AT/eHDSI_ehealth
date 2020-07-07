package com.gnomon.epsos.model;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;

@ManagedBean
@RequestScoped
public class MedicationDispensed implements Serializable {

    private static final long serialVersionUID = 8235682380235702521L;
    private String dispensedId;
    private String patientId;
    private String document;

    public MedicationDispensed() {
    }

    public String getDispensedId() {
        return dispensedId;
    }

    public void setDispensedId(String dispensedId) {
        this.dispensedId = dispensedId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }
}
