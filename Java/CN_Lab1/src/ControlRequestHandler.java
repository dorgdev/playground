import java.io.BufferedReader;

/**
 * Handles CTRL messages containing control commands for the application.
 * For the list of supported commands, see the {@link Command} enum.
 */
public class ControlRequestHandler extends AbstractRequestHandler {

	/** The response message when STOP request's processing is done. */
	public static final String SERVER_STOPPED = 
			"Stopped the server. Accepting only control messages now.\n";
	/** The response message when START request's processing is done. */
	public static final String SERVER_STARTED =
			"Started the server. Accepting all messages now.\n";
	/** The response message when QUIT request's processing is done. */
	public static final String SERVER_SHUT_DOWN = 
			"Server is shutting down gracefully.\n";
	/**
	 * Valid command types.
	 */
	public enum Command {
		/** Asks the server for statistics collected so far. */
		STATS,
		/** Asks the server to start accepting new client requests. */
		START,
		/** Asks the server to stop accepting requests other than CTRL messages. */
		STOP,
		/** Asks the server to shut down gracefully. */
		QUIT
	}

	/**
	 * Creates a new {@link ControlRequestHandler} from the server's options.
	 * @param options The {@link ServerOptions} used for initialization.
	 */
	public ControlRequestHandler(ServerOptions options) {
	  super(options);
  }
	
	@Override
	protected HttpResponse doHandleRequest(BufferedReader reader,
	    HttpRequest request) throws HandlingException {
		// Control responses are always plain text, so override the guessed type.
		request.contentType = HttpRequest.ContentType.TEXT;
		// Parse the command (its value is the HTTP resource).
		Command command;
		try {
			command = Command.valueOf(request.resource.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new HandlingException("Unknown command type: " + request.resource,
					HandlingException.ErrorCode.BAD_REQUEST);
		}
		// Handle the request with accordance to its type.
		switch (command) {
		case STATS:
			return generateStatisticsPage(request);
		case START:
			return startWebServer(request);
		case STOP:
			return stopWebServer(request);
		case QUIT:
			return shutDownWebServer(request);
		}
		// We don't know how to handle such a request.
		throw new HandlingException("Unhandled command type: " + request.resource,
				HandlingException.ErrorCode.NOT_IMPLEMENTED);
	}
	
	/**
	 * (Re)starts the web server.
	 * @param request The original client's request.
	 * @return A response object to reply to the client with.
	 */
	private HttpResponse startWebServer(HttpRequest request) {
		// Clears the proxy's white list.
		RequestHandlerProxy.getInstance().clearWhiteList();
		// Reply with a suitable answer.
		HttpResponse response = new HttpResponse();
		response.setBuffer(SERVER_STARTED.getBytes(), request);
		return response;
	}

	/**
	 * Stops the web server.
	 * @param request The original client's request.
	 * @return A response object to reply to the client with.
	 */
	private HttpResponse stopWebServer(HttpRequest request) {
		// Add control messages to the proxy's white list.
		RequestHandlerProxy.getInstance().addToWhiteList(HttpRequest.Type.CTRL);
		// Reply with a suitable answer.
		HttpResponse response = new HttpResponse();
		response.setBuffer(SERVER_STOPPED.getBytes(), request);
		return response;
	}

	/**
	 * Shuts down the {@link WebServer} gracefully.
	 * @param request The original client's request.
	 * @return A response object to reply to the client with.
	 * @throws HandlingException In case of an invalid web-server operation.
	 */
	private HttpResponse shutDownWebServer(HttpRequest request) 
			throws HandlingException {
		// Asks the web server to shut down.
		if (options.getWebServer() == null) {
			throw new HandlingException("Cannot shut down server.",
					HandlingException.ErrorCode.NOT_IMPLEMENTED);
		}
		options.getWebServer().shutDown();
		// Reply with a suitable answer.
		HttpResponse response = new HttpResponse();
		response.setBuffer(SERVER_SHUT_DOWN.getBytes(), request);
		return response;
	}
	
	/**
	 * Creates an HTTP response with statistics of the {@link WebServer} so far.
	 * @param request The original client's request.
	 * @return A response object to reply to the client with.
	 */
	private HttpResponse generateStatisticsPage(HttpRequest request) {
		// Gather statistics information.
		// Reply with a suitable answer.
		HttpResponse response = new HttpResponse();
		String stats = StatsCollector.getInstance().toString();
		response.setBuffer(stats.getBytes(), request);
		return response;
	}
}
