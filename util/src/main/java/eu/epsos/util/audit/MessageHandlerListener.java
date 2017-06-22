package eu.epsos.util.audit;

import java.io.Serializable;

public interface MessageHandlerListener {

    boolean handleMessage(Serializable message);
}
