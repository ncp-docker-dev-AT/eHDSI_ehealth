package eu.epsos.pt.cc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import epsos.openncp.protocolterminator.clientconnector.GeneralFault;
import epsos.openncp.protocolterminator.clientconnector.GeneralFaultDocument;
import epsos.openncp.pt.client.GeneralFaultMessage;
import eu.epsos.exceptions.NoPatientIdDiscoveredException;
import eu.epsos.exceptions.XCAException;
import eu.epsos.exceptions.XDRException;


public class ClientConnectorServiceUtils {
	private static final Logger logger = LoggerFactory.getLogger(ClientConnectorServiceUtils.class);
	private static final String DEFAULT_ERROR_CODE = "1000";
	private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
	public static final String TASK_NAMESPACE = "http://clientconnector.protocolterminator.openncp.epsos/";
	public static final String TASK_NAMESPACE_PREFIX = "soapenv";

	public static AxisFault createGeneralFaultMessage(Throwable e) {
		
		if (e instanceof AxisFault) {
	        return (AxisFault) e;
	    }
		String errorCode = DEFAULT_ERROR_CODE;
		String faultMessage = "";
		OMElement response = null;
	   	Throwable t = e;
	   	AxisFault axisFault = null;
		if (e instanceof InvocationTargetException) {
	        t = ((InvocationTargetException) e).getTargetException();
	    } else if (e instanceof UndeclaredThrowableException) {
	        t = ((UndeclaredThrowableException) e).getCause();
	    }else if(t instanceof NoPatientIdDiscoveredException) {
	    	response = OM_FACTORY.createOMElement(new QName(TASK_NAMESPACE, "noPatientIdDiscoveredException", TASK_NAMESPACE_PREFIX));
			errorCode = t.getMessage();
			faultMessage = ((NoPatientIdDiscoveredException) t).getContext();
			response.setText(faultMessage);
		}else if(t instanceof XCAException) {
	    	response = OM_FACTORY.createOMElement(new QName(TASK_NAMESPACE, "xcaException", TASK_NAMESPACE_PREFIX));
			errorCode = t.getMessage();
			faultMessage = ((XCAException) t).getContext();
			response.setText(faultMessage);
		}else if(t instanceof XDRException) {
	    	response = OM_FACTORY.createOMElement(new QName(TASK_NAMESPACE, "xdrException", TASK_NAMESPACE_PREFIX));
			errorCode = t.getMessage();
			faultMessage = ((XDRException) t).getContext();
			response.setText(faultMessage);
		}else { //
			errorCode = t.getMessage();
		}
		
		GeneralFault generalFault = GeneralFault.Factory.newInstance();
				
		generalFault.setFaultMessage(faultMessage);
		
		GeneralFaultDocument generalFaultDocument = GeneralFaultDocument.Factory.newInstance();
		generalFaultDocument.setGeneralFault(generalFault);
		
		GeneralFaultMessage exception = new GeneralFaultMessage(errorCode, t);
		exception.setFaultMessage(generalFaultDocument);
		
		axisFault = new AxisFault(errorCode, exception);
		axisFault.setDetail(response);
		
		return axisFault;
	}
}

