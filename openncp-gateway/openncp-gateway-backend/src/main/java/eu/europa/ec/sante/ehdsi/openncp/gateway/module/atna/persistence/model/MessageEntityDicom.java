package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DICOM")
public class MessageEntityDicom extends MessageEntity{

}