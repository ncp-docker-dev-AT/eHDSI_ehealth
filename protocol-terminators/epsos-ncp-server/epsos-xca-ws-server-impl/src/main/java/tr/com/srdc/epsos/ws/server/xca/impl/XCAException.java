package tr.com.srdc.epsos.ws.server.xca.impl;

public class XCAException extends Exception {

    private static final long serialVersionUID = -8381001130860083595L;

    public XCAException(String message) {
        super(message);
    }

    public XCAException(XCAError message) {
        super(message.getCode());
    }
}
