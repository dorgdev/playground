import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The main server of the application.
 * Listens to incoming client requests and assign {@link ClientHandler}s to
 * each one of them (which later be handled in their original order).
 */
class WebServer {

	/** The options which will be used to initialize the server's work. */
	private ServerOptions options;
	/** The server socket used for listening to new clients. */
	private ServerSocket listener;

	/**
	 * Creates a new {@link WebServer} instance from a given instance of
	 * {@link ServerOptions}.
	 * @param options The options from which the instance is initialized.
	 */
	public WebServer(ServerOptions options) {
		this.options = options;
		listener = null;
	}

	/**
	 * Runs the main logic of the {@link WebServer}. Generally:
	 * <ol>
	 *   <li>Creates the requests' {@link Executor}.
	 *   <li>Listens to an incoming request.
	 *   <li>Creates a {@link ClientHandler} to handle the request.
	 *   <li>Executes the {@link ClientHandler} in the {@link Executor}.
	 *   <li>Goes back to (2) above.
	 * </ol>
	 * <b>Note</b>:
	 * This method could only be invoked once. Following calls are no-op.
	 */
	public void run() {
		synchronized (this) {
			if (listener != null) {
				return;
			}
			// Create the listener. Failure should exit quietly.
			System.out.println("Start listening on port " + options.getPort());
			try {
				listener = new ServerSocket(options.getPort());
			} catch (IOException e) {
				System.err.println("Could not create ServerSocket: " + e.getMessage());
				return;
			}
		}
		// Initialize a thread pool for incoming connections handling.
		ThreadPoolExecutor pool = new ThreadPoolExecutor(options.getMaxThreads(), 
						options.getMaxThreads(), Integer.MAX_VALUE,
						TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		// Start handling incoming request.
		while (true) {
			Socket connection;
			try {
				// Wait for a request.
				connection = listener.accept();
				// Create a handler for it.
				ClientHandler currentClient = new ClientHandler(connection);
				// Add it to the executor.
				pool.execute(currentClient);
			} catch (IOException e) {
				// Also print the error if we intentionally closed the listener (to
				// be on the safe side in case an external problem occurred in the
				// same time).
				System.err.println(
						"Failed listening to a client connection: " + e.getMessage());
			}
		}
	}
}
