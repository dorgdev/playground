import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Represents the options passed to the server.
 */
public class ServerOptions {

	/** Whether the options file is allowed to use default values. */
	public static final String ALLOW_DEFAULTS_TAG = "allowDefaults";
	/** The port's tag in the options file. */
	public static final String PORT_TAG = "port";
	/** The default port to use if not specified. */
	public static final String DEFAULT_PORT = "8080";
	/** The root's tag in the options file. */
	public static final String ROOT_TAG = "root";
	/** The default root to use if not specified. */
	public static final String DEFAULT_ROOT = "C:\\ServerRoot";
	/** The default page's tag in the options file. */
	public static final String DEFAULT_PAGE_TAG = "defaultPage";
	/** The default page to use if not specified. */
	public static final String DEFAULT_DEFAULT_PAGE = "index.html";
	/** The max threads' tag in the options file. */
	public static final String MAX_THREADS_TAG = "maxThreads";
	/** The default number of threads to use if not specified. */
	public static final String DEFAULT_MAX_THREADS = "10";
	/** The maximal chunk size in a chunked response. */
	public static final String MAX_CHUNK_SIZE_TAG = "maxChunkSize";
	/** The default maximal chunk size to use if not specified. */
	public static final String DEFAULT_MAX_CHUNK_SIZE = "50";

	/** The port to which the server should listen. */
	private int port;
	/** The root directory where the server's files should be. */
	private String root;
	/** The default page to present when no specific one is specified. */
	private String defaultPage;
	/** The maximal number of threads to use when handling clients' requests. */
	private int maxThreads;
	/** The maximal size of a chunk to use when handling chunks. */
	private int maxChunkSize;
	/** The web server interface for listening to internal requests. */
	private WebServerInterface webServer;

	/**
	 * Creates a new {@link ServerOptions} from the given {@link File}
   * If the file contains the {@link ServerOptions#ALLOW_DEFAULTS_TAG} tag in
   * it with the value "yes", missing options will get default values according
   * the the following defaults:
	 * <ol>
	 *   <li>{@link ServerOptions#DEFAULT_ROOT}
	 *   <li>{@link ServerOptions#DEFAULT_PORT}
	 *   <li>{@link ServerOptions#DEFAULT_MAX_THREADS} 
	 *   <li>{@link ServerOptions#DEFAULT_DEFAULT_PAGE}
	 *   <li>{@link ServerOptions#DEFAULT_MAX_CHUNK_SIZE}
	 * </ol>
	 * @param file The file to read the options from.
	 * @return A new instance of {@link ServerOptions} for a successful parsing,
	 *         or <code>null</code> in case a failure.
	 */
	public static ServerOptions parseFromFile(File file) {
		try {
			ServerOptions options = new ServerOptions();
			Properties prop = new Properties();
			prop.load(new FileInputStream(file));
			// Check if default values are allowed.
			boolean allowDefault = prop.containsKey(ALLOW_DEFAULTS_TAG) &&
					prop.getProperty(ALLOW_DEFAULTS_TAG).equalsIgnoreCase("yes");
			// Go over the fields and fill them.
			// PORT
			if (allowDefault || prop.containsKey(PORT_TAG)) {
				String port = prop.getProperty(PORT_TAG, DEFAULT_PORT);
				options.port = Integer.parseInt(port);
			} else {
				throw new IllegalArgumentException("Missing " + PORT_TAG + 
						" field in the configuration file.");
			}
			// ROOT
			if (allowDefault || prop.containsKey(ROOT_TAG)) {
				String root = prop.getProperty(ROOT_TAG, DEFAULT_ROOT);
				if (new File(root).isDirectory()) {
					options.root = root;
				} else {
					throw new IllegalArgumentException(
							"Given a root which is not a directory: " + root);
				}
			} else {
				throw new IllegalArgumentException("Missing " + ROOT_TAG + 
						" field in the configuration file.");
			}
			// DEFAULT PAGE
			if (allowDefault || prop.containsKey(DEFAULT_PAGE_TAG)) {
				options.defaultPage = 
						prop.getProperty(DEFAULT_PAGE_TAG, DEFAULT_DEFAULT_PAGE);
			} else {
				throw new IllegalArgumentException("Missing " + DEFAULT_PAGE_TAG + 
						" field in the configuration file.");
			}
			// MAX THREADS
			if (allowDefault || prop.containsKey(MAX_THREADS_TAG)) {
				String threads = prop.getProperty(MAX_THREADS_TAG, DEFAULT_MAX_THREADS);
				options.maxThreads = Integer.parseInt(threads);
			} else {
				throw new IllegalArgumentException("Missing " + MAX_THREADS_TAG +
						" field in the configuration file.");
			}
			// MAX CHUNKS - always an optional field.
			String chunk =
					prop.getProperty(MAX_CHUNK_SIZE_TAG, DEFAULT_MAX_CHUNK_SIZE);
			options.maxChunkSize = Integer.parseInt(chunk);
			if (options.maxChunkSize <= 0) {
				throw new IllegalArgumentException("Max chunk size must be a " +
						"positive number. Given: " + options.maxChunkSize);
			}
			
			return options;
		} catch (Exception e) {
			System.err.println(
					"Error occured while parsing the options file: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Default CTOR. Creates a new instance of {@link ServerOptions}.<br>
	 * CTOR is private so the only way to create {@link ServerOptions} is via
	 * {@link ServerOptions#parseFromFile(File)} method.
	 * <br>
	 */
	private ServerOptions() {
		webServer = null;
	}

	/**
	 * @return The port the server should listen to.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return The root directory where all the resources reside.
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * @return The default page to respond with when not specified.
	 */
	public String getDefaultPage() {
		return defaultPage;
	}

	/**
	 * @return The maximal number of threads to use in the server for handling
	 * 				 clients.
	 */
	public int getMaxThreads() {
		return maxThreads;
	}
	
	/**
	 * @return The maximal chunk size to use when responding with a chunked
	 * 				 message.
	 */
	public int getMaxChunkSize() {
		return maxChunkSize;
	}

	/**
	 * @return The web server held by the options.
	 */
	public WebServerInterface getWebServer() {
		return webServer;
	}

	/**
	 * @param webServer The new instance held by the options.
	 */
	public void setWebServer(WebServerInterface webServer) {
		this.webServer = webServer;
	}
}
