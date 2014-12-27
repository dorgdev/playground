
public class Example {

  public double doSomething(double x, double y) {
    return Math.sqrt(sqr(x) + sqr(y));
  }

  private double sqr(double x) {
    return x * x;
  }

}
