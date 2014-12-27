import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Executor;

/**
 * Handles a single client request. Implements {@link Runnable} to be invoked
 * under the {@link Executor} of the {@link WebServer}.
 * Once created, the {@link ClientHandler} is the sole responsible for the
 * client's socket, and the only one authorized to close it (or its streams).
 */
public class ClientHandler implements Runnable {

	/** The CSS style for an error response. */
	public static final String CSS_ERROR_STYLE =
			"background: url(/oops.jpg); background-size: 25%;";
	
	/** The client's socket. */
	private Socket socket;
	
	/**
	 * Creates a new {@link ClientHandler} instance from the client's socket.
	 * @param socket The client's socket.
	 */
	public ClientHandler(Socket socket) {
		this.socket = socket;
	}
	
	/**
	 * The {@link Runnable} main method. Calls the client handling code.
	 * In case of an error, the handler will try to reply to the client with an
	 * error message, close the socket and the handling will quietly exit after
	 * printing the error message (internally to STDERR).
	 */
	@Override
	public void run() {
		try {
			System.out.println("Handling a new client.");
			handle();
		} catch (HandlingException e) {
			HandlingException.ErrorCode code = e.getCode();
			// Print (internally) a message regarding the error.
			System.err.println("An error occured during the client handling (" + 
						code.getCode() + "): " + e.getMessage());
			// Try to reply to the client (in case the socket is not closed already).
			if (!socket.isClosed()) {
				replyWithError(code);
			}
		} finally {
			try {
				if (!socket.isClosed()) {
					socket.close();
				}
			} catch (IOException e) {
				// Ignore second chance exception. Not really much we can do. 
			}
		}
	}

	/**
	 * Replies to the client with an appropriate error message (with accordance
	 * to the error that happened).
	 * @param code The error code referring to the problem that occured.
	 */
	private void replyWithError(HandlingException.ErrorCode code) {
		try {
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			// Print the response (the error code) header.
			System.out.println(code.toString());
			// Build the response.
			StringBuilder data = new StringBuilder(); 
			data.append("<html>\n")
			    .append("	 <head/>\n")
				  .append("	 <body style=\"").append(CSS_ERROR_STYLE).append("\">\n")
			    .append("    <font align=\"center\" color=\"#FF3333\"><b>\n")
			    .append("      <u><h1>ERROR!</h1></u>\n")
			    .append("      <h2>").append(code.toString()).append("</h2>\n")
			    .append("    </h2></b></font>\n")
			    .append("    <a href=\"/oops.jpg\" border=\"1\"/>\n")
			    .append("	 </body>\n")
			    .append("</html>");
			// First, reply with the error code.
			writer.write(code.toString() + HttpResponse.CRLF);
			// Add a custom response (for supporting browsers).
			writer.write(data.toString());
			writer.flush();
		} catch (IOException e) {
			// Ignore second chance exception. Not really much we can do. 
		}
	}
	
	/**
	 * Performs the main handling of a client:
	 * <ol>
	 *   <li> Parse the request's header.
	 *   <li> Run the requested command.
	 * </ol>
	 */
	private void handle() throws HandlingException {
		try {
			HttpRequest request = new HttpRequest();
			// Create a reader and a writer.
			BufferedReader reader = 
			    new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStream output = socket.getOutputStream();
			// Parse the request's headers.
			HeaderParser.parse(reader, request);
			// Print the header received.
			System.out.println("An HTTP request received from a client:");
			System.out.println(request.toString());
			// Handle the request accordingly.
			RequestHandlerProxy.getInstance().handleRequest(reader, output, request);
		} catch (HandlingException e) {
			// Propagate any *expected* exception.
			throw e;
		} catch (Exception e) {
			// Wrap any unexpected exception with an internal-error code.
			throw new HandlingException(e,
					HandlingException.ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
}
