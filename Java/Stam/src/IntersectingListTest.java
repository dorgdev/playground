import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class IntersectingListTest extends TestCase {

  public void test() {
    //      0123456789
    // U1:  +--------+
    // U2:    +--+
    // U3:  +---+
    // U4:      +----+
    // U5:   +----+
    // U6:          ++
    List<Integer> start = Arrays.asList(0, 2, 0, 4, 1, 8);
    List<Integer> end =   Arrays.asList(9, 5, 4, 9, 6, 9);
    List<Integer> count = Arrays.asList(2, 3, 4, 4, 5, 4, 3, 2, 3, 3);
    //System.out.println("Checking: -1");
    assertEquals(0, IntersectingList.users(-1, start, end));
    for (int i = 0; i < count.size(); ++i) {
      //System.out.println("Checking: " + i);
      assertEquals(count.get(i).intValue(), IntersectingList.users(i, start, end));
    }
    //System.out.println("Checking: 10");
    assertEquals(0, IntersectingList.users(10, start, end));
  }

  public int count(int time, List<Integer> start, List<Integer> end) {
    int count = 0;
    for (int i = 0; i < start.size(); ++i) {
      if (start.get(i) <= time && end.get(i) >= time) {
        count++;
      }
    }
    return count;
  }
  
  public void test2() {
    List<Integer> start = Arrays.asList(0,5,3,7,3,8, 4,1,5,9, 0,4,3,6,8);
    List<Integer> end =   Arrays.asList(9,9,6,8,6,10,7,3,6,12,2,5,8,7,12);
    for (int i = -1; i < 14; ++i) {
      assertEquals(count(i, start, end), IntersectingList.users(i, start, end));
    }
  }
  
}
