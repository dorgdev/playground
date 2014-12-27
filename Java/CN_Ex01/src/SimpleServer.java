// Computer Networks - Exercise 2
// Students: Dor Gross (039344999) & Efrat Guttman (029990975)

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class contains a main method, which starts a simple TCP server, which
 * is responsible of listening to incoming TCP connections and printing the
 * data received from them (differently for HTTP and non-HTTP requests).
 */
public class SimpleServer {
	
	/** The message to display whenever a new connection is accepted */
	public static final String START_MSG = 
			"November 2012: Dor Gross 039344999 & Efrat Guttman 029990975";

	/** The default port to which the server will listen. */
	public static final int DEFAULT_PORT = 5000;

	/** Maximum number of lines to read when handling a non-HTTP request. */
	public static final int MAX_NUM_LINES_NON_HTTP = 12;

	/** The regular expression matching the first line of an HTTP message. */
	public static final String HTTP_REGEX = ".+ .+ [H|h][T|t][T|t][P|p]/1.\\d";

	/** A regex matching engine, identifying HTTP opening lines. */
	public static final Pattern HTTP_PATTERN = Pattern.compile(HTTP_REGEX);
	
	/** The port's number to which the instance will listen. */
	private int port;
	
	/**
	 * Creates and runs a {@link SimpleServer} instance.
	 * @param args Program's arguments. Unused.
	 */
	public static void main(String[] args) {
		SimpleServer server = new SimpleServer(DEFAULT_PORT );
		server.run();
	}

	/**
	 * Creates a new {@link SimpleServer}, using the port it will listen to.
	 * @param port The port to which the server will listen. 
	 */
	public SimpleServer(int port) {
		this.port = port;
	}
	
	/**
	 * Starts accepting and handling incoming requests.
	 */
	public void run() {
		// Creating the server socket to listen to incoming connection.
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException ex) {
			System.err.println(
					"Failed to listen on port " + port + ": " + ex.getMessage());
			return;
		}
		// Keep getting new requests forever...
		while (true) {
			Socket socket = null;
			try {
				// Wait for a new connection.
				socket = serverSocket.accept();
			} catch (IOException ex) {
				// If we failed to build the socket, print an error and try 
				// listening again.
				System.err.println("Error accpeting a new socket: " + ex.getMessage());
				continue;
			}
			// Handle the client.
			handleClient(socket);
		}
		// Usually, we would have close the server socket here, but for the sake
		// of the exercise, which will never reach this code (due to the run-true
		// loop), we will omit this code here.
	}
	
	/**
	 * Handles a single client represented by its socket.
	 * @param socket The client's socket
	 */
	private void handleClient(Socket socket) {
		try {
			// Create a list to hold all the input lines.
			List<String> lines = new ArrayList<String>();
			// Construct a reader from the connection's input.
			BufferedReader reader = 
					new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Read the first line and validate it.
			String line = reader.readLine();
			if (line == null) {
				// Received a connection with no data.
				reader.close();
				return;
			}
			lines.add(line);
			
			// Check if it's an HTTP request.
			boolean isHttp = HTTP_PATTERN.matcher(line).matches();
			// Read the input lines according to the input type - if it's an HTTP
			// request, read it to its end, otherwise, read the first 12 lines.
			line = reader.readLine();
			while (line != null && (!isHttp || !line.equals(""))) {
				lines.add(line);
				// When handling an non-HTTP input, stop after 12 lines.
				if (!isHttp && lines.size() >= MAX_NUM_LINES_NON_HTTP) {
					break;
				}
				line = reader.readLine();
			}
			
			// Print the START_MSG followed by the input lines.
			System.out.println(START_MSG);
			for (String saved_line : lines) {
				System.out.println(saved_line);
			}
			
			// Close the connection.
			reader.close();
			socket.close();
		} catch (IOException ex) {
			System.err.println("An error occured: " + ex.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException ex) {
				// Ignore second chance exception, nothing to do with it.
			}
		}
	}
}
