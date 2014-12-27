import java.io.BufferedReader;

/**
 * Handles HTTP GET requests.
 */
public class GetRequestHandler extends AbstractRequestHandler {
	
	/**
	 * Creates a new {@link GetRequestHandler} instance. 
	 * @param options The options controlling the server behavior.
	 */
	public GetRequestHandler(ServerOptions options) {
		super(options);
	}

	@Override
	protected HttpResponse doHandleRequest(BufferedReader reader, 
			HttpRequest request) throws HandlingException {
		// Creating the HTTP response.
		HttpResponse response = new HttpResponse();
		// Reading the requested data.
		FillData(request, response);
		// Replying.
		return response;
	}
}
