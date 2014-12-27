import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzes the data downloaded using the downloader.
 */
public class DataAnalyzer {

  /** The content-type HTTP header. */
  public static final String CONTENT_TYPE_HEADER = "content-type";
  /** A pattern recognizing images. */
  public static final Pattern IMAGE_PATTERN = 
      Pattern.compile("image/.*");
  /** A pattern recognizing video. */
  public static final Pattern VIDEO_PATTERN = 
      Pattern.compile("video/.*");
  /** A pattern recognizing documents. */
  public static final Pattern DOCUMENT_PATTERN = 
      Pattern.compile("document/.*");
  /** A pattern recognizing HTML pages. */
  public static final Pattern HTML_PATTERN = 
      Pattern.compile("text/htm.*");
  /** The HTML's link pattern extractor (extract href and src links of anchors and images). */
  public static final Pattern LINK_EXTRACTOR =
      Pattern.compile("<(?:a|img|link|iframe|frame)(?:[^>]*)(?:src|href)=\"([^\"]*)\"");
  /** A pattern recognizing domains (from within an extracted link). */
  public static final Pattern DOMAIN_PATTERN = 
      Pattern.compile("(?:https?:)?//([\\w|\\d|\\.]+)(/.*)?");
  /** A pattern recognizing content which doesn't require dowloading its content. */
  public static final Pattern NO_CONTENT_PATTERN = Pattern.compile( 
      "/.*\\.(?:bmp|jpg|png|gif|ico|avi|mpg|mp4|wmv|mov|flv|swf|pdf|doc|docx|xls|xlsx|ppt|pptx)" +
      "(?:\\?.*)?");

  /** The analyzed resource within the domain. */
  private String resource;
  /** The headers retrieved in the HTTP response. */
  private Properties headers;
  /** The data (if applicable) downloaded from the domain's server. */
  private String data;
  /** The RTT of the download. */
  private long rtt;
  /** The depth of the current resource in the crawling recursion. */
  private int depth;
  /** The crawling information associated with this domain. */
  private CrawlInfo crawlInfo;
  /** The link callback to use for each new link. */
  private LinkCallback linkCB;

  /**
   * Construct a new {@link DataAnalyzer}.
   * @param resource The specific resource the analyzer is about to analyze.
   * @param headers The headers recieved in the HTTP response.
   * @param data The data read in the response.
   * @param rtt The RTT of the download.
   * @param depth The current depth in the crawling recursion.
   * @param crawlInfo The information associated with the crawled domain.
   * @param linkCB The {@link LinkCallback} to use for each new link.
   */
  public DataAnalyzer(String resource, Properties headers, String data, long rtt, 
      int depth, CrawlInfo crawlInfo, LinkCallback linkCB) {
    this.resource = resource;
    this.headers = headers;
    this.data = data;
    this.rtt = rtt;
    this.depth = depth;
    this.crawlInfo = crawlInfo;
    this.linkCB = linkCB;
  }

  /**
   * Analyzes the given data.
   */
  public void analyze() {
    // Get the resource's size.
    long size = 0;
    if (headers.contains(URLDownloader.CONTENT_LENGTH_HEADER)) {
      size = Long.parseLong(headers.getProperty(URLDownloader.CONTENT_LENGTH_HEADER));
    } else if (data != null) {
      size = data.length();
    } else {
      System.out.println("Cannot estimate correct resource size. Assumed chunked.");
    }
    // Accumulate by type.
    String type = headers.getProperty(CONTENT_TYPE_HEADER, "");
    if (IMAGE_PATTERN.matcher(type).matches()) {
      crawlInfo.newImage(size, rtt);
    } else if (VIDEO_PATTERN.matcher(type).matches()) {
      crawlInfo.newVideo(size, rtt);
    } else if (DOCUMENT_PATTERN.matcher(type).matches()) {
      crawlInfo.newDocument(size, rtt);
    } else {
      crawlInfo.newPage(size, rtt);
      if (data != null && HTML_PATTERN.matcher(type).matches()) {
        parseHtml();
      }
    }
  }

  /**
   * Parses the HTML downloaded from the domain's server, and issue new links query for each
   * found link in the HTML.
   */
  private void parseHtml() {
    int numLinks = 0;
    String lowerData = data.toLowerCase();
    Matcher linkMatcher = LINK_EXTRACTOR.matcher(lowerData);
    while (linkMatcher.find()) {
      // Found another matched link, issue a request for it.
      String link = linkMatcher.group(1);
      numLinks++;
      crawlInfo.addLinks(1);
      Matcher domainMatcher = DOMAIN_PATTERN.matcher(link);
      if (domainMatcher.matches()) {
        // This a connected domain. Handle it accordingly.
        if (domainMatcher.groupCount() >= 1) {
          crawlInfo.addConnectedDomain(domainMatcher.group(1));
        } else {
          // Can't extract the connected domain.
          continue;
        }
      } else {
        if (!link.startsWith("/")) {
          // This is a canonical link. Create the full resource.
          if (resource.endsWith("/")) {
            link = resource + link;
          } else {
            int lastSlash = resource.lastIndexOf('/');
            if (lastSlash > 0) {
              link = resource.substring(0, lastSlash + 1) + link;
            } // Else - unknown format. Keep it as is.
          }
        }
        // Ask to crawl the resource (if it wasn't crawled before).
        if (linkCB != null) {
          boolean getContent = !NO_CONTENT_PATTERN.matcher(link).matches(); 
          linkCB.newLink(link, resource, getContent, depth - 1);
        }
      }
    }
    System.out.println("Extracted " + numLinks + " links from the content of: " + resource);
  }
}
