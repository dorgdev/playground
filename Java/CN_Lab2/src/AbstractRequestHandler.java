import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An abstract implementation of the {@link RequestHandler} interface.
 * The {@link AbstractRequestHandler} prints the content of the HTTP headers
 * and invokes the internal handling of the deriving class.
 * It also implements several utilities methods some deriving handlers use.
 */
public abstract class AbstractRequestHandler implements RequestHandler {
	
	/** The HTTP content-type header name. */
	public static final String CONTENT_TYPE_TAG = "content-type";
	/** The content-type in case of an image content. */
	public static final String CONTENT_TYPE_IMAGE = "image";
	/** The content-type in case of an HTML content. */
	public static final String CONTENT_TYPE_HTML = "text/html";

	/** The running options of the server. */
	protected ServerOptions options;

	/**
	 * Creating a new {@link AbstractRequestHandler} using the
	 * {@link ServerOptions} it runs with.
	 * @param options The options of operations.
	 */
	public AbstractRequestHandler(ServerOptions options) {
		this.options = options;
	}

	/**
	 * Handles internally the client's request. Each deriving class should
	 * implement this method.
	 * @param reader A reader from the client's socket.
	 * @param request The client's {@link HttpRequest}.
	 * @return A suitable HTTP response to the client
	 * @throws HandlingException
	 */
	protected abstract HttpResponse doHandleRequest(BufferedReader reader, 
			HttpRequest request) throws HandlingException;

	/**
	 * Handling the request by first printing to STDOUT its content, and then
	 * invoking the internal handling mechanism (which differs between
	 * different implementations of this class).
	 */
	@Override
	public void handleRequest(BufferedReader reader, OutputStream output,
			HttpRequest request) throws HandlingException {
		// Handle the request internally.
		HttpResponse response = doHandleRequest(reader, request);
		// Fill the content type.
		response.addHeader(CONTENT_TYPE_TAG, request.contentType.toString());
		// Print the response's header
		System.out.println("Replying to client with the following HTTP response:");
		System.out.println(response.toString());
		// Reply to the client.
		if (request.chunked) {
			replyInChunks(output, response);
		} else {
			reply(output, response);
		}
	}

	/**
	 * Fills the {@link HttpResponse} data with the requested resource. If given
	 * a <code>null</code> resource, the response will be filled with an ad-hoc
	 * built HTML with the {@link HttpRequest} parameters.
	 * @param request The client's request.
	 * @param response The designated response object.
	 * @throws HandlingException In case of a problem filling the data.
	 */
	protected void FillData(HttpRequest request, HttpResponse response)
	    throws HandlingException {
    response.setBuffer(options.getPageGenerator().generatePage(request), request);
	}

	/**
	 * Replies to the client with the given response.
	 * @param output An output stream to the client's socket.
	 * @param response The response to reply with.
	 * @throws HandlingException In case of a problem replying.
	 */
	protected void reply(OutputStream output, HttpResponse response)
			throws HandlingException {
		try {
			output.write(response.toString().getBytes());
			output.write(response.getBuffer());
			output.flush();
		} catch (IOException e) {
			throw new HandlingException("Error replying: " + e.getMessage(), e,
					HandlingException.ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Replies to the client with the given response. The reply is in chunks.
	 * @param output An output stream to the client's socket.
	 * @param response The response to reply with.
	 * @throws HandlingException In case of a problem replying.
	 */
	private void replyInChunks(OutputStream output, HttpResponse response) 
			throws HandlingException {
		try {
			// Write the headers part.
			output.write(response.toString().getBytes());
			byte[] buffer = response.getBuffer();
			int leftToWrite = buffer.length;
			// Keep writing as long as not all the data was sent.
			while (leftToWrite > 0) {
				int size = Math.min(leftToWrite, options.getMaxChunkSize());
				// The size of the next chunk in Hex + CRLF.
				output.write((Integer.toHexString(size)).getBytes());
				output.write(HttpResponse.CRLF.getBytes());
				// The chunked data + CRLF.
				output.write(buffer, buffer.length - leftToWrite, size);
				output.write(HttpResponse.CRLF.getBytes());
				leftToWrite -= size;
			}
			// The last 0 indicating the end of the chunked data + CRLF.
			output.write((String.valueOf(0) + HttpResponse.CRLF).getBytes());
			output.flush();
		} catch (IOException e) {
			throw new HandlingException("Error replying: " + e.getMessage(), e,
					HandlingException.ErrorCode.INTERNAL_SERVER_ERROR);
		}

	}
}
