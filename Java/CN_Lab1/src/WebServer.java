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
class WebServer implements WebServerInterface {

	/** The options which will be used to initialize the server's work. */
	private ServerOptions options;
	/** Whether the server should shut down. */
	private boolean shutDownFlag;
	/** The server socket used for listening to new clients. */
	private ServerSocket listener;

	/**
	 * Creates a new {@link WebServer} instance from a given instance of
	 * {@link ServerOptions}.
	 * @param options The options from which the instance is initialized.
	 */
	public WebServer(ServerOptions options) {
		this.options = options;
		shutDownFlag = false;
		listener = null;
	}

	@Override
	public synchronized void shutDown() { 
		if (shutDownFlag) {
			// Already shutting down, nothing to do.
			return;
		}
		shutDownFlag = true;
		// Print an informative message about the shut down.
		System.out.println("Shutting down gracefully. Stop accepting requests.");
		// if the connection is server-socket is alive already, close it to
		// break the new-client-acceptance blocking method.
		if (listener != null) {
			try {
				listener.close();
			} catch (IOException e) {
				// Ignore exception, the socket will become invalid anyhow.
			}
		}
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
	@Override
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
			// Check whether the server should quit. 
			synchronized (this) {
				if (shutDownFlag) {
					// Server was asked to shut down gracefully. Stop accepting requests.
					break;
				}
      }
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
		// Clear the queued tasks anbd wait for all handled requests to finish.
		System.out.println("Shutting down clients' handling queue.");
		try {
			pool.purge();
			pool.shutdown();
			// Wait for the all client request to finish (30 secs).
			pool.awaitTermination(30, TimeUnit.SECONDS);
		} catch (Exception e) {
			// Ignore second chance exception and exit as if no error occurred.
		}
	}
}
