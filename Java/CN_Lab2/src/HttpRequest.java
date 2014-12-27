import java.util.HashMap;
import java.util.Map;

/**
 * A POJO that holds HTTP request related info.
 */
public class HttpRequest {
	
	/**
	 * An enum reflecting the type of HTTP request.
	 */
	public enum Type {
		UNKNOWN,
		GET,
		POST,
		TRACE,
		HEAD,
		CTRL
	};
	
	/**
	 * An enum reflecting the content-type requested in the HTTP request.
	 */
	public enum ContentType {
		IMAGE("image"),
		HTML("text/html"),
		ICON("icon"),
		TEXT("text/plain"),
		OTHER("application/octet-stream");
		
		/** The content-type this enum value represents. */
		private String value;
		
		/**
		 * Creates a new enum value with the content-type it represents.
		 * @param value The content-type value.
		 */
		ContentType(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
		  return value;
		}
	}
	
	/** The type of HTTP request. */
	public Type type;
	/** The requested resource. */
	public String resource;
	/** The designated content type requested. */
	public ContentType contentType;
	/** The length of the request's content. */
	public int contentLength;
	/** The HTTP referrer from the request. */
	public String referer;
	/** The host field in the request */
	public String host;
	/** The HTTP user-agent from the request. */
	public String userAgent;
	/** The parameters specified in the request. */
	public Map<String, String> parameters;
	/** The raw header. */
	public StringBuilder headers;
	/** Whether the client specified chunked: yes */
	public boolean chunked;
	/** the size (in bytes) of the request (as read from the wire). */
	public long bytes;
	
	/**
	 * Creates a new {@link HttpRequest} instance.
	 */
	public HttpRequest() {
		type = Type.UNKNOWN;
		resource = "";
		contentType = ContentType.OTHER;
		contentLength = 0;
		referer = "";
		userAgent = "";
		host = "";
		parameters = new HashMap<String, String>();
		headers = new StringBuilder();
		chunked = false;
		bytes = 0;
  }
	
	/**
	 * Returns the lines of this {@link HttpRequest} (the original header).
	 */
	@Override
	public String toString() {
		return headers.toString();
	}
}
