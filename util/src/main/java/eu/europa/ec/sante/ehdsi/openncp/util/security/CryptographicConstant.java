package eu.europa.ec.sante.ehdsi.openncp.util.security;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;

/**
 *
 */
public class CryptographicConstant {

    // eHDSI DIGEST default value Algorithm
    public static final String ALGO_ID_DIGEST_SHA256 = DigestMethod.SHA256;

    // eHDSI SIGNATURE default values for Algorithms
    public static final String ALGO_ID_SIGNATURE_RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    public static final String ALGO_ID_SIGNATURE_DSA_SHA256 = "http://www.w3.org/2009/xmldsig11#dsa-sha256";

    // eHDSI CANONICALIZE default values for Algorithms
    public static final String ALGO_ID_C14N_INCL_OMIT_COMMENTS = CanonicalizationMethod.INCLUSIVE;
    public static final String ALGO_ID_C14N_INCL_WITH_COMMENTS = CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS;
    public static final String ALGO_ID_C14N_EXCL_OMIT_COMMENTS = CanonicalizationMethod.EXCLUSIVE;
    public static final String ALGO_ID_C14N_EXCL_WITH_COMMENTS = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;

    private CryptographicConstant() {
    }
}
