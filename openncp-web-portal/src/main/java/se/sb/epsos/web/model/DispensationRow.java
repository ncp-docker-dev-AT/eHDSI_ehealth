package se.sb.epsos.web.model;

import java.io.Serializable;
import java.util.Date;

public class DispensationRow implements Serializable {

    private static final long serialVersionUID = -7340918304594660151L;

    private PrescriptionRow prescriptionRow;

    private boolean dispense;
    private String productName;
    private String productId;
    private QuantityVO packageSize;
    private QuantityVO nbrPackages;
    private boolean substitute;
    private Date dispensationDate = new Date();

    public DispensationRow(PrescriptionRow prescriptionRow) {
        this.prescriptionRow = prescriptionRow;
    }

    public PrescriptionRow getPrescriptionRow() {
        return this.prescriptionRow;
    }

    public void setPrescriptionRow(PrescriptionRow prescriptionRow) {
        this.prescriptionRow = prescriptionRow;
    }

    public boolean isDispense() {
        return dispense;
    }

    public void setDispense(boolean dispense) {
        this.dispense = dispense;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public QuantityVO getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(QuantityVO packageSize) {
        this.packageSize = packageSize;
    }

    public QuantityVO getNbrPackages() {
        return nbrPackages;
    }

    public void setNbrPackages(QuantityVO nbrPackages) {
        this.nbrPackages = nbrPackages;
    }

    public boolean isSubstitute() {
        return substitute;
    }

    public void setSubstitute(boolean substitute) {
        this.substitute = substitute;
    }

    public Date getDispensationDate() {
        return dispensationDate;
    }

    public void setDispensationDate(Date dispensationDate) {
        this.dispensationDate = dispensationDate;
    }
}
