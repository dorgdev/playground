import java.io.BufferedReader;


public class TraceRequestHandler extends AbstractRequestHandler {
	
	/**
	 * Creates a new {@link GetRequestHandler} instance. 
	 * @param options The options controlling the server behavior.
	 */
	public TraceRequestHandler(ServerOptions options) {
		super(options);
	}

	@Override
	protected HttpResponse doHandleRequest(BufferedReader reader, 
			HttpRequest request) throws HandlingException {
		// Creating the HTTP response.
		HttpResponse response = new HttpResponse();
		// Fill the response with the content of the received header.
		response.setBuffer(request.toString().getBytes(), request);
		// Replying.
		return response;
	}
}



