import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
	/** When requesting the following resource, the handler will generate a
	 *  unique page presenting the request's parameters. */
	public static final String PARAMS_INFO_RES = "/params_info.html";
	/** The CSS style for the params_info ad-hoc page. */
	public static final String CSS_GANGNAM_STYLE =
			"background: url(/params_back.jpg) no-repeat; background-size: 100%;";

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
		OutputStream reponseOutputStream =
				new StatsCollector.ResponseOutputStream(response, output);
		if (request.chunked) {
			replyInChunks(reponseOutputStream, response);
		} else {
			reply(reponseOutputStream, response);
		}
		
		// If we got to this point, the request and response were handled
		// successfully, so report a successful request.
		StatsCollector.getInstance().reportSuccess(request, response);
	}

	/**
	 * Validates the request's resource is valid. Returns a {@link File} 
	 * pointer for the valid resource. Will return <code>null</code> in case
	 * the user had requested the params_info.html (see PARAMS_INFO).
	 * @param requestResource the HTTP request's resource.
	 * @return A {@link File} pointing to the valid resource.
	 * @throws HandlingException In case of an invalid resource.
	 * @see AbstractRequestHandler#PARAMS_INFO_RES
	 */
	protected File validateResource(String requestResource) 
			throws HandlingException {
		// Make sure the requested resource is in the resource library (disallow
		// the usage of ".." path descriptors.
		if (requestResource.contains("/../") ||
				requestResource.startsWith("../") ||
				requestResource.endsWith("/..") ||
				requestResource.equals("..")) {
			throw new HandlingException("Permission denied: " + requestResource,
					HandlingException.ErrorCode.FORBIDDEN);
		}
		// The only option to return null is if the user asks for params_info page.
		if (requestResource.equalsIgnoreCase(PARAMS_INFO_RES)) {
			return null;
		}
		String fileName = options.getRoot() + requestResource;
		File resource = new File(fileName);
		// If it's a directory, add the default page to the resource.
		if (resource.exists() && resource.isDirectory()) {
			fileName += File.separator + options.getDefaultPage();
			resource = new File(fileName);
		}
		// Make sure the resource exists.
		if (!resource.exists() || !resource.isFile()) {
			throw new HandlingException(
					"Could not find the requested resource: " + requestResource,
					HandlingException.ErrorCode.NOT_FOUND);
		}
		// Make sure we can read the resource.
		if (!resource.canRead()) {
			throw new HandlingException("Permission denied: " + requestResource,
					HandlingException.ErrorCode.NOT_FOUND);
		}
		return resource;
	}

	/**
	 * Fills the {@link HttpResponse} data with the requested resource. If given
	 * a <code>null</code> resource, the response will be filled with an ad-hoc
	 * built HTML with the {@link HttpRequest} parameters.
	 * @param request The client's request.
	 * @param response The designated response object.
	 * @param resource The requested resource, or <code>null</code> for 
	 * 				parameters ad-hoc page.
	 * @throws HandlingException In case of a problem filling the data.
	 */
	protected void FillData(HttpRequest request, HttpResponse response,
			File resource) throws HandlingException {
		byte[] buffer;
		if (resource != null) {
			buffer = readFully(resource);
		} else {
			StringBuilder data = new StringBuilder();
			data.append("<html>\n")
				  .append("	 <head/>\n")
				  .append("	 <body style=\"").append(CSS_GANGNAM_STYLE).append("\">\n")
				  .append("    <font align=\"center\" color=\"#FFAAAA\"><u><b><h1>")
				  .append("      Showing your parameters GANGNAM STYLE!")
				  .append("    </h1></b></u></font>")
				  .append("		 <table align=\"center\" border=\"1\" width=\"300\">\n")
				  .append("      <tr bgcolor=\"#FFAAAA\" align=\"center\">\n")
				  .append("        <td><b>Name</b></td>\n")
				  .append("        <td><b>Value</b></td>\n")
				  .append("      </tr>\n");
			for (String key : request.parameters.keySet()) {
				String value = request.parameters.get(key);
				data.append("      <tr align=\"center\" bgcolor=\"white\">\n")
				    .append("        <td>\n").append(key).append("</td>\n")
				    .append("				 <td>").append(value).append("</td>\n")
				    .append("			 </tr>\n");
			}
			data.append("		 </table>\n")
			    .append("	 </body>\n")
			    .append("</html>");
			buffer = data.toString().getBytes();
		}
		response.setBuffer(buffer, request);
	}

	/**
	 * Reads the content of the given file into a buffer and return the buffer.
	 * @param file The {@link File} to read.
	 * @return The content of the file.
	 * @throws HandlingException In case of a problem reading from the file.
	 */
	protected byte[] readFully(File file) throws HandlingException {
		try {
			int dataLen = (int)file.length();
			byte[] data = new byte[dataLen];

			FileInputStream fis = new FileInputStream(file);
			int readLen = 0;
			while (readLen < dataLen && fis.available() != 0) {
				fis.read(data, readLen, dataLen - readLen);
			}
			fis.close();

			return data;
		} catch (IOException e) {
			throw new HandlingException(
					"Failed reading from file: " + e.getMessage(), e,
					HandlingException.ErrorCode.INTERNAL_SERVER_ERROR);
		}
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
