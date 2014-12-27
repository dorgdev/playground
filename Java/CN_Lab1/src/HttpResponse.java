/**
 * Represents an HTTP response object. Build the headers and the holds the
 * data of the response.
 */
public class HttpResponse {

	/** The HTTP CRLF separator. */
	public static final String CRLF = "\r\n";
	/** The HTTP header response of transfer-encoding. */
	public static final String TRANSFER_ENCODING_TAG = "transfer-encoding";
	/** The transfer-encoding type of chunked response. */
	public static final String CHUNKS_ENCODING = "chunks";

	/** The headers part of the response. */
	protected StringBuilder headers;
	/** the size (in bytes) of the response (as sent on the wire). */
	protected long bytes;
	/** The data buffer. */
	protected byte[] buffer;

	/**
	 * Creating a new {@link HttpResponse} with a default OK reply.
	 */
	public HttpResponse() {
		headers = new StringBuilder();
		headers.append("HTTP/1.1 200 OK").append(CRLF);
		buffer = new byte[0];
		bytes = 0;
	}

	/**
	 * Adding a new header to the response.
	 * @param key The header's name.
	 * @param value The header's value.
	 */
	public void addHeader(String key, String value) {
		headers.append(key).append(HeaderParser.HEADER_SEPARATOR);
		headers.append(value).append(CRLF);
	}

	/**
	 * Setting the data buffer of the response. Also sets the content-length or
	 * transfer-encoding headers accordingly (depends on whether the response
	 * should be chunked or not).
	 * @param buffer The data to set.
	 * @param request The client's original request.
	 */
	public void setBuffer(byte[] buffer, HttpRequest request) {
		this.buffer = buffer;
		if (request.chunked) {
			addHeader(TRANSFER_ENCODING_TAG, CHUNKS_ENCODING);
		} else {
			addHeader(HeaderParser.CONTENT_LENGTH_TAG, 
					String.valueOf(buffer.length));
		}
	}

	/**
	 * Returns the headers value with an additional CRLF suffix, as required in
	 * a full valid response.
	 */
	@Override
	public String toString() {
		return headers.toString() + CRLF;
	}

	/**
	 * Returns the buffer held in the response.
	 * @return The response's data.
	 */
	public byte[] getBuffer() {
		return buffer;
	}
	
	/**
	 * @return The size of the request, in bytes, as sent on the wire.
	 */
	public long getBytesSize() {
		return bytes;
	}
	
	/**
	 * Reports new data sent on the wire.
	 * @param size The size of the sent data.
	 */
	public void bytesSent(long size) {
		bytes += size;
	}
}
