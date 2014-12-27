import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A singleton proxy for HTTP {@link RequestHandler}s.
 * Proxies the calls to the correct handler according to the
 * {@link HttpRequest} type (Get, Post, Trace, etc.).
 */
public final class RequestHandlerProxy implements RequestHandler {

	/** The sole and single instance of this class. */
	private static RequestHandlerProxy instance = new RequestHandlerProxy();
	/** The internal mapping from requests types to their handlers. */
	private Map<HttpRequest.Type, RequestHandler> handlers;
	
	/**
	 * @return The single instance of this class.
	 */
	public static RequestHandlerProxy getInstance() {
		return instance;
	}

	/**
	 * Proxies the call to the correct handler.
	 */
	@Override
	public void handleRequest(BufferedReader reader, OutputStream output,
			HttpRequest request) throws HandlingException {
		// Make sure we have a handler that can handle this type of request.
		if (!handlers.containsKey(request.type)) {
			throw new HandlingException("Unsupported command type: " + request.type,
					HandlingException.ErrorCode.NOT_IMPLEMENTED);
		}
		// Get the handler and handle the request.
		RequestHandler actualHandler = handlers.get(request.type);
		actualHandler.handleRequest(reader, output, request);
	}
	
	/**
	 * A private CTOR for the singleton pattern.
	 * Build the internal mapping of request's type to matching handler.
	 * <b>Note</b>: To support more request, simply add an handler here. 
	 */
	private RequestHandlerProxy() {
		handlers = new HashMap<HttpRequest.Type, RequestHandler>();
	}
		
	/**
	 * Sets a new handler for a specific HTTP method. Overrides previous handler
	 * in case existed.
	 * @param type The HTTP method type to set its handler.
	 * @param handler The {@link RequestHandler} to use for that method.
	 */
	public void setHandler(HttpRequest.Type type, 
			RequestHandler handler) {
		handlers.put(type, handler);
	}
}
