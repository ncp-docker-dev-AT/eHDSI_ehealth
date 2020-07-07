package com.gnomon.epsos.model;

import tr.com.srdc.epsos.util.Constants;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "medicationsDispensed")
@SessionScoped
public class MedicationBean implements Serializable {

    private static final long serialVersionUID = -3893661360983844773L;
    private List<MedicationDispensed> medicationDispensedList = new ArrayList<>();

    public MedicationBean() {

        List<String> medications = loadMedications();
        for (String fileName : medications) {
            MedicationDispensed medicationDispensed = new MedicationDispensed();
            medicationDispensed.setDocument(fileName);
            medicationDispensedList.add(medicationDispensed);
        }
    }

    public List<MedicationDispensed> getMedicationDispensedList() {
        return medicationDispensedList;
    }

    public void setMedicationDispensedList(List<MedicationDispensed> medicationDispensedList) {
        this.medicationDispensedList = medicationDispensedList;
    }

    private List<String> loadMedications() {

        String directoryName = Constants.EPSOS_PROPS_PATH + "integration/" +
                Constants.HOME_COMM_ID + "/medication";
        File folder = new File(directoryName);
        File[] listOfFiles = folder.listFiles();

        List<String> medicationList = new ArrayList<>();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    medicationList.add(file.getName());
                }
            }
        }
        return medicationList;
    }
}
