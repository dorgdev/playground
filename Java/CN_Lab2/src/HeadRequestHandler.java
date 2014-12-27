import java.io.BufferedReader;

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
		// Creating the HTTP response.
		HttpResponse response = new HttpResponse();
		// Reading the requested data (for content-length updating).
    if (request.chunked) {
      response.addHeader(HttpResponse.TRANSFER_ENCODING_TAG,
          HttpResponse.CHUNKS_ENCODING);
    } else {
      HttpResponse dummyResponse = new HttpResponse();
      FillData(request, dummyResponse);
      response.addHeader(HeaderParser.CONTENT_LENGTH_TAG,
          String.valueOf(dummyResponse.getBuffer().length));
    }
		// Replying.
		return response;
	}
}
