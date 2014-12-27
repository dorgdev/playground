import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A singleton proxy for HTTP {@link RequestHandler}s.
 * Proxies the calls to the correct handler according to the
 * {@link HttpRequest} type (Get, Post, Trace, etc.).
 */
public final class RequestHandlerProxy implements RequestHandler {

	/** The sole and single instance of this class. */
	private static RequestHandlerProxy instance =
			new RequestHandlerProxy();
	
	/** The internal mapping from requests types to their handlers. */
	private Map<HttpRequest.Type, RequestHandler> handlers;
	/** A white list of allowed request types. When not empty, only request 
	 *  types showing in the list will be replied. */
	private Set<HttpRequest.Type> whiteList;
	
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
		// Make sure the type of operation is allowed.
		if (!(whiteList.isEmpty() || whiteList.contains(request.type))) {
			// The list is not empty, but doesn't contain the type - Forbidden.
			throw new HandlingException("Black listed operation: " + request.type,
					HandlingException.ErrorCode.FORBIDDEN);
		}
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
		// Using a thread-safe set for concurrency reasons.
		whiteList = new ConcurrentSkipListSet<HttpRequest.Type>();
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
	
	/**
	 * Adds a new type to the proxy's white list. A no-op in case the type is
	 * already in the white list.
	 * @param type The {@link HttpRequest.Type} to add.
	 */
	public void addToWhiteList(HttpRequest.Type type) {
		if (!whiteList.contains(type)) {
			whiteList.add(type);
		}
	}

	/**
	 * Clears the proxy's white list (which enables all supported operations).
	 * A no-op in case the white list is already empty.
	 */
	public void clearWhiteList() {
		whiteList.clear();
	}
}
