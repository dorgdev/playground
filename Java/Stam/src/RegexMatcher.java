

public class RegexMatcher {

  public static boolean match(String text, String pattern) {
    return matchWithPos(pattern, pattern.length() - 1, text, text.length() - 1);
  }

  private static boolean matchWithPos(String pattern, int patternPos, String text, int textPos) {
    if (patternPos == -1) {
      return textPos == -1;
    }
    if (pattern.charAt(patternPos) != '*') {
      if (textPos < 0) {
        return false;
      }
      if (matchChars(pattern.charAt(patternPos), text.charAt(textPos))) {
        return matchWithPos(pattern, patternPos - 1, text, textPos - 1);
      }
      return false;
    }
    if (patternPos-- == 0) {
      return false;
    }
    char patternChar = pattern.charAt(patternPos);
    // * with 0 occurrences.
    if (matchWithPos(pattern, patternPos - 1, text, textPos)) {
      return true;
    }
    // * with more than 0 occurrences.
    while (textPos >= 0 && matchChars(patternChar, text.charAt(textPos--))) {
      if (matchWithPos(pattern, patternPos - 1, text, textPos)) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchChars(char pattern, char c) {
    return (pattern == '.' || pattern == c);
  }
}
