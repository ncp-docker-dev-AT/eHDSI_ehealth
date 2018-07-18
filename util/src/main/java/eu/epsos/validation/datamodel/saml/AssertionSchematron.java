package eu.epsos.validation.datamodel.saml;

import eu.epsos.validation.datamodel.common.ObjectType;

/**
 * This enumerator gathers all the schematrons used in the Audit Messages Validator
 * at EVS Client.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 * @deprecated
 */
@Deprecated
public enum AssertionSchematron {

    EPSOS_HCP_IDENTITY_ASSERTION("epSOS - HCP Identity Assertion"),
    EPSOS_TRC_ASSERTION("epSOS - TRC Assertion");

    private String name;

    AssertionSchematron(String s) {
        name = s;
    }

    public static AssertionSchematron checkSchematron(String model) {

        for (AssertionSchematron s : AssertionSchematron.values()) {
            if (model.equals(s.toString())) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public ObjectType getObjectType() {
        return ObjectType.ASSERTION;
    }
}
