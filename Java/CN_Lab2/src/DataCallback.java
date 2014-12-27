import java.util.Properties;

/**
 * A callback to be invoked by a {@link URLDownloader} when its data is ready to be processed.
 */
public interface DataCallback {
  /**
   * Notifying new data is ready.
   * @param resource The resource to which the data in the callback refers.
   * @param headers The response's headers (as key:value properties).
   * @param data The data received. null if no data arrived or requested.
   * @param rtt The amount of time from when the request was sent to the time the response arrived.
   * @param depth The depth of the current request (in the crawling mechanism).
   */
	public void dataAvailable(String resource, Properties headers, String data, long rtt, int depth);
}
