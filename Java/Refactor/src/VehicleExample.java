
public class VehicleExample {

  
  public abstract class Operator {
    public abstract void accelerate();
  }
  
  public class Driver extends Operator {
    public void accelerate() { /* ... */ };
    public void handBrake() { /* ... */ };
  }

  public class Pilot extends Operator {
    public void accelerate() { /* ... */ };
    public void barrelRole() { /* ... */ };
  }

  
  public abstract class Vehicle {
    public abstract Operator getOperator();
  }

  public abstract class Car extends Vehicle {
    public Operator getOperator() { return new Driver(); }
  }

  public abstract class Plane extends Vehicle {
    public Operator getOperator() { return new Pilot(); }
  }

}
