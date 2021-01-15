package eu.epsos.pt.cc.dts;

import tr.com.srdc.epsos.data.model.GenericDocumentCode;

/**
 * This is an Data Transformation Service. This provide functions to transform data into a
 * tr.com.srdc.epsos.data.model.GenericDocumentCode object.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class GenericDocumentCodeDts {

    /**
     * Private constructor to disable class instantiation.
     */
    private GenericDocumentCodeDts() {
    }

    public static GenericDocumentCode newInstance(epsos.openncp.protocolterminator.clientconnector.GenericDocumentCode documentCode) {

        final GenericDocumentCode result = new GenericDocumentCode();

        result.setSchema(documentCode.getSchema());
        result.setValue(documentCode.getNodeRepresentation());

        return result;
    }
}
