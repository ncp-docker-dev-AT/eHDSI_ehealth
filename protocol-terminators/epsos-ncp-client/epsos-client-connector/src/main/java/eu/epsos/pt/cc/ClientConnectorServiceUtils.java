package eu.epsos.pt.cc;

import eu.epsos.exceptions.ExceptionWithContext;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class ClientConnectorServiceUtils {

    public static final String TASK_NAMESPACE = "http://clientconnector.protocolterminator.openncp.epsos/";
    public static final String TASK_NAMESPACE_PREFIX = "soapenv";
    private static final Logger logger = LoggerFactory.getLogger(ClientConnectorServiceUtils.class);
    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();

    private ClientConnectorServiceUtils() {
    }

    public static AxisFault createGeneralFaultMessage(Throwable e) {

        logger.info("AxisFault Builder: '{}'", e.getMessage());
        if (e instanceof AxisFault) {
            return (AxisFault) e;
        }
        String errorMessage = "";
        String errorCode = "";
        String faultMessage = "";
        OMElement response = null;
        Throwable throwable = e;
        AxisFault axisFault;
        if (e instanceof InvocationTargetException) {
            throwable = ((InvocationTargetException) e).getTargetException();
        } else if (e instanceof UndeclaredThrowableException) {
            throwable = e.getCause();
        } else if (throwable instanceof ExceptionWithContext) {
            response = OM_FACTORY.createOMElement(
                    new QName(TASK_NAMESPACE, throwable.getClass().getSimpleName(), TASK_NAMESPACE_PREFIX));
            // OpenNCP Error Code
            errorCode = ((ExceptionWithContext) throwable).getOpenncpErrorCode().getCode();
            // OpenNCP additional information
            errorMessage = throwable.getMessage();
            // National Country additional information
            faultMessage = ((ExceptionWithContext) throwable).getContext();
            response.setText(faultMessage);
        } else { //
            errorMessage = throwable.getMessage();
        }

        axisFault = new AxisFault(errorMessage, new QName(errorCode));
        axisFault.setDetail(response);

        return axisFault;
    }
}
