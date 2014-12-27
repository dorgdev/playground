import java.util.Date;
import java.util.List;


public class ShoppingCart {

  public class Item {
    public Item(String name, long itemId, double price, 
                double shipment, int quantity, Date date) {
      // ...
    }
    
    // ...
  }
  
  public void addItemToCart(Item item) {
    commitToDB(item);
    createOrderRequest(item);
    findSuggestionsForItem(item);
    redirectToSuccessPage(item);
  }
  
  public void commitToDB(Item item) {
    // ...
  }

  public void createOrderRequest(Item item) {
    // ...
  }

  public void findSuggestionsForItem(Item item) { 
    // ...
  }
  
  public void redirectToSuccessPage(Item item) {
    // ...
  }
  
}
