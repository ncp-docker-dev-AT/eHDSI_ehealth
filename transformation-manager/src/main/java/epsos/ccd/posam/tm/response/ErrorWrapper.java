package epsos.ccd.posam.tm.response;

import eu.europa.ec.sante.ehdsi.constant.error.ITMTSAMError;

public class ErrorWrapper {

    private ITMTSAMError error;
    private String context;

    public ErrorWrapper(ITMTSAMError error, String context) {
        this.error = error;
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public ITMTSAMError getError() {
        return error;
    }
}
