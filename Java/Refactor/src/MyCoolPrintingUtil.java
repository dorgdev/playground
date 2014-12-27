import java.util.List;

public class MyCoolPrintingUtil {

  public static void printWithSpace(List<String> list) {
    print(list, ' ');
  }

  public static void printWithComma(List<String> list) {
    print(list, ',');
  }

  public static void printWithNewLine(List<String> list) {
    print(list, '\n');
  }

  public static void print(List<String> list, char delim) {
    for (String str : list) {
      System.out.print(str + delim);
    }
  }
  
}


