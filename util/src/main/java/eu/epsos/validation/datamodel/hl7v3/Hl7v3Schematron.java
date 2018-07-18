package eu.epsos.validation.datamodel.hl7v3;

import eu.epsos.validation.datamodel.common.ObjectType;

/**
 * This enumerator gathers all the schematrons used in the HL7v3 Validator at EVS Client.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 * @deprecated
 */
@Deprecated
public enum Hl7v3Schematron {

    EPSOS_ID_SERVICE_REQUEST("eHDSI - Identification Service (request)", ObjectType.XCPD_QUERY_REQUEST),
    EPSOS_ID_SERVICE_RESPONSE("eHDSI - Identification Service (response)", ObjectType.XCPD_QUERY_RESPONSE);

    private String name;
    private ObjectType objectType;

    Hl7v3Schematron(String s, ObjectType ot) {
        name = s;
        objectType = ot;
    }

    public static Hl7v3Schematron checkSchematron(String model) {
        for (Hl7v3Schematron m : Hl7v3Schematron.values()) {
            if (model.equals(m.toString())) {
                return m;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public ObjectType getObjectType() {
        return objectType;
    }
}
