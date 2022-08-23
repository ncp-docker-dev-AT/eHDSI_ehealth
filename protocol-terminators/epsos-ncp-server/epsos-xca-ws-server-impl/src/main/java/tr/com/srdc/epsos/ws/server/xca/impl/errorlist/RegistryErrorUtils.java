package tr.com.srdc.epsos.ws.server.xca.impl.errorlist;

import eu.europa.ec.sante.ehdsi.constant.error.ITMTSAMError;
import eu.europa.ec.sante.ehdsi.constant.error.IheErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.openncp.pt.common.RegistryErrorSeverity;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryError;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

public class RegistryErrorUtils {

    private static final OMFactory omFactory = OMAbstractFactory.getOMFactory();
    private static final oasis.names.tc.ebxml_regrep.xsd.rs._3.ObjectFactory ofRs = new oasis.names.tc.ebxml_regrep.xsd.rs._3.ObjectFactory();

    public static void addErrorMessage(RegistryErrorList registryErrorList, OpenNCPErrorCode openncpErrorCode, String codeContext, String location, RegistryErrorSeverity severity) {
        registryErrorList.getRegistryError().add(createErrorMessage("[" + openncpErrorCode.getCode() + " - " + openncpErrorCode.getDescription() + "] " + codeContext, location, severity));
    }


    public static void addErrorOMMessage(OMNamespace ons, OMElement registryErrorList, OpenNCPErrorCode openncpErrorCode, String codeContext, RegistryErrorSeverity severity) {
        registryErrorList.addChild(createErrorOMMessage(ons, "[" + openncpErrorCode.getCode() + " - " + openncpErrorCode.getDescription() + "] " + codeContext, null, severity));
    }

    public static void addErrorOMMessage(OMNamespace ons, OMElement registryErrorList, IheErrorCode iheErrorCode, String codeContext, RegistryErrorSeverity severity) {
        registryErrorList.addChild(createErrorOMMessage(ons, "[" + iheErrorCode.getCode() + " - " + iheErrorCode.getDescription() + "] " + codeContext, null, severity));
    }

    public static void addErrorOMMessage(OMNamespace ons, OMElement registryErrorList, OpenNCPErrorCode openncpErrorCode, String codeContext, String location, RegistryErrorSeverity severity) {
        registryErrorList.addChild(createErrorOMMessage(ons, "[" + openncpErrorCode.getCode() + " - " + openncpErrorCode.getDescription() + "] " + codeContext, location, severity));
    }

    public static void addErrorOMMessage(OMNamespace ons, OMElement registryErrorList, ITMTSAMError error, String operationType, RegistryErrorSeverity severity) {
        registryErrorList.addChild(createErrorOMMessage(ons,
                "ECDATransformationHandler.Error." + operationType + "(" + error.getCode() + " / " + error.getDescription() + ")",
                null,
                severity));
    }

    private static RegistryError createErrorMessage(String codeContext, String location, RegistryErrorSeverity severity) {
        var registryError = ofRs.createRegistryError();
        registryError.setErrorCode(IheErrorCode.XDSRepositoryError.getCode());
        registryError.setLocation(location);
        registryError.setSeverity(severity.getText());
        registryError.setCodeContext(codeContext);
        return registryError;
    }

    private static OMElement createErrorOMMessage(OMNamespace ons, String codeContext, String location, RegistryErrorSeverity severity) {

        var registryError = omFactory.createOMElement("RegistryError", ons);
        registryError.addAttribute(omFactory.createOMAttribute("codeContext", null, codeContext));
        registryError.addAttribute(omFactory.createOMAttribute("errorCode", null, IheErrorCode.XDSRepositoryError.getCode()));
        String aux = severity != null? severity.getText() : null;
        registryError.addAttribute(omFactory.createOMAttribute("severity", null, aux));
        registryError.addAttribute(omFactory.createOMAttribute("location", null, location));
        return registryError;
    }
}
