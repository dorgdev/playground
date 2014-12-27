import junit.framework.TestCase;


public class RegexMatcherTest extends TestCase {
  public void testMatcher() {
    assertTrue(RegexMatcher.match("abc", "abc"));
    assertTrue(RegexMatcher.match("abc", "a*bc"));
    assertFalse(RegexMatcher.match("aaaaaaabc", "c*bc"));
    assertTrue(RegexMatcher.match("aaaaaaabc", "a.*bc"));
    assertTrue(RegexMatcher.match("abbbbaaaaaabc", "ab*a*b*c"));
    assertTrue(RegexMatcher.match("abbbbaaaaaabc", "ab*a*h*bc"));
    assertTrue(RegexMatcher.match("bbd", "b*bbd"));
    assertTrue(RegexMatcher.match("bbd", ".*bbd"));
    assertFalse(RegexMatcher.match("bbd", ".*cbd"));
    assertTrue(RegexMatcher.match("", ".*"));
    assertTrue(RegexMatcher.match("", ".*c*"));
    assertFalse(RegexMatcher.match("bbbbdbdbdaac", ".*d"));
    assertTrue(RegexMatcher.match("bbbb", "b*c*"));
    assertTrue(RegexMatcher.match("bbbb", "b*b*c*"));
    assertTrue(RegexMatcher.match("abcd", ".*d"));
    assertFalse(RegexMatcher.match("abcd", ".*d*c*c*e*hhh*j*.*c*h"));
  }
}