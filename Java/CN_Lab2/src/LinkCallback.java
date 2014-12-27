/**
 * A callback which is responsible to notify there's a new link in the system.
 */
public interface LinkCallback {

  /**
   * A callbacl to request a new link (resource).
   * @param resource The requested resource.
   * @param referer The referer from which the request was issued.
   * @param getContent Whether to read the content of the resource (or just the header).
   * @param depth How deep this resource request is, in the crawling recursion.
   */
  public void newLink(String resource, String referer, boolean getContent, int depth);
  
}
