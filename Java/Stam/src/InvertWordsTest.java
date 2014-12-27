import junit.framework.TestCase;


public class InvertWordsTest extends TestCase{


  public void test() {
    char[] str = "    hello    world    how are    you   ".toCharArray();
    String exp = "    olleh    dlrow    woh era    uoy   ";
    InvertWords.invertWords(str);
    for (int i = 0; i < str.length; ++i) {
      assertEquals(exp.charAt(i), str[i]);
    }
  }
  
}
