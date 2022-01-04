package eu.epsos.exceptions;

public abstract class ExceptionWithContext extends Exception{

    private static final long serialVersionUID = 1L;

    private String context;

    public ExceptionWithContext(String message) {
        super(message);
    }

    public ExceptionWithContext(String message, Throwable cause) {
        super(message, cause);
    }

    public ExceptionWithContext(Throwable cause) {
        super(cause);
    }

    public ExceptionWithContext(String message, String context) {
        super(message);
        this.context = context;
    }

    public ExceptionWithContext(String message, String context, Throwable cause) {
        super(message, cause);
        this.context = context;
    }

    public String getContext() {
        return context;
    }

}
