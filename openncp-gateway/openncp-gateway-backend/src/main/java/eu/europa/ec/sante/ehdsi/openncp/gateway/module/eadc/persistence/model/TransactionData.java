package eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "eTransactionData")
public class TransactionData {

    @Id
    @Column(name = "TransactionData_PK")
    private String id;

    private String dataType;

    private String dataTypeName;

    private String dataValue;

    private String valueDisplay;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="Transaction_FK")
    private Transaction transaction;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public String getValueDisplay() {
        return valueDisplay;
    }

    public void setValueDisplay(String valueDisplay) {
        this.valueDisplay = valueDisplay;
    }
}
