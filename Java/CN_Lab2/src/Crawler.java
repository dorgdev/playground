import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Handles a single crawl of a website.
 */
public class Crawler {

  /**
   * An inner interface, representing a callback that should be invoked when the crawler
   * finishes the crawling.
   */
  public static interface CrawlCallback {
    /**
     * Notifying the domain crawling had finished.
     * @param info The information associated with the finished crawling process.
     */
    public void doneCrawling(CrawlInfo info);
  }

  /** Error message to return if the crawler is already running. */
  public static final String ALREADY_RUNNNING = "Crawler already running.";
  /** Error message to return if the domain is unreachable. */
  public static final String INAVLID_DOMAIN_NAME = "Inavlid domain name or port given: ";
  
  /** The options configuring the way the crawler will work. */
  private ServerOptions options;
  /** Number of outstanding operations (downloads and analyzes). */
  private int outstandingOps;
  /** The current crawling link callback used for each new found link. */
  private LinkCallback linkCB;
  /** The current crawling data callback used for each new downloaded data. */
  private DataCallback dataCB;
  /** The callback to be invoked once a crawling finishes. */
  private CrawlCallback crawlCB;
  /**Whether the crawler is in the middle of a crawling process or not. */
  private boolean crawling;
  
  /**
   * Create a new {@link Crawler}.
   * @param options The {@link ServerOptions} used for configuring the crawler.
   * @param crawlCB The callback to be invoked once a crawling finishes.
   */
  public Crawler(ServerOptions options, CrawlCallback crawlCB) {
    this.options = options;
    this.outstandingOps = 0;
    this.crawlCB = crawlCB;
    this.crawling = false;
  }

  /**
   * @return Whether the crawler is currently running or not.
   */
  public boolean isRunning() {
    synchronized (this) {
      return crawling;
    }
  }
  
  /**
   * Starts the crawling mechanism: Starts the downloaders and analyzers pools, check the robots
   * (if necessary), and starts the crawling from "/". Returns when the crawling ends.
   * @param domain The crawled domain.
   * @param port The crawled domain's port.
   * @param startPage The first page to start the crawl with.
   * @param respectRobots Whether the robots resource should be respected.
   * @param depth The maximal depth of the recursion.
   * @return An empty message for a successful crawl launch, or an error message if there was
   *         any problem with starting the crawl.
   */
  public String crawl(String domain, int port, String startPage, boolean respectRobots, 
      int depth) {
    // Make sure there's no concurrent crawling going on.
    synchronized (this) {
      if (crawling) {
        return ALREADY_RUNNNING;
      }
      crawling = true;
    }
    // Verify domain.
    Socket socket = null;
    try {
      socket = new Socket(domain, port);
    } catch (IOException e) {
      crawling = false;
      // The domain is invalid, failed to connect to it.
      return INAVLID_DOMAIN_NAME + domain + ":" + port;
    } finally {
      try {
        if (socket != null) {
          socket.close();
        }
      } catch (IOException e) {
        // Ignore seconds chance exception.
      }
    }
    // Create a new thread to host the crawling and start it.
    final String fDomain = domain;
    final int fPort = port;
    final String fStartPage = startPage;
    final boolean fRespectRobots = respectRobots;
    final int fDepth = depth > 0 ? depth : Integer.MAX_VALUE;
    Thread runner = new Thread(new Runnable() {
      @Override
      public void run() {
        doCrawl(fDomain, fPort, fStartPage, fRespectRobots, fDepth);
      }
    }); 
    runner.start();
    return "";
  }

