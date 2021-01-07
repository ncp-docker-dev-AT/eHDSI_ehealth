package eu.epsos.pt.cc.dts.axis2;

import epsos.openncp.protocolterminator.clientconnector.RetrieveDocumentResponse;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;

/**
 * This is an Data Transformation Service providing functions to transform data into a RetrieveDocumentResponseDTS object.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class RetrieveDocumentResponseDTS {

    /**
     * Private constructor to disable class instantiation.
     */
    private RetrieveDocumentResponseDTS() {
    }

    public static RetrieveDocumentResponse newInstance(DocumentResponse documentResponse) {

        if (documentResponse == null) {
            return null;
        }

        final RetrieveDocumentResponse result = RetrieveDocumentResponse.Factory.newInstance();
        result.setReturn(DocumentDts.newInstance(documentResponse));

        return result;
    }
}
