package ca.ericbannatyne.nfa;

/**
 * This exception should be thrown when an attempt is made to add a start, final
 * or current state to an NFA that is not already an available state of that
 * NFA.
 */
public class UnknownStateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4441799907952202543L;

	/**
	 * 
	 */
	public UnknownStateException() {
		super();
	}

	/**
	 * @param message
	 */
	public UnknownStateException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UnknownStateException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnknownStateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public UnknownStateException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
