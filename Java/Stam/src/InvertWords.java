
public class InvertWords {
  public static boolean validLetter(char c) {
    return c >= 'a' && c <= 'z';
  }
  public static void invertWords(char[] str) {
    int wStart = 0;
    while (wStart < str.length) {
      while (wStart < str.length && !validLetter(str[wStart])) {
        wStart++;
      }
      int wEnd = wStart + 1;
      while (wEnd < str.length && validLetter(str[wEnd])) {
        wEnd++;
      }
      int left = wStart;
      int right = wEnd - 1;
      while (left < right) {
        char temp = str[left];
        str[left++] = str[right];
        str[right--] = temp;
      }
      wStart = wEnd + 1;
    }
  }
}
