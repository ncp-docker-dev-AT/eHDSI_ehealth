package se.sb.epsos.web.model;

import se.sb.epsos.web.service.DocumentClientDtoCacheKey;
import tr.com.srdc.epsos.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Dispensation extends CdaDocument {

    private static final long serialVersionUID = -308023631714191605L;

    private Prescription prescription;

    private List<DispensationRow> rows;

    public Dispensation(String sessionId, String patientId, Prescription prescription) {

        super(new DocumentClientDtoCacheKey(sessionId, patientId, Constants.UUID_PREFIX + UUID.randomUUID().toString()));
        this.prescription = prescription;
        rows = new ArrayList<>();
        if (prescription != null) {
            for (PrescriptionRow prescriptionRow : prescription.getRows()) {
                rows.add(new DispensationRow(prescriptionRow));
            }
        }
    }

    public List<DispensationRow> getRows() {
        return this.rows;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }
}
