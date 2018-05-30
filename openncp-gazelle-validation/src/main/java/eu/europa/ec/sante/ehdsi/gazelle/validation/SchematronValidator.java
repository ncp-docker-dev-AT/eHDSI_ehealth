package eu.europa.ec.sante.ehdsi.gazelle.validation;

import eu.epsos.validation.datamodel.common.NcpSide;

public interface SchematronValidator {

    boolean validateObject(String base64Object, String xmlReferencedStandard, String xmlMetadata);

    boolean validateObject(String base64Object, String xmlReferencedStandard, String xmlMetadata, String objectType, NcpSide ncpSide);
}
