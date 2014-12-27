import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;


public class RandomMaxIndexTest extends TestCase {

  public void test() {
    int[] nums = {1, 3, 5, 7, 9 /*4*/, 2, 6, 2, 7, 9 /*9*/, 4, 2, 1, 0, 5, 3, 6, 8, 4, 7, 2, 9 /*21*/, 3};
    Set<Integer> indices = new HashSet<Integer>();
    while (indices.size() < 3) {
      indices.add(RandomMaxIndex.randomMaxIndex(nums));
    }
    assertTrue(indices.contains(4));
    assertTrue(indices.contains(9));
    assertTrue(indices.contains(21));
  }
}
