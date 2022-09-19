package eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "eTransactionError")
public class TransactionError {
    @Id
    @Column(name = "TransactionError_PK", nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Transaction_FK")
    private Transaction transaction;

    @Column(name = "ErrorDescription")
    private String errorDescription;

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
