import java.util.List;
import java.util.Set;


public class Example12 {

  public class MarriageAgency {
    List<Person> customers;
    // ...
    public Person findMatch(Person newCustomer) {
      for (String interest : newCustomer.getInterests()) {
        for (Person customer : customers) {
          if (customer.getInterests().contains(interest)) {
            return customer;
          }
        }
      }
      return null;
    }
  }
  
  public class Person {
    Set<String> interests;
    // ...
    public void addInterest(String interest) { interests.add(interest); }
    public Set<String> getInterests() { return interests; }
  }
}
