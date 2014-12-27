/**
 * An interface for classes which can generates HTML pages on request.
 */
public interface PageGenerator {

  /**
   * Generates some content for a page, as outlined in the HTTP request.
   * @param request The {@link HttpRequest} associated with the page request.
   * @return The content of the result page.
   * @throws HandlingException In case of a problem generating the content.
   */
  public byte[] generatePage(HttpRequest request) throws HandlingException;
}
