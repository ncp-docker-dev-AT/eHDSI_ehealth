package eu.epsos.pt.cc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDataSource;
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
	private static final String NoPatientIdDiscovered = "4707";
	private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory(); 
	
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
	    	response = OM_FACTORY.createOMElement(TMSConstants.NO_PATIENT_ID_DISCOVERED);
			errorCode = NoPatientIdDiscovered;
			faultMessage = "There is no patient discovered having the ID specified!";
			response.setText(faultMessage);
		}else if(t instanceof XCAException) {
	    	response = OM_FACTORY.createOMElement(TMSConstants.XCA_EXCEPTION);
			errorCode = t.getMessage();
			faultMessage = "XCA Exception";
			response.setText(faultMessage);
		}else if(t instanceof XDRException) {
	    	response = OM_FACTORY.createOMElement(TMSConstants.XDR_EXCEPTION);
			errorCode = t.getMessage();
			faultMessage = "XDR Exception";
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
	
	
	interface TMSConstants { 
		
		public static final String TASK_NAMESPACE = "http://clientconnector.protocolterminator.openncp.epsos/"; 
		public static final String TASK_NAMESPACE_PREFIX = "soapenv"; 
		    
		public static final QName NO_PATIENT_ID_DISCOVERED = new QName(TASK_NAMESPACE, "noPatientIdDiscovered", TASK_NAMESPACE_PREFIX); 
		public static final QName XCA_EXCEPTION = new QName(TASK_NAMESPACE, "xcaException", TASK_NAMESPACE_PREFIX); 
		public static final QName XDR_EXCEPTION  = new QName(TASK_NAMESPACE, "xdrException", TASK_NAMESPACE_PREFIX); 
		
	    public static final QName INVALID_INPUT_FORMAT = new QName(TASK_NAMESPACE, "invalidInputMessageFault", TASK_NAMESPACE_PREFIX); 
	    public static final QName UNAVAILABLE_TASK = new QName(TASK_NAMESPACE, "unavailableTaskFault", TASK_NAMESPACE_PREFIX); 
	    public static final QName UNAVAILABLE_ATTACHMENT = new QName(TASK_NAMESPACE, "unavailableAttachmentFault", TASK_NAMESPACE_PREFIX); 
	    public static final QName INVALID_TOKEN = new QName(TASK_NAMESPACE, "invalidParticipantTokenFault", TASK_NAMESPACE_PREFIX); 
	    public static final QName ACCESS_DENIED = new QName(TASK_NAMESPACE, "accessDeniedFault", TASK_NAMESPACE_PREFIX); 
	}
}

