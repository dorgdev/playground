import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class collects statistics over different information with regard to the
 * {@link WebServer} work. It's a singleton class, fully thread-safe, and it
 * can report back the accumulative information so far.
 */
public class StatsCollector {
	
	/** The single instance if this class in the system. */
	public static final StatsCollector instance = new StatsCollector();
	
	/**
	 * An internal POJO class for holding stats per HTTP method.
	 */
	private class MethodStats {
		/** Amount of request from this type. */
		public int numRequest;
		/** Total bytes received, for requests of this type. */
		public long totalBytesReceived;
		/** Total bytes sent, for requests of this type. */
		public long totalBytesSent;
		/** Number of times each resources was requested. */
		public Map<String, Integer> resourceToNum;
		
		/**
		 * Initializes the internal state. 
		 */
		public MethodStats() {
	    numRequest = 0;
	    totalBytesReceived = 0;
	    totalBytesSent = 0;
	    resourceToNum = new HashMap<String, Integer>();
    }
	}
	
	/**
	 * Wrapping an {@link InputStream} and updates the {@link HttpRequest} with
	 * the amount of bytes read.
	 */
	public static class RequestInputStream extends InputStream {
		
		/** The request to update, */
		private  HttpRequest request;
		/** The wrapped stream. */
		private InputStream stream;
		
		/**
		 * Creates a new instance.
		 * @param request The request to update.
		 * @param stream The wrapped stream.
		 */
		public RequestInputStream(HttpRequest request, InputStream stream) {
			this.request = request;
			this.stream = stream;
		}

    @Override
    public int available() throws IOException {
	    return stream.available();
    }

    @Override
    public void close() throws IOException {
	    stream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
    	stream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
	    return stream.markSupported();
    }

    @Override
    public int read() throws IOException {
    	request.bytes++;
	    return stream.read();
    }

    @Override
    public int read(byte[] buff, int offset, int len) throws IOException {
    	int length = stream.read(buff, offset, len);
    	request.bytes += length;
	    return length;
    }

    @Override
    public int read(byte[] buff) throws IOException {
    	int length = stream.read(buff);
    	request.bytes += length;
	    return length;
    }

    @Override
    public synchronized void reset() throws IOException {
    	stream.reset();
    }

		@Override
    public long skip(long n) throws IOException {
	    return stream.skip(n);
    }
	}
	
	/**
	 * Wrapping an {@link OutputStream} and updates the {@link HttpResponse} with
	 * the amount of bytes written.
	 */
	public static class ResponseOutputStream extends OutputStream {
		/** The response to update, */
		private  HttpResponse response;
		/** The wrapped stream. */
		private OutputStream stream;
		
		/**
		 * Creates a new instance.
		 * @param request The request to update.
		 * @param stream The wrapped stream.
		 */
		public ResponseOutputStream(HttpResponse response, OutputStream stream) {
			this.response = response;
			this.stream = stream;
		}

    @Override
    public void close() throws IOException {
	    stream.close();
    }

		@Override
    public void flush() throws IOException {
			stream.flush();
    }

    @Override
    public void write(byte[] buff, int offset, int len) throws IOException {
    	stream.write(buff, offset, len);
    	response.bytesSent(len);
    }

    @Override
    public void write(byte[] buff) throws IOException {
    	stream.write(buff);
    	response.bytesSent(buff.length);
    }

    @Override
    public void write(int b) throws IOException {
    	stream.write(b);
    	response.bytesSent(1);
    }
	}
	
	/** A mapping from an HTTP method to a {@link MethodStats} object. */
	private Map<HttpRequest.Type, MethodStats> methodToStats;
	/** the number of times each error code had occurred. */
	private Map<HandlingException.ErrorCode, Integer> errorToNum;
	/** The current time when the system started. */
	private long startTime;
	
	/**
	 * @return The single instance of this class (for the singleton pattern).
	 */
	public static StatsCollector getInstance() {
		return instance;
	}
	
