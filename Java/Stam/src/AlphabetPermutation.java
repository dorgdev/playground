import java.util.Map;
import java.util.Set;


public class AlphabetPermutation {

  public static int getNumCombinations(Map<String, String> alphabet, String text) {
    Set<String> valid = alphabet.keySet();
    int[] occurrences = new int[text.length()];
    for (int i = 0; i < occurrences.length; ++i) {
      occurrences[i] = -1;
    }
    int len = text.length();
    for (int startPos = len - 1; startPos >= 0; --startPos) {
      int count = 0;
      for (int endPos = len; endPos > startPos; --endPos) {
        String substr = text.substring(startPos, endPos);
        if (!valid.contains(substr)) {
          continue;
        }
        if (endPos == len) {
          count++;
        } else {
          count += occurrences[endPos];
        }
      }
      occurrences[startPos] = count;
    }
    for (int i : occurrences) {
      System.out.print(i + ",");
    }
    System.out.println();
    return occurrences[0];
  }
}
