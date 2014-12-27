import java.io.BufferedReader;
import java.io.IOException;

/**
 * Handles HTTP POST requests.
 */
public class PostRequestHandler extends AbstractRequestHandler {

	/**
	 * Creates a new {@link PostRequestHandler} instance. 
	 * @param options The options controlling the server behavior.
	 * */
	public PostRequestHandler(ServerOptions options) {
		super(options);
	}

	/**
	 * In addition to reading the requested resource and replying, also parses
	 * the POSTed parameters (further reads from the connection).
	 * @return 
	 */
	@Override
	protected HttpResponse doHandleRequest(BufferedReader reader,
			HttpRequest request) throws HandlingException {
		// Try to parse the POSTed parameters (if exist).
		if (request.contentLength > 0) {
			try {
				// There should be parameters to parse.
				char[] buff = new char[request.contentLength];
				int dataRead = 0;
				while (dataRead < request.contentLength) {
					dataRead += reader.read(
							buff, dataRead, request.contentLength - dataRead);
				}
				// Create a String from the data and parse it.
				HeaderParser.parseParameters(request, new String(buff));
			} catch (IOException e) {
				throw new HandlingException(
						"Failed to parse POST params: " + e.getMessage(), e,
						HandlingException.ErrorCode.BAD_REQUEST);
			}
		}
		// Creating the HTTP response.
		HttpResponse response = new HttpResponse();
		// Reading the requested data.
		FillData(request, response);
		// Replying.
		return response;
	}
}



