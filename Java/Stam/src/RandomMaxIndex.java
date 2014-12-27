import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RandomMaxIndex {
  public static int randomMaxIndex(int[] nums) {
    if (nums == null || nums.length == 0) {
      return -1;
    }
    int max = nums[0];
    for (int num : nums) {
      if (num > max) {
        max = num;
      }
    }
    Random r = new Random();
//    List<Integer> indices = new ArrayList<Integer>();
//    for (int i = 0; i < nums.length; ++i) {
//      if (nums[i] == max) {
//        indices.add(i);
//      }
//    }
//    return indices.get(r.nextInt(indices.size()));
    int count = 0;
    for (int num : nums) {
      if (num == max) {
        count++;
      }
    }
    int pos = r.nextInt(count) + 1;
    for (int i = 0; i < nums.length; ++i) {
      if (nums[i] == max) {
        pos--;
        if (pos == 0) {
          return i;
        }
      }
    }
    return -1;
  }
}
