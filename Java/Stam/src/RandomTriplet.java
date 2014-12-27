import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class RandomTriplet {

  public RandomTriplet(String str) {
    if (str.length() < 3) {
      throw new IllegalArgumentException("Should be at least 3 chars in the String...");
    }
    this.str = str;
    this.strLen = str.length();
    this.random = new Random();
  }

  public String getRandomeTriplet() {
    List<Integer> indices = new ArrayList<Integer>(3);
    int index0 = random.nextInt(strLen);
    indices.add(index0);
    int index1 = random.nextInt(strLen);
    while (index1 == index0) {
      index1 = random.nextInt(strLen);
    }
    indices.add(index1);
    int index2 = random.nextInt(strLen);
    while (index2 == index0 || index2 == index1) {
      index2 = random.nextInt(strLen);
    }
    indices.add(index2);
    Collections.sort(indices);
    return "" + str.charAt(indices.get(0)) + str.charAt(indices.get(1)) + str.charAt(indices.get(2));
  }
  
  private final String str;
  private final int strLen;
  private Random random;
}
