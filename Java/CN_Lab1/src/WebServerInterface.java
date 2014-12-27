/**
 * Represents an interface for a web server. 
 */
public interface WebServerInterface {

	/**
	 * Asks the {@link WebServer} to shut down.
	 */
	public void shutDown();
	
	/**
	 * Runs the web server's main loop. When this method returns, it is assumes
	 * that the web server had shut down its services.
	 */
	public void run();
	
}
