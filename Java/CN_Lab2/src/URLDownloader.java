import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Properties;

/**
 * This class is responsible for downloading the content of a given URL.
 * It knows how to handle HEAD and GET requests differently, and how to call back when it's done.
 */
public class URLDownloader {

  /** HTTP line termination string. */
	public static final String CRLF = "\r\n";
	/** HTTP GET method header. */
  public static final String METHOD_GET = "GET ";
  /** HTTP HEAD method header. */
  public static final String METHOD_HEAD = "HEAD ";
  /** The HTTP version we use in out requests. */
  public static final String HTTP_VERSION = " HTTP/1.1";
  /** The user-agent field, used in our crawling. */
  public static final String USER_AGENT_HEADER = "user-agent: ";
  /** The host (domain) header for which the request is sent to. */
  public static final String HOST_HEADER = "Host: ";
  /** The refere (referrer) header from which the download was requested. */
  public static final String REFERER_HEADER = "Referer: ";
  /** The content-length header. */
  public static final String CONTENT_LENGTH_HEADER = "content-length";
  /** The transfer-chunked header. */
  public static final String TRANSFER_ENCODING_HEADER = "transfer-encoding";
  /** The chunked transfer encoding. */
  public static final String CHUNKED_ENCODING = "chunked";

	/** The domain in which the downlaoded resource lives. */
	private String domain;
	/** The domain's port for the downloaded resource. */
	private int port;
	/** The current depth of the crawling recursion. */
	private int depth;
	/** The resource to download within the domain. */
	private String resource;
	/** The referer of the resource request. */
	private String referer;
	/** The user agent used for downloading the resource. */
	private String userAgent;
	/** Whether or not the content of the resource should be retrieved as well. */
	private boolean getContent;
	/** The data downloaded from the URL. Only valid after the downloader finished its run. */
	private String data;
  /** The data callback to use for each downloaded URL. */
  private DataCallback dataCB;

	/**
	 * Creating a new {@link URLDownloader}.
	 * @param domain The domain in which the requested resource lives.
	 * @param port The port to connect to for the downloaded resource.
	 * @param depth The current crawling depth.
	 * @param resource The resource to retrieve. Null for no referer.
	 * @param referer The resource's referer page.
	 * @param userAgent The userAgent to use for the download.
	 * @param getContent Whether to bring the content as well.
	 * @param dataCB The data callback to use for each downloaded URL.
	 */
	public URLDownloader(String domain, int port, int depth, String resource, String referer, 
	    String userAgent, boolean getContent, DataCallback dataCB) {
		this.domain = domain;
		this.port = port;
		this.depth = depth;
		this.resource = resource;
		this.referer = referer;
		this.userAgent = userAgent;
		this.getContent = getContent;
		this.data = null;
		this.dataCB = dataCB;
	}

	/**
	 * @return The data downloaded from the URL.
	 */
	public String getData() {
	  return data;
	}

	/**
	 * Downloads the requested resource.
	 */
	public void download() {
	  System.out.println("Downloader starts downloading URL: " + resource);
	  Socket socket = null;
	  Properties headers = new Properties();
	  try {
  		StringBuilder request = new StringBuilder(getContent ? METHOD_GET : METHOD_HEAD);
  		request
  		    .append(resource).append(HTTP_VERSION).append(CRLF)
  		    .append(USER_AGENT_HEADER).append(userAgent).append(CRLF)
  		    .append(HOST_HEADER).append(domain).append(CRLF);
  		if (referer != null) {
  		  String actualReferer = 
  		      String.format("http://%s%s%s", domain, port != 80 ? (":" + port) : "", referer);
  		  request.append(REFERER_HEADER).append(actualReferer).append(CRLF);
  		}
  		request.append(CRLF);

  		// Connect and create the socket's IO objects.
  		socket = new Socket(domain, port);
  		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
  		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  		
  		// Write the request.
  		long startTime = System.currentTimeMillis();
  		writer.write(request.toString());
  		writer.flush();
  		
  		// Read the reply's headers.
  		String line = reader.readLine();
  		while (line != null && line.length() > 0) {
  	    // Tries to parse according to headers' format ("key: value").
  	    int separatorIndex = line.indexOf(HeaderParser.HEADER_SEPARATOR);
  	    if (separatorIndex < 1) {
  	      line = reader.readLine();
  	      continue;
  	    }
  	    String key = line.substring(0, separatorIndex);
  	    String value = line.substring(separatorIndex + HeaderParser.HEADER_SEPARATOR.length());
  	    headers.put(key.toLowerCase(), value);
  			line = reader.readLine();
  		}
  		// Done reading the response's header, calculate the RTT.
      long rtt = System.currentTimeMillis() - startTime;
  		
  		// Read the content (only if necessary).
  		if (getContent) {
  	    if (headers.containsKey(CONTENT_LENGTH_HEADER)) {
  	      // Read as the content length indicated.
  	      int length = Integer.parseInt(headers.getProperty(CONTENT_LENGTH_HEADER));
  	      data = new String(readKnownSize(reader, length));
  	    } else if (headers.getProperty(TRANSFER_ENCODING_HEADER, "").contains(CHUNKED_ENCODING)) {
  	      // Read until the input is over (in chunks).
  	      data = readChunked(reader);
  	    } else {
  	      System.err.println("Unknown data transfer - no data length found.");
  	    }
  		}

  		// Call the callback when done.
  		if (dataCB != null) {
  		  dataCB.dataAvailable(resource, headers, data, rtt, depth);
  		}
  		System.out.println("Downloader done downloading the URL: " + resource);
  	} catch (IOException e) {
  	  System.err.println("Error downloading request: " + e.getMessage());
  	} finally {
  	  if (socket != null) {
  	    try {
  	      socket.close();
  	    } catch (IOException e2) {
  	      // Ignore second chance exception. Nothing much we can do...
  	    }
  	  }
  	}
	}
	
	/**
	 * Reads a content in the length of the given value.
	 * @param reader The reader to read with.
	 * @param length The length of the expected content.
	 * @return The data read.
	 * @throws IOException In case of a probelm while reading.
	 */
	public char[] readKnownSize(BufferedReader reader, int length) throws IOException {
    char[] content = new char[length];
    for (int i = 0; i < length; ++i) {
      content[i] = (char)reader.read();
    }
    return content;
	}
	
	 /**
   * Reads content in an unknown length - received in chunks.
   * @param reader The reader to read with.
   * @return The data read.
   * @throws IOException In case of a probelm while reading.
   */
  public String readChunked(BufferedReader reader) throws IOException {
    StringBuilder builder = new StringBuilder();
    String line = reader.readLine();
    while (line != null && !line.equals("")) {
      // Chunk's length comes in hex.
      int nextLen = Integer.parseInt(line, 16);
      // Last chunk's size should be 0.
      if (nextLen == 0) {
        break;
      }
      // Read the expected size.
      builder.append(readKnownSize(reader, nextLen));
      // Expect a CRLF
      line = reader.readLine();
      // Read the actual next line.
      if (line != null) {
        line = reader.readLine();
      }
    }
    return builder.toString();
  }
  
}
