package epsos.ccd.posam.tm.exception;

import eu.europa.ec.sante.ehdsi.constant.error.ITMTSAMError;
import eu.europa.ec.sante.ehdsi.constant.error.TMError;

/**
 * Transformation Manager Exception class.<br>
 * Enables throwing TM specific Exception during processing.
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 * @version 1.4, 2010, 20 October
 */
public class TMException extends Exception {

	private ITMTSAMError reason;

	public TMException(TMError reason) {
		this.reason = reason;
	}
	
	public TMException(TmErrorCtx reason) {
		this.reason = reason;
	}


	public ITMTSAMError getReason() {
		return reason;
	}

	@Override
	public String toString() {
		return reason.getCode() + ": " + reason.getDescription();
	}
}
