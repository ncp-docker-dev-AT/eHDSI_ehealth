package eu.epsos.validation.datamodel.common;

/**
 * This enumerator represents the two NCP Sides.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public enum NcpSide {

    NCP_A("NCP-A"),
    NCP_B("NCP-B");
    private String name;

    NcpSide(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }
}
