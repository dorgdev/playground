import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Handles the content of a robots.txt configuration, telling which request should be made and
 * which shouldn't. 
 */
public class RobotsHandler {

  /** The "user-agent" tag in the robots' file. */
  public static final String USER_AGENT_TAG = "User-Agent";
  /** The "disallow" tag in the robots' file. */
  public static final String DISALLOW_TAG = "Disallow";
  /** The "allow" tag in the robots' file. */
  public static final String ALLOW_TAG = "Allow";
  /** The value to set for the wildcard. */
  public static final String WILDCARD_REPLACE = ".*";
  /** All the required replacement to make a regular expression from a robots' entry  */
  public static final String[][] PATTERN_SUBS = new String[][] {
    new String[] {"\\*", WILDCARD_REPLACE},
    new String[] {"\\?", "\\\\?"}
  };
  /** A pattern equivalent to the end of the line. */
  public static final String END_OF_LINE_PATTERN = "$";
  /** The start of every regular expression pattern used. */
  public static final String PATTERN_START = "(";
  /** The end of every regular expression pattern used. */
  public static final String PATTERN_END = ")";
  /** The OR operator of every regular expression used. */
  public static final String PATTERN_OR = "|";

  /** A control list for different user-agents, for allowed and disallowed resources. */
  private List<Pattern[]> control;
  /** Whether the robots should accept all requests. */
  private boolean acceptAll;
  
  /**
   * Create a new empty (all allowed) robots handler.
   */
  public RobotsHandler() {
    control = new LinkedList<Pattern[]>();
    acceptAll = false;
  }

  /**
   * Initializes the robots handler with the content of the robots file of the domain.
   * @param content The robots resource of the domain.
   * @throws IOException In case of an IO problem while intializing the robots.
   */
  public void init(String content) throws IOException {
    String line;
    StringBuilder disallowBuilder = null;
    StringBuilder allowBuilder = null;
    String userAgent = null;
    BufferedReader reader = new BufferedReader(new StringReader(content));
    while ((line = reader.readLine()) != null) {
      String[] temp = line.split(": ");
      // Check for a legal line. Ignore other lines.
      if (temp.length != 2) {
        continue;
      }
      String tag = temp[0];
      boolean isUserAgent = tag.equalsIgnoreCase(USER_AGENT_TAG);
      // Suuport wildcards.
      String value = temp[1];
      for (String[] pair : PATTERN_SUBS) {
        value = value.replaceAll(pair[0], pair[1]);
      }
      if (!(value.endsWith(END_OF_LINE_PATTERN) || isUserAgent)) {
        value += WILDCARD_REPLACE;
      }
      // Handle differently according to the tag type.
      if (isUserAgent) {
        // It's a new user-agent. Close any previous one and start a new list.
        if (userAgent != null) {
          addUserAgent(userAgent, disallowBuilder, allowBuilder);
        }
        disallowBuilder = new StringBuilder(PATTERN_START); 
        allowBuilder = new StringBuilder(PATTERN_START); 
        userAgent = value;
      } else if (tag.equalsIgnoreCase(DISALLOW_TAG) || tag.equalsIgnoreCase(ALLOW_TAG)) {
        // It's a new allow/disallow value.
        StringBuilder builder = 
            tag.equalsIgnoreCase(DISALLOW_TAG) ? disallowBuilder : allowBuilder;
        if (builder == null) {
          System.err.println("Got a disallow/allow record without a user-agent first.");
          continue;
        }
        if (builder.length() > 1) {
          builder.append(PATTERN_OR);
        }
        builder.append(value);
      }
    }
    if (userAgent != null) {
      addUserAgent(userAgent, disallowBuilder, allowBuilder);
    }
  }
  
  /**
   * Checks whether a certain link for a certain user-agent is allowed according to the robots
   * policy of the domain.
   * @param userAgent The user-agent performing the request.
   * @param resource The resource to check.
   * @return True if the request is valid, false otherwise.
   */
  public boolean isValid (String userAgent, String resource) {
    if (acceptAll) {
      return true;
    }
    for (Pattern[] item : control) {
      // Only check first matching user-agent record.
      if (!item[0].matcher(userAgent).matches()) {
        continue;
      }
      // Matched. Check if disallowed redource.
      if (!item[1].matcher(resource).matches()) {
        return true;
      }
      // Item disallowed. Check for exceptions.
      return item[2].matcher(resource).matches();
    }
    // No matching agent, assume disallowed.
    return false;
  }
  
  /**
   * Tells the {@link RobotsHandler} to accept all requests.
   */
  public void acceptAll() {
    acceptAll = true;
  }
  
  /**
   * Adds a new control record to the control list.
   * @param userAgent The user agent of which the allow/disallow patterns refer to.
   * @param disallow A pettern for banned resources. 
   * @param allow A pettern for exceptions in the banned resources.
   */
  private void addUserAgent(String userAgent, StringBuilder disallow, StringBuilder allow) {
    control.add(new Pattern[] {
        Pattern.compile(userAgent),
        Pattern.compile(disallow.append(PATTERN_END).toString()),
        Pattern.compile(allow.append(PATTERN_END).toString()) });
  }
}