	/**
	 * Default CTOR, private for the singleton pattern.
	 * Initializes all the internal mapping with new empty values.
	 */
	private StatsCollector() {
		startTime = System.currentTimeMillis();
		// Initialize the errors mapping.
		errorToNum = new HashMap<HandlingException.ErrorCode, Integer>();
		HandlingException.ErrorCode codes[] = HandlingException.ErrorCode.values();
		for (HandlingException.ErrorCode code : codes) {
			errorToNum.put(code, 0);
		}
		// Initialize the HTTP methods mapping.
		methodToStats = new HashMap<HttpRequest.Type, MethodStats>();
		for (HttpRequest.Type type : HttpRequest.Type.values()) {
			methodToStats.put(type, new MethodStats());
		}
	}

	/**
	 * Report a failed request (for some error code).
	 * @param code The cause for the error.
	 */
	public synchronized void reportError(HandlingException.ErrorCode code) {
		errorToNum.put(code, errorToNum.get(code).intValue() + 1);
	}
	
	/**
	 * Reports a successful HTTP request-response session.
	 * @param req The {@link HttpRequest} received from the client.
	 * @param res The {@link HttpResponse} sent back to the client.
	 */
	public synchronized void reportSuccess(HttpRequest req, HttpResponse res) {
		// Request count.
		MethodStats stats = methodToStats.get(req.type);
		stats.numRequest++;
		// Bytes count.
		stats.totalBytesReceived += req.bytes;
		stats.totalBytesSent += res.getBytesSize();
		// Resource count.
		int resourceCount = stats.resourceToNum.containsKey(req.resource) ?
				stats.resourceToNum.get(req.resource) : 1;
		stats.resourceToNum.put(req.resource, resourceCount);
	}
	
	/**
	 * Generates a string with all the statistics.
	 */
	@Override
	public synchronized String toString() {
		StringBuilder builder = new StringBuilder();
		// Uptime.
		long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000; 
		builder.append("Uptime: ").append(uptimeSeconds).append(" seconds\n\n");
		// Go over the valid requests.
		int totalSuccessful = 0;
		long totalBytesSent = 0;
		long totalBytesReceived = 0;
		for (HttpRequest.Type type : methodToStats.keySet()) {
			MethodStats stats = methodToStats.get(type);
			totalSuccessful += stats.numRequest;
			totalBytesReceived += stats.totalBytesReceived;
			totalBytesSent += stats.totalBytesSent;
			builder.append("Stats for HTTP method - ").append(type.name())
			    .append("\n  * Number of requests: ").append(stats.numRequest)
			    .append("\n  * Bytes Received: ").append(stats.totalBytesReceived)
			    .append("\n  * Bytes Sent: ").append(stats.totalBytesSent)
			    .append("\n  * Requested resources:\n");
			for (String resource : stats.resourceToNum.keySet()) {
				builder.append("    * ").append(resource).append(": ")
						.append(stats.resourceToNum.get(resource).intValue()).append("\n");
			}
			builder.append("\n");
		}
		builder.append("Total Successful -")
				.append("\n  * Total requests: ").append(totalSuccessful)
				.append("\n  * Total bytes received: ").append(totalBytesReceived)
				.append("\n  * Total bytes sent: ").append(totalBytesSent)
				.append("\n\n");
		// Go over the invalid requests.
		builder.append("Total Unsuccessful Requests -\n");
		int totalUnsuccessful = 0;
		for (HandlingException.ErrorCode code : errorToNum.keySet()) {
			totalUnsuccessful += errorToNum.get(code).intValue();
			builder.append("  * HTTP ").append(code.getCode()).append(": ")
					.append(errorToNum.get(code).intValue()).append("\n");
		}
		builder.append("  * Total: ").append(totalUnsuccessful).append("\n");
		// Return the overall statistics.
	  return builder.toString();
	}
}