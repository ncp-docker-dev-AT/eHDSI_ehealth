package com.gnomon.epsos.model;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tr.com.srdc.epsos.util.Constants;

@ManagedBean(name = "medicationsDispensed")
@RequestScoped
public class MedicationBean implements Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MedicationBean.class);
	private static final long serialVersionUID = -3893661360983844773L;
	private List<MedicationDispensed> medicationDispensedList = new ArrayList<>();

	public MedicationBean() {
/*
		List<String> medications = loadMedications();
		for (String fileName : medications) {
			MedicationDispensed medicationDispensed = new MedicationDispensed();
			medicationDispensed.setDocument(fileName);
			medicationDispensedList.add(medicationDispensed);
		}
		*/
		loadMedications();
	}

	public List<MedicationDispensed> getMedicationDispensedList() {
		return medicationDispensedList;
	}

	public void setMedicationDispensedList(List<MedicationDispensed> medicationDispensedList) {
		this.medicationDispensedList = medicationDispensedList;
	}

	private /*List<String>*/ void loadMedications() {

		String directoryName = Constants.EPSOS_PROPS_PATH + "integration/" + Constants.HOME_COMM_ID + "/medication";
		File folder = new File(directoryName);
		File[] listOfFiles = folder.listFiles();

//		List<String> medicationList = new ArrayList<>();
		if (listOfFiles != null) {
			for (File file : listOfFiles) {
				if (file.isFile() && file.getName().endsWith(".xml")) {
//					medicationList.add(file.getName());
					medicationDispensedList.add(loadMedicationDispensed( file));
				}
			}
		}
//		return medicationList;
	}

	private MedicationDispensed loadMedicationDispensed(File file) {
		MedicationDispensed medicationDispensed = new MedicationDispensed();

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagNameNS("*", "id");
			if (nodeLst.getLength() > 0) {
				Element link = (Element) nodeLst.item(0);
				medicationDispensed.setDispensedId(link.getAttribute("extension"));
				medicationDispensed.setDocument(file.getName());
			}

			nodeLst = doc.getElementsByTagNameNS("*", "effectiveTime");
			if (nodeLst.getLength() > 0) {
				Element link = (Element) nodeLst.item(0);

				String time = link.getAttribute("value");
				if (time != null) {
					if (time.length() >= 14) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
						medicationDispensed.setEffectiveTime(sdf.parse(time.substring(0, 14)));
					} else if (time.length() >= 8) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
						medicationDispensed.setEffectiveTime(sdf.parse(time.substring(0, 8)));
					}
				}

			}
		} catch (Exception e) {
			LOGGER.error("Error getting country ids '{}'", e.getMessage(), e);
		}

		return medicationDispensed;
	}

}
