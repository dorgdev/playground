import java.io.BufferedReader;
import java.io.File;

/**
 * Handles HTTP HEAD requests.
 */
public class HeadRequestHandler extends AbstractRequestHandler {
	
	/**
	 * Creates a new {@link HeadRequestHandler} instance. 
	 * @param options The options controlling the server behavior.
	 */
	public HeadRequestHandler(ServerOptions options) {
		super(options);
	}

	@Override
	protected HttpResponse doHandleRequest(BufferedReader reader,
			HttpRequest request) throws HandlingException {
		// Validate the requested resource.
		File resource = validateResource(request.resource);
		// Creating the HTTP response.
		HttpResponse response = new HttpResponse();
		// Reading the requested data (for content-length updating).
		String contentLength = String.valueOf(readFully(resource).length);
		response.addHeader(HeaderParser.CONTENT_LENGTH_TAG, contentLength);
		// Replying.
		return response;
	}
}
