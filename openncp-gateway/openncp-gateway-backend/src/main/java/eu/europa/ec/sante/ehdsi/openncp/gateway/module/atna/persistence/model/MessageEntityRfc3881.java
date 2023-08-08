package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("RFC3881")
public class MessageEntityRfc3881 extends MessageEntity{

}