import java.io.BufferedReader;
import java.io.File;

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
		// Validate the requested resource.
		File resource = validateResource(request.resource);
		// Creating the HTTP response.
		HttpResponse response = new HttpResponse();
		// Reading the requested data.
		FillData(request, response, resource);
		// Replying.
		return response;
	}
}
