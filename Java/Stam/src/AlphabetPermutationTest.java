import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.TestCase;


public class AlphabetPermutationTest extends TestCase {

  public void test() {
    Map<String, String> alpha = new HashMap<String, String>();
    alpha.put("1", "A");
    alpha.put("11", "K");
    String text = "111";
    assertEquals(3, AlphabetPermutation.getNumCombinations(alpha, text));
  }
 
  public void test2() {
//    StringTokenizer keys = 
//        new StringTokenizer("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16," +
//                            "17,18,19,20,21,22,23,24,25,26", ",");
//    StringTokenizer vals = 
//        new StringTokenizer("A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z", ",");
    String[] keys = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26".split(",");
    Map<String, String> map = new HashMap<String, String>();
    for (String key : keys) {
      map.put(key, "AAA");
    }
    assertEquals(3,  AlphabetPermutation.getNumCombinations(map, "111"));
    assertEquals(0,  AlphabetPermutation.getNumCombinations(map, "303"));
    assertEquals(2,  AlphabetPermutation.getNumCombinations(map, "2345"));
    assertEquals(2,  AlphabetPermutation.getNumCombinations(map, "8888"));
  }
}
