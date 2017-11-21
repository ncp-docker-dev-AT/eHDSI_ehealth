package tr.com.srdc.epsos.consent;

import urn.oasis.names.tc.xacml3.DecisionType;
import urn.oasis.names.tc.xacml3.ResponseType;

public class ResponseHandler {

    private ResponseHandler() {
    }

    public static boolean handleResponse(ResponseType response) {

        return response.getResult().get(0).getDecision().compareTo(DecisionType.PERMIT) == 0;
    }
}
