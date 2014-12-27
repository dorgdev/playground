import junit.framework.TestCase;


public class RootTest extends TestCase {

  public void testRoot() {
    for (double i = 0; i < 100; ++i) {
      assertEquals(Math.sqrt(i), Root.root(i), Root.DELTA);
    }
    try {
      Root.root(-1);
      fail("No exception?");
    } catch (Exception e) {
      // Ignore.
    }
  }
  
}
