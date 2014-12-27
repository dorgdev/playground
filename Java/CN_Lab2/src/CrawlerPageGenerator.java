import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Represents the {@link PageGenerator} of the crawling system.
 */
public class CrawlerPageGenerator implements PageGenerator, Crawler.CrawlCallback {

  /** The root page of the program. Leads to the INDEX_HTML_PAGE page below. */
  public static final String ROOT_HTML_PAGE = "/";
  /** The name of the directory holding the crawling information's data. */
  public static final String INFO_DIR_NAME = ROOT_HTML_PAGE + "info/";
  /** The main page's resource. */
  public static final String INDEX_HTML_PAGE = ROOT_HTML_PAGE + "index.html";
  /** The crawl request page. */
  public static final String CRAWL_HTML_PAGE = ROOT_HTML_PAGE + "crawl.html";
  /** The page showing the status of the crawler (used as an iframe). */
  public static final String HISTORY_N_MSG_HTML_PAGE = ROOT_HTML_PAGE + "history_n_msg.html";
  /** The HTML param name for the domain to crawl. */
  public static final String DOMAIN_PARAM = "domain";
  /** The HTML param name for the domain's port to crawl. */
  public static final String PORT_PARAM = "port";
  /** The default port to use for crawling. */
  public static final String DEFAULT_PORT = "80";
  /** The HTML param name for the resource to start the crawl from in the domain. */
  public static final String RESOURCE_PARAM = "resource";
  /** The default resource to start the crawl from. */
  public static final String DEFAULT_RESOURCE = "/";
  /** The HTML param name for the depth of the crawling recursion. */
  public static final String DEPTH_PARAM = "depth";
  /** The HTML param name for whether to respect robots or not. */
  public static final String ROBOTS_PARAM = "robots";
  /** The HTML param name for the message to show in the readiness message or not. */
  public static final String MSG_PARAM = "msg";
  /** The text to show when the given depth is illegal */
  public static final String ILLEGAL_DEPTH  = "Illegal crawling depth given: ";
  /** The text to show when the given resource is illegal. */
  public static final String ILLEGAL_RESOURCE = "Illegal resource given: ";
  /** The text to show if the cralwer is running. */
  public static final String CRAWLER_RUNNING = "Crawler is running...";
  /** The text to show if the cralwer finished running. */
  public static final String CRAWLER_DONE = "Crawler finished running :)";
  /** The text to show if the cralwer started running. */
  public static final String CRAWLER_STARTED = "Crawler started running :)";
  /** The text to show if the cralwer failed to start. */
  public static final String CRAWLER_FAILED = "Crawling failed to start: ";
  /** The default URL encoding to use for HTTP parameters. */
  public static final String DEFAULT_URL_ENCODING = "UTF-8";
  /** The date formatter used for generating links from crawling time. */
  public static final SimpleDateFormat LINK_FORMATTER = 
      new SimpleDateFormat("yyyyMMdd_hhmmss");
  /** The date formatter used for generating crawling time information. */
  public static final SimpleDateFormat INFO_FORMATTER = 
      new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
  /** Filters irrlevant files from the internal crawling information directory. */
  public static final FileFilter VALID_INFO_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.getName().matches(".+_\\d{8}_\\d{6}.html");
    }
  };
  
  /** The options which will be used to initialize the server's work. */
  private ServerOptions options;
  /** Maps from a crawled domain to its info presenting link. */
  private ConcurrentMap<String, String> domainToLink;
  /** Maps from a crawled domain's link to its information. */
  private ConcurrentMap<String, CrawlInfo> linkToCrawlInfo;
  /** The program crawler instance.  */
  private Crawler crawler;
  /** Compare links by their pointed info crawling time. */
  private Comparator<String> linksComparator;
  /** Marks that the crawling is running. */
  private boolean crawlerRunning;

  /**
   * @param options The options according which the pages are generated.
   */
  public CrawlerPageGenerator(ServerOptions options) {
    this.options = options;
    this.domainToLink = new ConcurrentHashMap<String, String>();
    this.linkToCrawlInfo = new ConcurrentHashMap<String, CrawlInfo>();
    this.crawler = new Crawler(options, this);
    // Set the links comparator.
    linksComparator = new Comparator<String>() {
      @Override
      public int compare(String link1, String link2) {
        if (!linkToCrawlInfo.containsKey(link1) || !linkToCrawlInfo.containsKey(link2)) {
          return 0;
        }
        return (int)(linkToCrawlInfo.get(link1).getCreationTime() -
            linkToCrawlInfo.get(link2).getCreationTime());
      }
    };
    // Load persistent internal data.
    loadInternalData();
  }

  /**
   * Goes over the internal private directory which hold past crawling information, and rebuilds
   * the data held in memory.  This is useful in case the server crashes and reloads.
   */
  private void loadInternalData() {
    try {
      File internalDir = new File(options.getRoot() + INFO_DIR_NAME);
      if (!internalDir.exists()) {
        // Persistent data dir is missing, creating it.
        internalDir.mkdir();
        return;
      }
      for (File resource : internalDir.listFiles(VALID_INFO_FILE_FILTER)) {
        CrawlInfo info = null;
        ObjectInputStream ois = null;
        try {
          ois = new ObjectInputStream(new FileInputStream(resource));
          info = (CrawlInfo)(ois.readObject());
          String link = INFO_DIR_NAME + resource.getName();
          domainToLink.put(info.getDomain(), link);
          linkToCrawlInfo.put(link, info);
          
          System.out.println("Loaded: " + resource);
        } catch (Exception e) {
          // An error occured. It could be stale or unreadable data, either ways, ignore this 
          // resource and print the error for tracing.
          System.err.println("Failed loading a resource (" + resource.getName() 
              + "): " + e.getMessage());
        } finally {
          try {
            if (ois != null) {
              ois.close();
            }
          } catch (Exception e) {
            // Ignore second chance exception.
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Failed loading persistent data, starting from scratch: " + 
          e.getMessage());
    }
  }
  
  @Override
  public byte[] generatePage(HttpRequest request) throws HandlingException {
    // First, make sure the request is valid.
    File file = validatePage(request);
    // Now generate the page.
    if (file != null) {
      // It's a real file. Read it from disk.
      return readFully(file);
    }
    // Generate the resource.
    if (request.resource.equals(INDEX_HTML_PAGE) || 
        request.resource.equals(ROOT_HTML_PAGE)) {
      return generateMainPage("");
    }
    if (request.resource.equals(CRAWL_HTML_PAGE)) {
      return generateCrawlPage(request);
    }
    if (request.resource.equals(HISTORY_N_MSG_HTML_PAGE)) {
      return generateInternalPage(request);
    }
    if (!linkToCrawlInfo.containsKey(request.resource)) {
      // This shouldn't happen. Each crawled link shoul have its crawling info.
      throw new HandlingException("Couldn't find the crawling info page: " + request.resource,
          HandlingException.ErrorCode.INTERNAL_SERVER_ERROR);
    }
    return generateCrawlInfoPage(linkToCrawlInfo.get(request.resource));
  }

  /**
   * Validates the requested page and returns a handle for it.
   * @param request The {@link HttpRequest} associated with the page request.
   * @return The {@link File} handle of the requested page, or null in case of an in-memory page.
   * @throws HandlingException In case of an invalid request/resource.
   */
  private File validatePage(HttpRequest request) throws HandlingException {
    String resource = request.resource;
    // Make sure the requested resource is in the resource library (disallow
    // the usage of ".." path descriptors.
    if (resource.contains("/../") ||
        resource.startsWith("../") ||
        resource.endsWith("/..") ||
        resource.equals("..")) {
      throw new HandlingException("Permission denied: " + resource,
          HandlingException.ErrorCode.FORBIDDEN);
    }
    // Check if it's a known page (which is genreated on the fly).
    if (resource.equals(INDEX_HTML_PAGE) || resource.equals(ROOT_HTML_PAGE)) {
      return null;
    }
    if (resource.startsWith(INFO_DIR_NAME) || 
        resource.equals(CRAWL_HTML_PAGE) ||
        resource.equals(HISTORY_N_MSG_HTML_PAGE)) {
      if (request.host.length() == 0) {
        throw new HandlingException("Missing host header in request.", 
            HandlingException.ErrorCode.BAD_REQUEST);
      }
      
      // It's an internal link. Make sure the referrer is internal.
      String expectedPrefix = "http://" + request.host.toLowerCase();
      if (request.referer.toLowerCase().startsWith(expectedPrefix)) {
        return null;
      }
      System.err.println("Invalid referrer: " + request.referer);
      throw new HandlingException("Permission denied: " + resource,
          HandlingException.ErrorCode.FORBIDDEN);
    }
    
    // It is not. Try reading it from the server's root.
    String fileName = options.getRoot() + resource;
    File file = new File(fileName);
    // If it's a directory, add the default page to the resource.
    if (file.exists() && file.isDirectory()) {
      fileName += File.separator + options.getDefaultPage();
      file = new File(fileName);
    }
    // Make sure the resource exists.
    if (!file.exists() || !file.isFile()) {
      throw new HandlingException(
          "Could not find the requested resource: " + resource,
          HandlingException.ErrorCode.NOT_FOUND);
    }
    // Make sure we can read the resource.
    if (!file.canRead()) {
      throw new HandlingException("Permission denied: " + resource,
          HandlingException.ErrorCode.NOT_FOUND);
    }
    return file;
  }

  /**
   * Reads the content of the given file into a buffer and return the buffer.
   * @param file The {@link File} to read.
   * @return The content of the file.
   * @throws HandlingException In case of a problem reading from the file.
   */
  protected byte[] readFully(File file) throws HandlingException {
    try {
      int dataLen = (int)file.length();
      byte[] data = new byte[dataLen];

      FileInputStream fis = new FileInputStream(file);
      int readLen = 0;
      while (readLen < dataLen && fis.available() != 0) {
        fis.read(data, readLen, dataLen - readLen);
      }
      fis.close();

      return data;
    } catch (IOException e) {
      throw new HandlingException(
          "Failed reading from file: " + e.getMessage(), e,
          HandlingException.ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Generates the content of the main page, showing the crawling submission form and
   * former crawled domains.
   * @param message An optional message to display to the user.
   * @return The content of the main page.
   */
  private byte[] generateMainPage(String message) {
    StringBuilder data = new StringBuilder();
    data.append("<html>\n")
      .append(" <head>\n")
      .append("   <title>Efi's & Dor's Gangnam Web Crawler</title>\n")
      .append(" </head>\n")
      .append(" <body bgcolor=\"#EEEEFF\">\n")
      .append("  <table width=\"100%\"><tr><td width=\"50%\">\n")
      .append("   <h1><font color=\"blue\">Efi's & Dor's Gangnam Web Crawler</font></h1>\n")
      .append("   <form name=\"input\" action=\"crawl.html\" method=\"post\"><table>\n")
      .append("    <font size=\"4\" color=\"red\">\n")
      .append("     <tr>\n")
      .append("      <td>\n")
      .append("       <div title=\"The domain to crawl.\">Domain: <div>\n")
      .append("      </td>\n")
      .append("      <td><input type=\"text\" name=\"").append(DOMAIN_PARAM)
      .append("\" cols=\"100\"/></td>\n")
      .append("     </tr><tr>\n")
      .append("      <td>\n")
      .append("       <div title=\"Domain's HTTP port.\">Port: </div>\n")
      .append("      </td>\n")
      .append("      <td><input type=\"number\" min=\"0\" max=\"65535\" name=\"").append(PORT_PARAM)
      .append("\" value=\"").append(DEFAULT_PORT).append("\"/></td>\n")
      .append("     </tr><tr>\n")
      .append("      <td>\n")
      .append("       <div title=\"The resource to start the crawl from.\">Resource: </div>\n")
      .append("      </td>\n")
      .append("      <td><input type=\"text\" name=\"").append(RESOURCE_PARAM)
      .append("\" cols=\"100\" value=\"").append(DEFAULT_RESOURCE).append("\"/></td>\n")
      .append("     </tr><tr>\n")
      .append("      <td>\n")
      .append("       <div title=\"Zero (0) for infinite recursion.\">Crawl's Depth: </div>\n")
      .append("      </td>\n")
      .append("      <td><input type=\"number\" min=\"0\" name=\"").append(DEPTH_PARAM)
      .append("\" value=\"3\"/></td>\n")
      .append("     </tr><tr>\n")
      .append("      <td>\n")
      .append("       <div title=\"Ignore robots.\">Ignore robots.txt: </div>\n")
      .append("      </td>\n")
      .append("      <td><input type=\"checkbox\" name=\"").append(ROBOTS_PARAM)
      .append("\" checked=\"checked\"/></td>\n")
      .append("     </tr><tr>\n")
      .append("      <td><input type=\"submit\" value=\"Start Crawling\" align=\"center\"></td>\n")
      .append("     </tr>\n")
      .append("    </font>\n")
      .append("   </table></form>\n")
      .append("  </td><td width=\"50%\">\n")
      .append("   <iframe width=\"448\" height=\"252\" src=\"")
      .append("http://www.youtube.com/embed/9bZkp7q19f0\" frameborder=\"1\" allowfullscreen>\n")
      .append("   </iframe>\n")
      .append("  </td></tr></table>\n")
      .append("  <br>\n");
    String iframeSrc = HISTORY_N_MSG_HTML_PAGE;
    if (!message.isEmpty()) {
      try {
        iframeSrc += "?" + MSG_PARAM + "=" + URLEncoder.encode(message, DEFAULT_URL_ENCODING);
      } catch (UnsupportedEncodingException e) {
        // Nothing much we can do. Ignore the message.
        iframeSrc = HISTORY_N_MSG_HTML_PAGE;
      }
    }
    data.append("  <iframe src=\"").append(iframeSrc)
        .append("\" height=\"75%\" width=\"100%\"></iframe>\n")
        .append(" </body>\n")
        .append("</html>");
    return data.toString().getBytes();
  }

  /**
   * Generates a page for a crawl request.
   * @param request The HTTP request with the crawling parameters.
   * @return A response page for the crawling request.
   */
  private byte[] generateCrawlPage(HttpRequest request) {
    // Get relevant parameters for crawler and start a new crawling.
    String domain = request.parameters.get(DOMAIN_PARAM);
    boolean respectRobots = !request.parameters.containsKey(ROBOTS_PARAM);
    String givenDepth = request.parameters.containsKey(DEPTH_PARAM)
        ? request.parameters.get(DEPTH_PARAM) : String.valueOf(Integer.MAX_VALUE);
    int depth;
    try {
      depth = Integer.parseInt(givenDepth);
    } catch (NumberFormatException e) {
      return generateMainPage(ILLEGAL_DEPTH + givenDepth);
    }
    String givenPort = request.parameters.containsKey(PORT_PARAM)
        ? request.parameters.get(PORT_PARAM) : DEFAULT_PORT;
    int port;
    try {
      port = Integer.parseInt(givenPort);
    } catch (NumberFormatException e) {
      return generateMainPage(ILLEGAL_DEPTH + givenPort);
    }
    String resource = DEFAULT_RESOURCE;
    try {
      if (request.parameters.containsKey(RESOURCE_PARAM)) {
        resource = 
            URLDecoder.decode(request.parameters.get(RESOURCE_PARAM), DEFAULT_URL_ENCODING);
      }
    } catch (UnsupportedEncodingException e) {
      return generateMainPage(ILLEGAL_RESOURCE + e.getMessage());
    }
    String error = crawler.crawl(domain, port, resource, respectRobots, depth);
    if (error.isEmpty()) {
      crawlerRunning = true;
      return generateMainPage(CRAWLER_STARTED);
    }
    return generateMainPage(CRAWLER_FAILED + error);
  }

  @Override
  public void doneCrawling(CrawlInfo info) {
    String domain = info.getDomain();
    String link = String.format(
        "%s%s_%s.html", 
        INFO_DIR_NAME, domain, 
        LINK_FORMATTER.format(new Date(info.getCreationTime())));
    domainToLink.put(domain, link);
    linkToCrawlInfo.put(link, info);
    // Save the information for persistency.
    ObjectOutputStream oos = null;
    try {
      oos = new ObjectOutputStream(new FileOutputStream(options.getRoot() + link));
      oos.writeObject(info);
    } catch (IOException e) {
      System.err.println("Failed writing the crawled information to disk (" + link +
          "): " + e.getMessage());
    } finally {
      if (oos != null) {
        try {
          oos.close();
        } catch (IOException e) {
          // Ignore second chance exception.
        }
      }
    }
  }

  /**
   * Divides the first value by the second value. Returns 0 if the divider is 0.
   * @param size Size to divide.
   * @param divider The divider.
   * @return The division result, or 0 if the divider is 0.
   */
  private double safeDiv(long size, int divider) {
    if (divider == 0) {
      return 0;
    }
    return size * 1.0 / divider;
  }
  
  /**
   * Generates a page which tells what to status of the crawler and previously crawled domains. 
   * If the crawler is still running, the page will ask to refresh itself every 3 seconds until 
   * the crawler finishes running.
   * @param requst The request coming from the client.
   * @return The HTML body of the page.
   */
  private byte[] generateInternalPage(HttpRequest request) {
    String message = "";
    if (request.parameters.containsKey(MSG_PARAM)) {
      try {
        message = URLDecoder.decode(request.parameters.get(MSG_PARAM), DEFAULT_URL_ENCODING);
      } catch (UnsupportedEncodingException e) {
        // Nothing really we can do. Just ignore the message.
        message = "";
      }
    }
    StringBuilder data = new StringBuilder();
    data.append("<html>\n")
        .append(" <head>\n")
        .append("  <meta http-equiv=\"refresh\" content=\"5; url=")
        .append(HISTORY_N_MSG_HTML_PAGE).append("\"/>\n")
        .append(" </head>\n")
        .append(" <body>\n");
    boolean stillRunning = crawler.isRunning();
    if (!message.isEmpty()) {
      data.append("  <font size=\"6\" color=\"red\">").append(message).append("</font><br>\n");
    } else if (crawlerRunning) {
      message = stillRunning ? CRAWLER_RUNNING : CRAWLER_DONE;
      data.append("  <font size=\"6\" color=\"red\">").append(message).append("</font><br>\n");
      crawlerRunning = stillRunning;
    }
    data.append("  <font size=\"4\">\n")
        .append("   <u><font color=\"green\">Previously Crawled Domains:</font></u>\n");
    if (linkToCrawlInfo.size() == 0) {
      data.append("   <i>None</i>\n");
    } else {
      data.append("   <ol>\n");
      List<String> links = new ArrayList<String>(linkToCrawlInfo.keySet());
      Collections.sort(links, linksComparator);
      for (String link : links) {
        CrawlInfo info = linkToCrawlInfo.get(link);
        data.append("    <li><a href=\"")
            .append(link)
            .append("\">")
            .append(info.getDomain())
            .append("</a><font color=\"grey\"> (crawl time: ")
            .append(INFO_FORMATTER.format(new Date(info.getCreationTime())))
            .append(")</font></li>");
      }
      data.append("   </ol>\n");
    }
    data.append("  </font>\n")
        .append(" </body>\n")
        .append("</html>");
    
    return data.toString().getBytes();
  }
  
  /**
   * Generates a page with the information about a certain crawling.
   * @param info The crawling information to present in the page.
   * @return The content of the page.
   */
  private byte[] generateCrawlInfoPage(CrawlInfo info) {
    String domain = info.getDomain();
    boolean respectedRobots = info.isRespectRobots();
    List<String> crawledURLs = new ArrayList<String>(info.getCrawledURLs());
    Collections.sort(crawledURLs);
    List<String> connectedDomains = new ArrayList<String>(info.getConnectedDomains());
    Collections.sort(connectedDomains);
    StringBuilder data = new StringBuilder();
    data.append("<html>\n")
      .append(" <head><title>").append(domain).append("</title></head>\n")
      .append(" <body bgcolor=\"#EEEEFF\">\n")
      .append("  <h1><font color=\"blue\">\n")
      .append("   <u>Domain</u>: ").append(domain)
      .append("  </font></h1>\n")
      .append("  <font color=\"grey\" size=\"4\">")
      .append("   Port: ").append(info.getPort())
      .append("   Crawl Time: ").append(INFO_FORMATTER.format(new Date(info.getCreationTime())))
      .append("  </font>\n")
      .append("  <font size=\"4\">\n")
      .append("   <table border=\"1\" width=\"30%\" cellpadding=\"2\">\n")
      .append("    <tr bgcolor=\"#CCCCDD\">\n")
      .append("     <td>Type</td>\n")
      .append("     <td>Count</td>\n")
      .append("     <td>Total Size (B)</td>\n")
      .append("     <td>Average Size (B)</td>\n")
      .append("    <tr/><tr>\n")
      .append("     <td>Images</td>\n")
      .append("     <td>").append(info.getNumImages()).append("</td>\n")
      .append("     <td>").append(info.getSizeOfImages()).append("</td>\n")
      .append("     <td>").append(safeDiv(info.getSizeOfImages(), info.getNumImages()))
      .append("</td>\n")
      .append("    </tr><tr>\n")
      .append("     <td>Videos</td>\n")
      .append("     <td>").append(info.getNumVideos()).append("</td>\n")
      .append("     <td>").append(info.getSizeOfVideos()).append("</td>\n")
      .append("     <td>").append(safeDiv(info.getSizeOfVideos(), info.getNumVideos()))
      .append("</td>\n")
      .append("    </tr><tr>\n")
      .append("     <td>Documents</td>\n")
      .append("     <td>").append(info.getNumDocuments()).append("</td>\n")
      .append("     <td>").append(info.getSizeOfDocuments()).append("</td>\n")
      .append("     <td>").append(safeDiv(info.getSizeOfDocuments(), info.getNumDocuments()))
      .append("</td>\n")
      .append("    </tr><tr>\n")
      .append("     <td>Other pages</td>\n")
      .append("     <td>").append(info.getNumPages()).append("</td>\n")
      .append("     <td>").append(info.getSizeOfPages()).append("</td>\n")
      .append("     <td>").append(safeDiv(info.getSizeOfPages(), info.getNumPages()))
      .append("</td>\n")
      .append("    </tr>\n")
      .append("   </table>\n")
      .append("   <br>\n")
      .append("   <table width=\"25%\" cellpadding=\"2\">\n")
      .append("    <tr>\n")
      .append("     <td>Respected Robots: </td>\n")
      .append("     <td><font color=\"").append(respectedRobots ? "green" : "red")
      .append("\">").append(respectedRobots ? "Yes" : "No").append("</font></td>\n")
      .append("    </tr><tr>\n")
      .append("     <td>Total Extracted Links: </td>\n")
      .append("     <td>").append(info.getNumLinks()).append("</td>\n")
      .append("    </tr><tr>\n")
      .append("     <td>Total Crawled Links: </td>\n")
      .append("     <td>").append(crawledURLs.size()).append("</td>\n")
      .append("    </tr><tr>\n")
      .append("     <td>Total Connected Domains: </td>\n")
      .append("     <td>").append(connectedDomains.size()).append("</td>\n")
      .append("    </tr><tr>\n")
      .append("     <td>Average RTT (ms): </td>\n")
      .append("     <td>").append(info.getAverageRTT()).append("</td>\n")
      .append("    </tr>\n")
      .append("   </table>\n")
      .append("   <br>\n")
      .append("   <u>Connected Domains:</u><br>\n");
    if (connectedDomains.size() == 0) {
      data.append("   <i>None</i><br>\n");
    } else {
      data.append("   <ol>\n");
      for (String connectedDomain : connectedDomains) {
        data.append("    <li>");
        if (domainToLink.containsKey(connectedDomain)) {
          data.append("<a href=\"").append(domainToLink.get(connectedDomain)).append("\">")
          .append(connectedDomain).append("</a>");
        } else {
          data.append(connectedDomain);
        }
        data.append("</li>\n");
      }
      data.append("   </ol>\n");
    }
    data.append("   <u>Crawled Links:</u><br>\n");
    if (crawledURLs.size() == 0) {
      data.append("   <i>None</i><br>\n");
    } else {
      data.append("   <ol>\n");
      for (String url : crawledURLs) {
        data.append("    <li>").append(url).append("</li>\n");
      }
      data.append("   </ol>\n");
    }
    data.append("   <br>\n")
    .append("   <a href=\"").append(HISTORY_N_MSG_HTML_PAGE).append("\">Back to main page</a>\n")
    .append("  </font>\n")
    .append(" </body>\n")
    .append("</html>");

    return data.toString().getBytes();
  }
}
