package se.sb.epsos.web.service;

public class AssertionException extends Exception {

    private static final long serialVersionUID = -4355500349072886802L;

    public AssertionException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
