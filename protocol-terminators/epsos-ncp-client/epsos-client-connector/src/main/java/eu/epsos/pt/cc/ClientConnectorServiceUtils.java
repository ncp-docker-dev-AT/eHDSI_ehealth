package eu.epsos.pt.cc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.axis2.AxisFault;

import epsos.openncp.protocolterminator.clientconnector.GeneralFault;
import epsos.openncp.protocolterminator.clientconnector.GeneralFaultDocument;
import epsos.openncp.pt.client.GeneralFaultMessage;
import eu.epsos.exceptions.NoPatientIdDiscoveredException;

public class ClientConnectorServiceUtils {
	
	private static final String DEFAULT_ERROR_CODE = "1000";
	private static final String NoPatientIdDiscovered = "4707";
	
	public static AxisFault createGeneralFaultMessage(Throwable e) {
		
		if (e instanceof AxisFault) {
	        return (AxisFault) e;
	    }
		String errorCode = DEFAULT_ERROR_CODE;
		String faultMessage = "";
	   	Throwable t = e;
		if (e instanceof InvocationTargetException) {
	        t = ((InvocationTargetException) e).getTargetException();
	    } else if (e instanceof UndeclaredThrowableException) {
	        t = ((UndeclaredThrowableException) e).getCause();
	    }else if(t instanceof NoPatientIdDiscoveredException) {
			errorCode = NoPatientIdDiscovered;
			faultMessage = "There is no patient discovered having the ID specified!";
		}
		
		GeneralFault generalFault = GeneralFault.Factory.newInstance();
				
		generalFault.setFaultMessage(faultMessage);
		
		GeneralFaultDocument generalFaultDocument = GeneralFaultDocument.Factory.newInstance();
		generalFaultDocument.setGeneralFault(generalFault);
		
		GeneralFaultMessage exception = new GeneralFaultMessage(errorCode, t);
		exception.setFaultMessage(generalFaultDocument);
		
	    return AxisFault.makeFault(exception);
	}
	
}
