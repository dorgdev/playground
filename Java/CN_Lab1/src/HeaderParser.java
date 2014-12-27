import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class HeaderParser {

	/** The regular expression matching the first line of an HTTP message. */
	public static final String HTTP_REGEX = "^(.+) (.+) [H|h][T|t][T|t][P|p]/1.\\d$";
	/** A regex matching engine, identifying HTTP opening lines. */
	public static final Pattern HTTP_PATTERN = Pattern.compile(HTTP_REGEX);
	/** A pattern matching an image request. */
	public static final Pattern IMAGE_FILE_PATTERN = 
			Pattern.compile(".*\\.(bmp|gif|png|jpg)$");
	/** A pattern matching an HTML request. */
	public static final Pattern HTML_FILE_PATTERN = 
			Pattern.compile(".*(/|\\.(html?|php|aspx?))$");
	/** A pattern matching an icon request. */
	public static final Pattern ICON_FILE_PATTERN = 
			Pattern.compile(".*\\.icon?$");
	/** The HTTP header separator (between header's name and value). */
	public static final String HEADER_SEPARATOR = ": ";
	/** The CGI parameters separator used in the resource field. */
	public static final String CGI_SEPARATOR = "?";
	/** The HTTP parameters separator. */
	public static final String PARAMS_SEPARATOR = "&";
	/** The HTTP parameters key-value separator. */
	public static final char KEY_VALUE_SEPARATOR = '=';
	/** The HTTP content-length header name. */
	public static final String CONTENT_LENGTH_TAG = "content-length";
	/** The HTTP user-agent header name. */
	public static final String USER_AGENT_TAG = "user-agent";
	/** The HTTP referrer header name. */
	public static final String REFERRER_TAG = "referrer";
	/** The HTTP referer header name. */
	public static final String REFERER_TAG = "referer";
	/** The HTTP chunked header name. */
	public static final String CHUNKED_TAG = "chunked";
	/** The HTTP header value for "yes" (approval). */
	public static final String YES_HEADER_VALUE = "yes";
	/** The HTTP CRLF separator. */
	public static final String CRLF = "\r\n";

	
	/**
	 * A private CTOR to prevent instantiation (static utilities class).
	 */
	private HeaderParser() {
	}

	/**
	 * Parses an {@link InputStream} representing an HTTP header, and returns a
	 * new instance of {@link HttpRequest} instance.  In case of any parsing
	 * problem, throws an {@link HandlingException}.
	 * @param reader A {@link Reader} delivering the data to parse.
	 * @param request The {@link HttpRequest} to fill with parsed data.
	 * @throws HandlingException In case of a problem parsing the data.
	 */
	public static void parse(BufferedReader reader, HttpRequest request) 
			throws HandlingException {
		try {
			// Read the first line and validate it.
			String line = reader.readLine();
			if (line == null) {
				// Got a connection with no data.
				throw new HandlingException("Got a connection with no data.",
						HandlingException.ErrorCode.BAD_REQUEST);
			}

			// Try to parse the first line
			parseFirstLine(request, line);
			request.headers.append(line).append(CRLF);

			// Parse the rest of the header until CRLFCRLF (as an empty line).
			line = reader.readLine();
			while (line != null && line.length() != 0) {
				request.headers.append(line).append(CRLF);
				parseHeaderLine(request, line);
				line = reader.readLine();
			}
		} catch (IOException e) {
			// Failed to read from the socket. Assume the data stream was over, thus
			// a bad request.
			throw new HandlingException(
					"Failed to read data from stream: " + e.getMessage(), e, 
					HandlingException.ErrorCode.BAD_REQUEST);
		}
	}

	/**
	 * Parses the parameters in the given {@link String} and puts their values
	 * into the {@link HttpRequest} parameters field.<br>
	 * Expects a line in the format: <code>key1=value1&key2=value2&...</code>
	 * @param request The {@link HttpRequest} to fill its parameters.
	 * @param params A {@link String} in the format mentioned above.
	 */
	public static void parseParameters(HttpRequest request, String params) {
		// Make sure the params data contains enough information.
		if (params == null || params.length() < 3) {
			return;
		}
		// Split the data into key-value pairs, and iterate over the pairs.
		String[] keyValuePairs = params.split(PARAMS_SEPARATOR);
		for (String pair : keyValuePairs) {
			// Find the key and the value. Key name cannot be empty, value can.
			int separaotIndex = pair.indexOf(KEY_VALUE_SEPARATOR);
			if (separaotIndex > 0) {
				request.parameters.put(pair.substring(0, separaotIndex),
						pair.substring(separaotIndex + 1));
			}
		}
	}
	
	/**
	 * Parses the first line, which its parsing mechanism is different than the
	 * rest of the header lines (due to its format).
	 * @param request The {@link HttpRequest} to fill with the parsed data.
	 * @param line The first line read from the connection.
	 * @throws HandlingException In case of a problem parsing the line.
	 */
	private static void parseFirstLine(HttpRequest request, String line) 
			throws HandlingException {
		// Match the format of the first line (to make sure it's HTTP).
		Matcher matcher = HTTP_PATTERN.matcher(line);
		if (!matcher.matches()) {
			throw new HandlingException(
					"First line doesn't match HTTP format: " + line,
					HandlingException.ErrorCode.BAD_REQUEST);
		}
		// Identify the HTTP command specified in the request.
		String HttpCommand = matcher.group(1).toUpperCase();
		try {
			request.type = HttpRequest.Type.valueOf(HttpCommand);
		} catch (IllegalArgumentException e) {
			System.err.println("Got an unknown command type: " + HttpCommand);
			request.type = HttpRequest.Type.UNKNOWN;
		}
		request.resource = matcher.group(2);
		// Parse parameters.
		int startParams = request.resource.indexOf(CGI_SEPARATOR);
		if (startParams >= 0) {
			// Found parameters. Parse them.
			String params = request.resource.substring(startParams + 1);
			parseParameters(request, params);
			// Clean the resource from the parameters.
			request.resource = request.resource.substring(0, startParams);
		}
		// Set the predicted content type.
		if (HTML_FILE_PATTERN.matcher(request.resource).matches()) {
			request.contentType = HttpRequest.ContentType.HTML;
		} else if (IMAGE_FILE_PATTERN.matcher(request.resource).matches()) {
			request.contentType = HttpRequest.ContentType.IMAGE;
		} else if (ICON_FILE_PATTERN.matcher(request.resource).matches()) {
			request.contentType = HttpRequest.ContentType.ICON;
		}
	}

	/**
	 * Parses an HTTP header line. 
	 * @param request The {@link HttpRequest} to fill with the parsed data.
	 * @param line The HTTP header line read from the connection.
	 * @throws HandlingException In case of a problem parsing the line.
	 */
	private static void parseHeaderLine(HttpRequest request, String line) 
			throws HandlingException {
		// Tries to parse according to headers' format ("key: value").
		int separatorIndex = line.indexOf(HEADER_SEPARATOR);
		if (separatorIndex < 1) {
			throw new HandlingException("Unexpected header format: " + line,
					HandlingException.ErrorCode.BAD_REQUEST);
		}
		String key = line.substring(0, separatorIndex);
		String value = line.substring(separatorIndex + HEADER_SEPARATOR.length());
		// Parse content-length header.
		if (key.equalsIgnoreCase(CONTENT_LENGTH_TAG)) {
			try {
				request.contentLength = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new HandlingException(
						"Failed to parse content-length. Illegal number: " + value,
						HandlingException.ErrorCode.BAD_REQUEST);
			}
		} else if (key.equalsIgnoreCase(REFERRER_TAG) ||
				key.equalsIgnoreCase(REFERER_TAG)) {
			// Parse referer/referrer header.
			request.referer = value;
		} else if (key.equalsIgnoreCase(USER_AGENT_TAG)) {
			// Parse user-agent header.
			request.userAgent = value;
		} else if (key.equalsIgnoreCase(CHUNKED_TAG)) {
			// Parse the chunked header.
			request.chunked = value.equalsIgnoreCase(YES_HEADER_VALUE);
		}
	}
}
