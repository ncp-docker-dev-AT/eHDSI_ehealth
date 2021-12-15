package eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc.persistence.model;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc.Direction;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "eTransaction")
public class Transaction {

    @Id
    @Column(name = "Transaction_PK")
    private String id;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    private String homeISO;

    @Column(name = "HomeNCP_OID")
    private String homeOID;

    private String homeHCID;

    private String homeHost;

    private String homeAddress;

    private String sndISO;

    @Column(name = "SndNCP_OID")
    private String sndOID;

    private String sndHCID;

    private String sndHost;

    private String sndAddress;

    private String sndMsgID;

    private String receivingISO;

    @Column(name = "ReceivingNCP_OID")
    private String receivingOID;

    private String receivingHost;

    @Column(name = "ReceivingAddr")
    private String receivingAddress;

    private String receivingMsgID;

    private String transactionCounter;

    private String humanRequestor;

    private String userId;

    private String poc;

    @Column(name = "POC_ID")
    private String pocID;

    private String authenticationLevel;

    private String requestAction;

    private String responseAction;

    private String serviceType;

    private String serviceName;

    @Convert(converter = EadcDbTimeConverter.class)
    private Instant startTime;

    @Convert(converter = EadcDbTimeConverter.class)
    private Instant endTime;

    private String duration;

    @OneToOne(mappedBy = "transaction")
    private TransactionData transactionData;

    public String getId() {
        return id;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getHomeISO() {
        return homeISO;
    }

    public String getHomeOID() {
        return homeOID;
    }

    public String getHomeHCID() {
        return homeHCID;
    }

    public String getHomeHost() {
        return homeHost;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public String getSndISO() {
        return sndISO;
    }

    public String getSndOID() {
        return sndOID;
    }

    public String getSndHCID() {
        return sndHCID;
    }

    public String getSndHost() {
        return sndHost;
    }

    public String getSndAddress() {
        return sndAddress;
    }

    public String getSndMsgID() {
        return sndMsgID;
    }

    public String getReceivingISO() {
        return receivingISO;
    }

    public String getReceivingOID() {
        return receivingOID;
    }

    public String getReceivingHost() {
        return receivingHost;
    }

    public String getReceivingAddress() {
        return receivingAddress;
    }

    public String getReceivingMsgID() {
        return receivingMsgID;
    }

    public String getTransactionCounter() {
        return transactionCounter;
    }

    public String getHumanRequestor() {
        return humanRequestor;
    }

    public String getUserId() {
        return userId;
    }

    public String getPoc() {
        return poc;
    }

    public String getPocID() {
        return pocID;
    }

    public String getAuthenticationLevel() {
        return authenticationLevel;
    }

    public String getRequestAction() {
        return requestAction;
    }

    public String getResponseAction() {
        return responseAction;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public String getDuration() {
        return duration;
    }

    public TransactionData getTransactionData() {
        return transactionData;
    }

    public void setTransactionData(TransactionData transactionData) {
        this.transactionData = transactionData;
    }
}
