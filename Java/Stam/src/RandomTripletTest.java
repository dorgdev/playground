import junit.framework.TestCase;


public class RandomTripletTest extends TestCase {

  public void test() {
    final String str = "hello world";
    RandomTriplet rt = new RandomTriplet(str);
    for (int i = 0; i < 10; ++i) {
      System.out.println("triplet: " + rt.getRandomeTriplet());
    }
  }
  
}
