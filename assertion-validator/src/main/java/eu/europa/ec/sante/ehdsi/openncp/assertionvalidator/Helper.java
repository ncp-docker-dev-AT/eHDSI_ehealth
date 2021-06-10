package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.MissingFieldException;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml.SAML;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.common.xml.SAMLSchemaBuilder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.transform.dom.DOMSource;

/**
 * TODO: improve the implementation by implementing a method which picks attribute values by attribute names
 * (avoid repetition in current methods)
 * TODO: Wave 2 review source code Assertions
 */
public class Helper {

    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    private Helper() {
    }

    /**
     * @param sh
     * @return
     */
    public static Assertion getHCPAssertion(Element sh) {

        try {
            // TODO: Since the XCA simulator sends this value in a wrong way, we are trying like this for the moment
            NodeList securityList = sh.getElementsByTagNameNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");
            Element security;
            if (securityList.getLength() > 0) {
                security = (Element) securityList.item(0);
            } else {
                throw (new MissingFieldException("Security element is required."));
            }
            NodeList assertionList = security.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion");
            Element hcpAss;
            Assertion hcpAssertion = null;

            if (assertionList.getLength() > 0) {
                for (int i = 0; i < assertionList.getLength(); i++) {
                    hcpAss = (Element) assertionList.item(i);
                    //SAMLSchemaBuilder.getSAML11Schema().newValidator().validate(new DOMSource(hcpAss));
                    SAMLSchemaBuilder schemaBuilder = new SAMLSchemaBuilder(SAMLSchemaBuilder.SAML1Version.SAML_11);
                    schemaBuilder.getSAMLSchema().newValidator().validate(new DOMSource(hcpAss));

                    hcpAssertion = (Assertion) SAML.fromElement(hcpAss);
                    if (hcpAssertion.getAdvice() == null) {
                        break;
                    }
                }
            }
            if (hcpAssertion == null) {
                throw (new MissingFieldException("HCP Assertion element is required."));
            }
            return hcpAssertion;

        } catch (Exception e) {
            logger.debug("Exception: '{}'", e.getMessage(), e);
            return null;
        }
    }

    public static String getAssertionsIssuer(Element sh) {

        Assertion assertion = getHCPAssertion(sh);
        if (assertion != null) {
            return assertion.getIssuer().getValue();
        } else {
            return "Unknown idP";
        }
    }

    public static String getAssertionsSPProvidedId(Element sh) {

        Assertion assertion = getHCPAssertion(sh);
        if (assertion != null) {
            return assertion.getSubject().getNameID().getSPProvidedID();
        } else {
            return "";
        }
    }

    public static String getUserID(Element sh) {
        String result = "N/A";

        try {
            Assertion assertion = getHCPAssertion(sh);
            if (assertion != null) {
                String val = assertion.getSubject().getNameID().getValue();
                if (StringUtils.isNotBlank(val)) {
                    result = val;
                }
            }
        } catch (Exception e) {
            logger.debug("Exception: '{}'", e.getMessage(), e);
        }

        return result;
    }

    public static String getAlternateUserID(Element sh) {
        String result = getXSPAAttributeByName(sh, "urn:oasis:names:tc:xacml:1.0:subject:subject-id", false);
        if (result == null) {
            return "N/A";
        }
        return result;
    }

    public static String getFunctionalRoleID(Element sh) {
        String result = getXSPAAttributeByName(sh, "urn:oasis:names:tc:xspa:1.0:subject:functional-role", false);
        if (result == null) {
            return "N/A";
        }
        return result;
    }

    public static String getRoleID(Element sh) {
        String result = getXSPAAttributeByName(sh, "urn:oasis:names:tc:xacml:2.0:subject:role", false);
        if (result == null) {
            return "N/A";
        }
        return result;
    }

    public static String getXSPALocality(Element soapHeader) {

        String result = getXSPAAttributeByName(soapHeader, "urn:oasis:names:tc:xspa:1.0:environment:locality", false);
        return StringUtils.isBlank(result) ? "N/A" : result;
    }

