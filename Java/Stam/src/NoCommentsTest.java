import junit.framework.TestCase;


public class NoCommentsTest extends TestCase{
  
  public void test() {
    String[] lines = {
        "this is a line with // a line comment.",
        "Here starts a multi /* line", 
        "comment that */ spans to here.",
        "No comments in this line."};
    String exp =
        "this is a line with \n" + 
        "Here starts a multi \n" +
        " spans to here.\n" +
        "No comments in this line.";
    String res = NoComments.noComments(lines);
    assertEquals(exp, res);
  }
  
}
