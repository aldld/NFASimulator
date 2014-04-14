package ca.ericbannatyne.nfa;

/**
 * This exception should be thrown when an attempt is made to add a start or
 * final state to an NFA that is not already an available state of that NFA.
 */
public class InvalidStartOrFinalStateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4441799907952202543L;

	/**
	 * 
	 */
	public InvalidStartOrFinalStateException() {
		super();
	}

	/**
	 * @param message
	 */
	public InvalidStartOrFinalStateException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidStartOrFinalStateException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidStartOrFinalStateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public InvalidStartOrFinalStateException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
