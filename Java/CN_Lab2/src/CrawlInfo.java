import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the information about a single crawling run (per domain).
 * This class is thread-safe.
 */
public class CrawlInfo implements Serializable {

  /** The serial version ID for thr serializable interface. */
  private static final long serialVersionUID = 1;
  /** The domain this cralwing info refers to. */
  private String domain;
  /** The int of the cralwed domain. */
  private int port;
  /** Should this crawl respect the robots resource of the domain. */
  private boolean respectRobots;
  /** Number of images crawled so far. */
  private int numImages;
  /** Total size of images crawled so far. */
  private long sizeOfImages;
  /** Number of videos crawled so far. */
  private int numVideos;
  /** Total size of videos crawled so far. */
  private long sizeOfVideos;
  /** Number of documents crawled so far. */
  private int numDocuments;
  /** Total size of documents crawled so far. */
  private long sizeOfDocuments;
  /** Number of pages crawled so far. */
  private int numPageVisited;
  /** Total size of pages crawled so far. */
  private long sizeOfPageVisited;
  /** Number of linkes seen so far. */
  private int numLinks;
  /** The connected domains seen so far in the crawling process. */
  private Set<String> connectedDomains;
  /** Total RTT time for all crawled links. */
  private long totalRTTtime;
  /** Resources vistied already. */
  Set<String> crawledURLs;
  /** The time (in ms since epoch) when the instance was created. */
  private long creationTime;

  /**
   * Creating a new instance.
   * @param domain The domain this information represents.
   * @param port The crawled domain's port.
   * @param respectRobots Should the crawling respect the robots of the domain.
   */
  public CrawlInfo(String domain, int port, boolean respectRobots) {
    this.domain = domain;
    this.port = port;
    this.respectRobots = respectRobots;
    this.numImages = 0;
    this.sizeOfImages = 0;
    this.numVideos = 0;
    this.sizeOfVideos = 0;
    this.numDocuments = 0;
    this.sizeOfDocuments = 0;
    this.numPageVisited = 0;
    this.sizeOfPageVisited = 0;
    this.numLinks = 0;
    this.connectedDomains = new HashSet<String>();
    this.totalRTTtime = 0;
    this.crawledURLs = new HashSet<String>();
    this.creationTime = System.currentTimeMillis();
  }
  
  /**
   * @return The domain to which this crawling info refers to.
   */
  public String getDomain() {
    return domain;
  }
  
  /**
   * @return The crawled domain's port.
   */
  public int getPort() {
    return port;
  }

  /**
   * Adds a resource to the list of crawled resources.
   * @param resource The crawled resource.
   */
  public synchronized void crawledURL(String resource) {
    crawledURLs.add(resource);
  }

  /**
   * Checks whether a certain resource was crawled already.
   * @param url The resource to check.
   * @return Whether it was cralwed already or not.
   */
  public synchronized boolean wasURLCrawled(String url) {
    return crawledURLs.contains(url);
  }
  
  /**
   * Returns a copy of the URLs crawled so far for concurrency reasons.
   * @return The URLs crawled so far.
   */
  public synchronized Set<String> getCrawledURLs() {
    Set<String> copy = new HashSet<String>(crawledURLs);
    return copy;
  }
  
  /**
   * @return The number of images crawled so far.
   */
  public synchronized int getNumImages() {
    return numImages;
  }

  /**
   * @return The total size of images crawled so far.
   */
  public synchronized long getSizeOfImages() {
    return sizeOfImages;
  }

  /**
   * Add a new image to the cralwed data.
   * @param sizeOfImage The size of the image.
   * @param rtt RTT of the resource.
   */
  public synchronized void newImage(long sizeOfImage, long rtt) {
    numImages++;
    sizeOfImages += sizeOfImage;
    totalRTTtime += rtt;
  }

  /**
   * @return The number of videos crawled so far.
   */
  public synchronized int getNumVideos() {
    return numVideos;
  }

  /**
   * @return The total size of videos crawled so far.
   */
  public synchronized long getSizeOfVideos() {
    return sizeOfVideos;
  }

  /**
   * Add a new video to the cralwed data.
   * @param sizeOfVideo The size of the video.
   * @param rtt RTT of the resource.
   */
  public synchronized void newVideo(long sizeOfVideo, long rtt) {
    numVideos++;
    sizeOfVideos += sizeOfVideo;
    totalRTTtime += rtt;
  }

  /**
   * @return The number of docs crawled so far.
   */
  public synchronized int getNumDocuments() {
    return numDocuments;
  }

  /**
   * @return The total size of docs crawled so far.
   */
  public synchronized long getSizeOfDocuments() {
    return sizeOfDocuments;
  }

  /**
   * Add a new doc to the cralwed data.
   * @param sizeOfDocument The size of the docs.
   * @param rtt RTT of the resource.
   */
  public synchronized void newDocument(long sizeOfDocument, long rtt) {
    numDocuments++;
    sizeOfDocuments += sizeOfDocument;
    totalRTTtime += rtt;
  }

  /**
   * @return The number of pages crawled so far.
   */
  public synchronized int getNumPages() {
    return numPageVisited;
  }

  /**
   * @return The total size of pages crawled so far.
   */
  public synchronized long getSizeOfPages() {
    return sizeOfPageVisited;
  }

  /**
   * Add a new page to the cralwed data.
   * @param sizeOfPage The size of the pages.
   * @param rtt RTT of the resource.
   */
  public synchronized void newPage(long sizeOfPage, long rtt) {
    numPageVisited++;
    sizeOfPageVisited += sizeOfPage;
    totalRTTtime += rtt;
  }
  
  /**
   * @return The number of links visited so far.
   */
  public synchronized int getNumLinks() {
    return numLinks;
  }

  /**
   * @param numLinks the number of links to add.
   */
  public synchronized void addLinks(int numLinks) {
    this.numLinks += numLinks;
  }

  /**
   * @return The number of connected domains.
   */
  public synchronized int getNumConnectedDomains() {
    return connectedDomains.size();
  }

  /**
   * Returns a copy of the list for concurrency reasons.
   * @return All the domains to which this domain is connected to.
   */
  public synchronized Set<String> getConnectedDomains() {
    Set<String> copy = new HashSet<String>(connectedDomains);
    return copy;
  }

  /**
   * @param connectedDomains Additional connected domain to add.
   */
  public synchronized void addConnectedDomain(String connectedDomain) {
    if (!connectedDomain.equalsIgnoreCase(domain)) {
      connectedDomains.add(connectedDomain);
    }
  }

  /**
   * @return The average RTT of a request.
   */
  public synchronized double getAverageRTT() {
    int totalRequests = numDocuments + numImages + numVideos + numPageVisited;
    return (totalRequests == 0) ? 0 : (totalRTTtime * 1.0 / totalRequests);
  }

  /**
   * @return Whether this crawling respects the domain's robots instructions.
   */
  public boolean isRespectRobots() {
    return respectRobots;
  }
  
  /**
   * @return The time (in ms since epoch) when the instance was created.
   */
  public long getCreationTime() {
    return creationTime;
  }
}
