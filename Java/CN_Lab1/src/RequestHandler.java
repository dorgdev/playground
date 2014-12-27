import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * An interface for handling HTTP requests.
 */
public interface RequestHandler {

	/**
	 * Handles a client's HTTP request.  Also receives the client's
	 * {@link Socket} in order to reply and/or read more data from the
	 * connection.
	 * @param reader A reader from the client's socket.
	 * @param output An output stream to the client's socket.
	 * @param request The client's parsed HTTP request.
	 * @throws HandlingException In case of a problem during the request.
	 */
	public void handleRequest(BufferedReader reader, OutputStream output, 
			HttpRequest request) throws HandlingException;
}
