import java.io.File;

/**
 * The entry point of the Web Server application (Lab 1 - Computer Networks).
 */
public class WebServerMain {

	/** The default location of the configuration file. */
	public static final String DEFAULT_CONFIG_FILE = "config.ini";
	
	/**
	 * @param args An empty array of arguments, or the location of the 
	 * 				configuration file.
	 */
	public static void main(String[] args) {
		if (args.length > 1) {
			System.err.println("Usage: java WebServerMain [config file]");
			System.exit(1);
		}
		String config = DEFAULT_CONFIG_FILE;
		if (args.length > 0 ) {
			// Try to read the configuration from a specified location.
			config = args[0];
		}
		ServerOptions options = ServerOptions.parseFromFile(new File(config));
		if (options == null) {
			// Invalid options given.
			System.exit(2);
		}
		// Create the main server.
		WebServerInterface server = new WebServer(options);
		options.setWebServer(server);
		
		// Create the supported HTTP methods handlers.
		RequestHandlerProxy proxy = RequestHandlerProxy.getInstance();
		proxy.setHandler(HttpRequest.Type.GET, new GetRequestHandler(options));
		proxy.setHandler(HttpRequest.Type.POST, new PostRequestHandler(options));
		proxy.setHandler(HttpRequest.Type.TRACE, new TraceRequestHandler(options));
		proxy.setHandler(HttpRequest.Type.HEAD, new HeadRequestHandler(options));
		proxy.setHandler(HttpRequest.Type.CTRL, new ControlRequestHandler(options));

		// Make sure the class loader loads the StatsCollector so it would start 
		// its timer.
		StatsCollector.getInstance();
		
		// Start the web server.
		server.run();
		System.out.println("Server running ended successfully.");
		
		// Make sure all threads quit (in case of a left unjoined internal thread).
		System.exit(0);
	}

}
