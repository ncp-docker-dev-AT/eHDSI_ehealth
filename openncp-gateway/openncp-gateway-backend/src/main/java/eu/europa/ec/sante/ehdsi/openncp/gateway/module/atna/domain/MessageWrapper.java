package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain;

import generated.AuditMessage;

public class MessageWrapper {

    private Long id;

    private AuditMessage auditMessage;

    private String xmlMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuditMessage getAuditMessage() {
        return auditMessage;
    }

    public void setAuditMessage(AuditMessage auditMessage) {
        this.auditMessage = auditMessage;
    }

    public String getXmlMessage() {
        return xmlMessage;
    }

    public void setXmlMessage(String xmlMessage) {
        this.xmlMessage = xmlMessage;
    }
}
