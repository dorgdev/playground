
public class BinaryTree<T> {
  public BinaryTree(T value) {
    this.value = value;
    this.left = null;
    this.right = null;
  }
  
  public BinaryTree getLeft() { return left; }
  public void setLeft(BinaryTree left) { this.left = left; }
  public boolean hasLeft() { return left != null; }
  
  public BinaryTree getRight() { return right; }
  public void setRight(BinaryTree right) { this.right = right; }
  public boolean hasRight() { return right != null; }
  
  public T value() { return value; }
  
  public int getHeight() {
    int heightLeft = hasLeft() ? left.getHeight() : 0;
    int heightRight = hasRight() ? right.getHeight() : 0;
    return 1 + Math.max(heightLeft, heightRight);
  }
  
  private T value;
  private BinaryTree left;
  private BinaryTree right;
}