  /**
   * Performs the crawl itself (the internal implementation of the crawl request).
   * @see Crawler#crawl(String, String, boolean, int)
   */
  public void doCrawl(String domain, int port, String startPage, boolean respectRobots, 
      int depth) {
    final CrawlInfo info = new CrawlInfo(domain, port, respectRobots);
    // Create the downloaders queue/thread-pool.
    int maxDownloaders = options.getMaxDownloaders();
    final BlockingQueue<Runnable> downloadQueue = new LinkedBlockingQueue<Runnable>();
    final ThreadPoolExecutor downloaders = 
        new ThreadPoolExecutor(maxDownloaders, maxDownloaders, Long.MAX_VALUE,
            TimeUnit.MILLISECONDS, downloadQueue);

    // Create the analyzers queue/thread-pool.
    int maxAnalyzers = options.getMaxAnalyzers();
    final BlockingQueue<Runnable> analyzeQueue = new LinkedBlockingQueue<Runnable>();
    final ThreadPoolExecutor analyzers = 
        new ThreadPoolExecutor(maxAnalyzers, maxAnalyzers, Long.MAX_VALUE,
            TimeUnit.MILLISECONDS, analyzeQueue);
    
    // Create the robots verifier.
    final RobotsHandler robots = getRobotsHandler(domain, port, respectRobots);
    
    // Set the data callback (for new data downloaded).
    dataCB = new DataCallback() {
      @Override
      public void dataAvailable(String resource, Properties headers, String data, long rtt, int depth) {
        final DataAnalyzer analyzer = 
            new DataAnalyzer(resource, headers, data, rtt, depth, info, linkCB);
        increaseOps();
        analyzers.execute(new Runnable() {
          @Override
          public void run() {
            System.out.println("Analyzer Queue Items Left: " + analyzeQueue.size());
            try {
              analyzer.analyze();
            } catch (Exception e) {
              // Handle the exception nicely, so the thread won't die.
              System.err.println("Caught an exception while analyzing: " + e.getMessage());
              e.printStackTrace();
            }
            decreaseOps();
          }
        });
      }
    };

    // Set the link callback (for extracted links).
    linkCB = new LinkCallback() {
      @Override
      public void newLink(String resource, String referer, boolean getContent, int depth) {
        // Do not crawl pass the requested crawl depth.
        if (depth <= 0) {
          return;
        }
        // Validate the link against the robots.
        if (!robots.isValid(options.getUserAgent(), resource)) {
          System.out.println("Resource banned by robots: " + resource);
          return;
        }
        // Do not crawl the same link twice.
        if (info.wasURLCrawled(resource)) {
          return;
        } else {
          info.crawledURL(resource);
        }
        // Always get the content if "useHead" is false.
        getContent |= !options.useHead();
        // Create a new downloader and enqueue it.
        final URLDownloader downloader = new URLDownloader(info.getDomain(), info.getPort(), 
            depth, resource, referer, options.getUserAgent(), getContent, dataCB);
        increaseOps();
        downloaders.execute(new Runnable() {
          @Override
          public void run() {
            System.out.println("Download Queue Items Left: " + downloadQueue.size());
            try {
              downloader.download();
            } catch (Exception e) {
              // Handle the exception nicely, so the thread won't die.
              System.err.println("Caught an exception while downloading: " + e.getMessage());
              e.printStackTrace();
            }
            decreaseOps();
          }
        });
      }
    };
    
    // Start the crawling.
    linkCB.newLink(startPage, null, true, depth);

    // Wait for it to finish.
    synchronized (this) {
      try {
        while (outstandingOps > 0) {
          this.wait();
        }
      } catch (InterruptedException e) {
        // Ignore.
      }
    }
    // Were done. Clear the resources and invoke the crawling callback.
    System.out.println("Done crawling the domain: " + domain);
    downloaders.shutdown();
    analyzers.shutdown();
    crawlCB.doneCrawling(info);
    // Mark the crawler as not running, so new crawls could start.
    synchronized (this) {
      crawling = false;
    }
  }
  
  /**
   * Increases the number of outstanding operations by one.
   */
  public synchronized void increaseOps() {
    outstandingOps++;
  }

  /**
   * Decreases the number of outstanding operation by one. If gets to 0, issues a notification.
   */
  public synchronized void decreaseOps() {
    outstandingOps--;
    if (outstandingOps == 0) {
      notify();
    }
  }

  /**
   * Constructs a new {@link RobotsHandler} for the cralwer. If the crawler should respect the
   * robots resource of the domain, its content should be downloaded and analyzed.
   * @param domain The domain to get its robots file.
   * @param port The port of the domain incoming HTTP requests.
   * @param respectRobots Whether the robots file should be respected or not.
   * @return A robots file handler which can accept/decline page requests.
   */
  public RobotsHandler getRobotsHandler(String domain, int port, boolean respectRobots) {
    RobotsHandler robotsHandler = new RobotsHandler();
    if (respectRobots) {
      URLDownloader robotsDownloader = new URLDownloader(domain, port, 0, "/robots.txt", null,
          options.getUserAgent(), true, null);
      robotsDownloader.download();
      if (robotsDownloader.getData() == null) {
        // There was a problem. Continue without concerning the robots resource.
        System.err.println("Problem getting the robots file. Continue without it.");
        robotsHandler.acceptAll();
      } else {
        try {
          robotsHandler.init(robotsDownloader.getData());
        } catch (IOException e) {
          // Shouldn't happen. Print a message and continue.
          System.err.println("Problem parsing the robots file: " + e.getMessage());
        }
      }
    } else {
      robotsHandler.acceptAll();
    }
    return robotsHandler;
  }
}
