import java.io.Serializable;

/**
 * An {@link Exception} to be thrown internally in the {@link WebServer}.
 */
public class HandlingException extends Exception {

	/** The starting string of the every HTTP error line. */
	private static final String ERROR_PREFIX = "HTTP/1.1 ";
	/** The closing string of the every HTTP error line. */
	private static final String ERROR_SUFFIX = "\r\n";
	
	/**
	 * Denotes the possible errors raised inside our {@link WebServer}.
	 */
	public enum ErrorCode {
		BAD_REQUEST(400, "Bad Request"),
		FORBIDDEN(403, "Forbidden"),
		NOT_FOUND(404, "Not Found"),
		INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
		NOT_IMPLEMENTED(501, "Not Implemented");
		
		/** The error message returned to the client. */
		private String error;
		/** The error's code. */
		private int code;
		/**
		 * Build a new Enum value. Uses the error message that should be sent
		 * back to the client in case an instance of this class is thrown.
		 * @param errorString The error that should be sent to the client.
		 */
		private ErrorCode(int code, String error) {
			this.error = error;;
			this.code = code;
		}
		
		/**
		 * @return The HTTP error code represented by this enum.
		 */
		public int getCode() {
			return code;
		}
		
		@Override
		public String toString() {
			return ERROR_PREFIX + code + " " + error + ERROR_SUFFIX;
		}
	}
	
	/** A serial version ID since {@link Exception} is {@link Serializable}. */
  private static final long serialVersionUID = 4730099381562864282L;

  /** The error code that caused this instance to be thrown. */
  private ErrorCode code;
  
	/**
	 * @param code The cause for this exception to be thrown.
	 * @see Exception#Exception(String, Throwable)
	 */
	public HandlingException(String msg, Throwable throwable, ErrorCode code) {
	  super(msg, throwable);
	  this.code = code;
  }

	/**
	 * @param code The cause for this exception to be thrown.
	 * @see Exception#Exception(String)
	 */
	public HandlingException(String message, ErrorCode code) {
	  super(message);
	  this.code = code;
  }

	/**
	 * @param code The cause for this exception to be thrown.
	 * @see Exception#Exception(Throwable)
	 */
	public HandlingException(Throwable throwable, ErrorCode code) {
	  super(throwable);
	  this.code = code;
  }
	
	/**
	 * @return Returns the error code that caused this exception to be thrown.
	 */
	public ErrorCode getCode() {
		return code;
	}
}
