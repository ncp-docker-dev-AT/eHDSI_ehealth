package eu.esens.abb.nonrep;

public class EnforcePolicyException extends Exception {
    
    private static final long serialVersionUID = -6196729998607048379L;

    public EnforcePolicyException() {
    }

    public EnforcePolicyException(String message) {
        super(message);
    }

    public EnforcePolicyException(Throwable cause) {
        super(cause);
    }

    public EnforcePolicyException(String message, Throwable cause) {
        super(message, cause);
    }
}
