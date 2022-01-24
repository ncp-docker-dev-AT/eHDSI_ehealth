package tr.com.srdc.epsos.data.model.xds;

/**
 * This ENUM gathers all the epSOS transacted documents.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public enum DocumentType {

    PATIENT_SUMMARY("Patient Summary"),
    EDISPENSATION("eDispensation"),
    EPRESCRIPTION("ePrescription"),
    ORCD("Original Clinical Document"),
    MRO("Medication Summary"),
    HCER("Heatlhcare Encounter Report Summary");
    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
