package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

public class XSDValidationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6854562291880477762L;

	private String message;

	public XSDValidationException(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
