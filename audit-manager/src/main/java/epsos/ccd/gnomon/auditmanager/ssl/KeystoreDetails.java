package epsos.ccd.gnomon.auditmanager.ssl;

import java.util.ArrayList;
import java.util.List;

/**
 * interface for classes that can retrieve details required for signing a jar or authentication.
 */
public class KeystoreDetails {

    private String keystoreLocation;
    private String keystorePassword;
    private String alias = "";
    private String keyPassword = null;
    private String keystoreType = "JKS";
    private String algType = "SunX509";
    private String authority = "";
    private List<String> authorizedDNs = new ArrayList<>();

    /**
     * create a KeystoreDetails for accessing a certificate
     *
     * @param keystoreLocation
     * @param keystorePassword
     * @param alias
     * @param keyPassword
     */
    public KeystoreDetails(String keystoreLocation, String keystorePassword, String alias, String keyPassword) {
        this.keystoreLocation = keystoreLocation;
        this.keystorePassword = keystorePassword;
        this.alias = alias;
        this.keyPassword = keyPassword;
    }

    public KeystoreDetails(String keystoreLocation, String keystorePassword, String alias) {
        this.keystoreLocation = keystoreLocation;
        this.keystorePassword = keystorePassword;
        this.alias = alias;
        this.keyPassword = keystorePassword;
    }

    /**
     * constructor used when loading details from file.
     */
    public KeystoreDetails() {
    }

    public String getKeystoreLocation() {
        return keystoreLocation;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getAlias() {
        return alias;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getAlgType() {
        return algType;
    }

    public void setAlgType(String algType) {
        this.algType = algType;
    }

    /**
     * combination of host (domain or IP) and port separated by a colon.
     *
     * @return
     */
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public void addAuthorizedDN(String dn) {
        if (!authorizedDNs.contains(dn)) {
            authorizedDNs.add(dn);
        }
    }

    public List<String> getAuthorizedDNs() {
        return authorizedDNs;
    }

    public void setAuthorizedDNs(List<String> authorizedDNs) {
        this.authorizedDNs = authorizedDNs;
    }
}
