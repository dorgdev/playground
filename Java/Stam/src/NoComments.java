
public class NoComments {

  public static String noComments(String[] lines) {
    StringBuffer sb = new StringBuffer();
    boolean mlComment = false;
    for (String line : lines) {
      for (int i = 0; i < line.length(); ++i) {
        boolean last = i == line.length() - 1;
        char c = line.charAt(i);
        if (mlComment) {
          if (c == '*' && !last && line.charAt(i+1) == '/') {
            mlComment = false;
            i++;
          }
          continue;
        }
        if (c == '/' && !last) {
          char nc = line.charAt(i+1);
          if (nc == '/') {
            break;
          } else if (nc == '*') {
            mlComment = true;
            i++;
            continue;
          }
        }
        sb.append(c);
      }
      sb.append('\n');
    }
    return sb.deleteCharAt(sb.length() - 1).toString();
  }

}