    /**
     * Util method which return the Point of Care information related to the HCP assertions, based on the element provided
     * Organization is the subject:Organization (Optional) value or if not present the environment:locality value (Required).
     *
     * @param sh
     * @return
     */
    public static String getPointOfCareUserId(Element sh) {

        String result = getXSPAAttributeByName(sh, "urn:oasis:names:tc:xspa:1.0:subject:organization", false);
        if (result == null) {
            result = getXSPAAttributeByName(sh, "urn:oasis:names:tc:xspa:1.0:environment:locality", false);
        }
        return StringUtils.isBlank(result) ? "N/A" : result;
    }

    public static String getOrganizationId(Element sh) {
        String result = getXSPAAttributeByName(sh, "urn:oasis:names:tc:xspa:1.0:subject:organization-id", false);
        if (result == null) {
            return "N/A";
        }
        return result;
    }

    public static String getOrganization(Element sh) {
        String result = getXSPAAttributeByName(sh, "urn:oasis:names:tc:xspa:1.0:subject:organization", false);
        if (result == null) {
            return "N/A";
        }
        return result;
    }

    public static String getPC_RoleID(Element sh) {
        String result = getXSPAAttributeByName(sh, "urn:epsos:names:wp3.4:subject:healthcare-facility-type", false);
        if (result == null) {
            return "N/A";
        }
        return result;
    }

    /**
     * @author Konstantin.Hypponen@kela.fi
     */
    public static Assertion getTRCAssertion(Element sh) {

        try {
            NodeList securityList = sh.getElementsByTagNameNS(
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");
            Element security;
            if (securityList.getLength() > 0) {
                security = (Element) securityList.item(0);
            } else {
                throw (new MissingFieldException("Security element is required."));
            }
            NodeList assertionList = security.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion");
            Element trcAss;
            Assertion trcAssertion = null;

            if (assertionList.getLength() > 0) {
                for (int i = 0; i < assertionList.getLength(); i++) {
                    trcAss = (Element) assertionList.item(i);

                    trcAssertion = (Assertion) SAML.fromElement(trcAss);
                    if (trcAssertion.getAdvice() != null) {
                        break;
                    }
                }
            }
            if (trcAssertion == null) {
                throw (new MissingFieldException("TRC Assertion element is required."));
            }
            return trcAssertion;

        } catch (Exception e) {
            logger.debug("Exception: '{}'", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extracts XDS-formatted patient ID from TRCAssertion
     *
     * @param sh SOAP header which includes TRC assertion
     * @return Patient ID in XDS format
     */
    public static String getDocumentEntryPatientIdFromTRCAssertion(Element sh) {

        String patientId = getXSPAAttributeByName(sh, "urn:oasis:names:tc:xacml:1.0:resource:resource-id", true);
        if (patientId == null) {
            logger.error("Patient ID not found in TRC assertion");
        }
        return patientId;
    }

    /**
     * @param sh            SOAP Header
     * @param attributeName Attribute name
     * @param trc           true, if attribute should be picked from TRC assertion
     * @return attribute value
     */
    private static String getXSPAAttributeByName(Element sh, String attributeName, boolean trc) {

        String result = null;
        Assertion assertion;

        try {

            if (trc) {
                assertion = getTRCAssertion(sh);
            } else {
                assertion = getHCPAssertion(sh);
            }
            if (assertion == null) {
                return null;
            }
            for (Attribute attr : assertion.getAttributeStatements().get(0).getAttributes()) {
                if (attr.getName().equals(attributeName)) {
                    String val = attr.getAttributeValues().get(0).getDOM().getTextContent();
                    if (StringUtils.isNotBlank(val)) {
                        result = val;
                    }
                }
            }
        } catch (Exception e) {
            String assertionType = trc ? "TRC" : "HCP";
            logger.error("XSPA attribute '{}' not found in '{}' assertion", attributeName, assertionType);
            logger.debug("Exception: '{}'", e.getMessage(), e);
        }

        return result;
    }
}
