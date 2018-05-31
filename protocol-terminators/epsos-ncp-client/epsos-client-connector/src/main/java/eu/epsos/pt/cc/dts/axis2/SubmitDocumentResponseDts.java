package eu.epsos.pt.cc.dts.axis2;

import epsos.openncp.protocolterminator.clientconnector.SubmitDocumentResponse;
import tr.com.srdc.epsos.data.model.XdrResponse;

/**
 * This is an Data Transformation Service. This provide functions to transform data into a SubmitDocumentResponseDts object.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class SubmitDocumentResponseDts {

    /**
     * Private constructor to disable class instantiation.
     */
    private SubmitDocumentResponseDts() {
    }

    public static SubmitDocumentResponse newInstance(XdrResponse xdrResponse) {

        final SubmitDocumentResponse result = SubmitDocumentResponse.Factory.newInstance();

        result.setResponseStatus(xdrResponse.getResponseStatus());

        return result;
    }
}
