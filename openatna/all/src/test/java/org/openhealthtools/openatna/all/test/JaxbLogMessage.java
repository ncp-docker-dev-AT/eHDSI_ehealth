package org.openhealthtools.openatna.all.test;

import org.openhealthtools.openatna.anom.AtnaMessage;
import org.openhealthtools.openatna.anom.JaxbIOFactory;
import org.openhealthtools.openatna.audit.process.AtnaLogMessage;

/**
 * @author Andrew Harrison
 */
public class JaxbLogMessage extends AtnaLogMessage {

    public JaxbLogMessage() {
        super(new JaxbIOFactory());
    }

    public JaxbLogMessage(AtnaMessage message) {
        super(message, new JaxbIOFactory());
    }
}
