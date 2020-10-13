package com.gnomon.epsos.model;

import java.io.Serializable;
import java.util.Date;

public class MedicationDispensed implements Serializable {

	private static final long serialVersionUID = 8235682380235702521L;
	private String dispensedId;
	private String patientId;
	private String document;
	private Date effectiveTime;

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

	public Date getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(Date effectiveTime) {
		this.effectiveTime = effectiveTime;
	}
}
