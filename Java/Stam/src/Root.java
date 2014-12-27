
public class Root {

  public static final double DELTA = 0.0001;
  
  public static double root(double num) {
    if (num < 0) {
      throw new IllegalArgumentException("Negative number for root");
    }
    double lo = 0;
    double hi = num;
    double mid = (hi - lo) / 2;
    double mul = mid * mid;
    while (Math.abs(mul - num) > DELTA) {
      if (mul > num) {
        hi = mid;
      } else {
        lo = mid;
      }
      mid = lo + (hi - lo) / 2;
      mul = mid * mid;
    }
    return mid;
  }

}
