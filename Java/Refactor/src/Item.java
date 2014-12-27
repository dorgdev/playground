

public class Item {

  public static class ItemId {
    private String id;
    public ItemId(String id) {
      this.setId(id);
    }
    public String getId() {
      return id;
    }
    public void setId(String id) {
      this.id = id;
    }
  }
  
  private ItemId id;
  
  public Item(ItemId id /* ... */) {
    this.setId(id);
    // ...
  }

  public ItemId getId() {
    return id;
  }

  public void setId(ItemId id) {
    this.id = id;
  }
  
  // ...
}
