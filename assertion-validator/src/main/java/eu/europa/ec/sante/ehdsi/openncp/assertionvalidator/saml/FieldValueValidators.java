package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.saml;

import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InvalidFieldException;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import eu.europa.ec.sante.openncp.securitymanager.TwoFactorAuthentication;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

public class FieldValueValidators {

    public static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("LOGGER_CLINICAL");

    private static final int CONDITIONS_SECOND_RANGE = 60; // a tolerance on the clock (second)
    private static final int HCP_MAXIMUM_TIME_SPAN = 4; // maximum time span for hcp identity assertion (hours)
    private static final int TRC_MAXIMUM_TIME_SPAN = 2; // maximum time span for trc assertion (hours)

    private FieldValueValidators() {
    }

    public static void validateVersionValue(Assertion assertion) throws InvalidFieldException {
        if (assertion.getVersion().getMajorVersion() != 2 || assertion.getVersion().getMinorVersion() != 0) {
            throw (new InvalidFieldException("Version must be 2.0"));
        }
    }

    public static void validateIssuerValue(Assertion assertion) throws InvalidFieldException {
        if (assertion.getIssuer().getValue() == null) {
            throw (new InvalidFieldException("Issuer should be filled."));
        } else if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && LOGGER_CLINICAL.isDebugEnabled()) {
            LOGGER_CLINICAL.debug("Issuer: '{}'", assertion.getIssuer().getValue());
        }
    }

    public static void validateNameIDValue(Assertion assertion) throws InvalidFieldException {
        if (assertion.getSubject().getNameID().getValue() == null) {
            throw (new InvalidFieldException("NameID should be filled."));
        } else if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && LOGGER_CLINICAL.isDebugEnabled()) {
            LOGGER_CLINICAL.debug("Subject Name ID: '{}'", assertion.getSubject().getNameID().getValue());
        }
    }

    public static void validateMethodValue(Assertion assertion) throws InvalidFieldException {
        if (!StringUtils.equals(assertion.getSubject().getSubjectConfirmations().get(0).getMethod(), "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches")) {
            throw (new InvalidFieldException("Method must be 'urn:oasis:names:tc:SAML:2.0:cm:sender-vouches'"));
        }
    }

    public static void validateNotBeforeValue(Assertion assertion) throws InvalidFieldException {
        var instant = Instant.now();
        if (assertion.getConditions().getNotBefore().isAfter(instant.plus(Duration.ofSeconds(CONDITIONS_SECOND_RANGE)))) {
            throw (new InvalidFieldException("The assertion has been issued in the future. Current time in server is: "
                    + instant + " However, the starting time of your assertion is: " + assertion.getConditions().getNotBefore()));
        }
    }

    public static void validateNotOnOrAfterValue(Assertion assertion) throws InvalidFieldException {
        var instant = Instant.now();
        if (assertion.getConditions().getNotOnOrAfter().isBefore(instant.minus(Duration.ofSeconds(CONDITIONS_SECOND_RANGE)))) {
            throw (new InvalidFieldException("The assertion is not valid now. Current time in server is: " + instant
                    + " However, the ending time of your assertion is: " + assertion.getConditions().getNotOnOrAfter()));
        }
    }

    public static void validateTimeSpanForHCP(Assertion assertion) throws InvalidFieldException {
        if (assertion.getConditions().getNotBefore().isBefore(assertion.getConditions().getNotOnOrAfter().minus(Duration.ofHours(HCP_MAXIMUM_TIME_SPAN)))) {
            throw (new InvalidFieldException("Maximum time span for HCP Identity Assertion can be at most 4 hours."));
        }
    }

    public static void validateTimeSpanForTRC(Assertion assertion) throws InvalidFieldException {
        if (assertion.getConditions().getNotBefore().isBefore(assertion.getConditions().getNotOnOrAfter().minus(Duration.ofHours(TRC_MAXIMUM_TIME_SPAN)))) {
            throw (new InvalidFieldException("Maximum time span for TRC Assertion can be at most 2 hours."));
        }
    }

    public static void validateAuthnContextClassRefValueForHCP(Assertion assertion) throws InvalidFieldException {
        if (assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getURI() == null) {
            throw (new InvalidFieldException("AuthnContextClassRef should be filled."));
        } else if (!TwoFactorAuthentication.getAuthTypeValues()
                .contains(assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getURI())) {
            throw (new InvalidFieldException("AuthnContextClassRef element must be a Two Factor Authentication token."));
        }
    }

    public static void validateAuthnContextClassRefValueForTRC(Assertion assertion) throws InvalidFieldException {
        if (assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getURI() == null) {
            throw (new InvalidFieldException("AuthnContextClassRef must be 'urn:oasis:names:tc:SAML:2.0:ac:classes:PreviousSession'"));
        }
        if (!StringUtils.equals(assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getURI(), "urn:oasis:names:tc:SAML:2.0:ac:classes:PreviousSession")) {
            throw (new InvalidFieldException("AuthnContextClassRef must be 'urn:oasis:names:tc:SAML:2.0:ac:classes:PreviousSession'"));
        }
    }

    public static void validateAssertionIdRefValue(Assertion assertion) throws InvalidFieldException {
        if (org.apache.commons.lang.StringUtils.isBlank(assertion.getAdvice().getAssertionIDReferences().get(0).getValue())) {
            throw (new InvalidFieldException("AssertionIdRef should be filled."));
        }
    }
}
