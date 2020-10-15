package com.gnomon.epsos.model;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
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

	public List<MedicationDispensed> getMedicationDispensedList() {
		return medicationDispensedList;
	}

	public void setMedicationDispensedList(List<MedicationDispensed> medicationDispensedList) {
		this.medicationDispensedList = medicationDispensedList;
	}

	@PostConstruct
	private void loadMedications() {

		String directoryName = Constants.EPSOS_PROPS_PATH + "integration/" + Constants.HOME_COMM_ID + "/medication";
		File folder = new File(directoryName);
		File[] listOfFiles = folder.listFiles();

		medicationDispensedList = Optional.ofNullable(listOfFiles).map(Arrays::asList).orElse(Collections.emptyList())
				.stream().filter(f -> f.isFile() && f.getName().endsWith(".xml")).map(this::loadMedicationDispensed)
				.collect(Collectors.toList());
		;

	}

	private MedicationDispensed loadMedicationDispensed(File file) {
		MedicationDispensed medicationDispensed = new MedicationDispensed();

		try {
			medicationDispensed.setDocument(file.getName());

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagNameNS("*", "id");
			if (nodeLst != null //
					&& nodeLst.getLength() > 0 //
					&& nodeLst.item(0) != null ) {
				Element link = (Element) nodeLst.item(0);
				String root = link.getAttribute("root");
				String extension = link.getAttribute("extension");
				String dispenseId = new StringBuilder().append("Root = ").append(root == null ? "" : root).//
						append("; ").append("Extension = ").append(extension == null ? "" : extension).toString();
				medicationDispensed.setDispensedId(dispenseId);
			}

			nodeLst = doc.getElementsByTagNameNS("*", "effectiveTime");
			if (nodeLst != null //
					&& nodeLst.getLength() > 0 //
					&& nodeLst.item(0) != null //
					&& ((Element) nodeLst.item(0)).getAttribute("value") != null) {
				Element link = (Element) nodeLst.item(0);
				String time = link.getAttribute("value");
				if (time.length() >= 14) {
					medicationDispensed.setEffectiveTime(new SimpleDateFormat("yyyyMMddHHmmss")//
							.parse(time.substring(0, 14)));
				} else if (time.length() >= 8) {
					medicationDispensed.setEffectiveTime(new SimpleDateFormat("yyyyMMdd")//
							.parse(time.substring(0, 8)));
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error getting country ids '{}'", e.getMessage(), e);
		}

		return medicationDispensed;
	}

}
