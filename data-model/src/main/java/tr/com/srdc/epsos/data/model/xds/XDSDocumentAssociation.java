package tr.com.srdc.epsos.data.model.xds;

/**
 * This class encapsulates a set of properties related to a document (Level 1 and 3), but not it's content.
 */
public class XDSDocumentAssociation {

    private XDSDocument cdaXML;
    private XDSDocument cdaPDF;

    public XDSDocument getCdaPDF() {
        return cdaPDF;
    }

    public void setCdaPDF(XDSDocument cdaPDF) {
        this.cdaPDF = cdaPDF;
    }

    public XDSDocument getCdaXML() {
        return cdaXML;
    }

    public void setCdaXML(XDSDocument cdaXML) {
        this.cdaXML = cdaXML;
    }
}
