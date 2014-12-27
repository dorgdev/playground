import junit.framework.TestCase;
import java.util.Random;;

public class BinaryTreeTest extends TestCase {

  public void test() {
    BinaryTree<Integer> root = new BinaryTree<Integer>(0);
    BinaryTree<Integer> temp = root;
    Random r = new Random();
    for (int i = 1; i < 10; ++i) {
      BinaryTree<Integer> newNode = new BinaryTree<Integer>(i);
      if (r.nextBoolean()) {
        temp.setLeft(newNode);
      } else {
        temp.setRight(newNode);
      }
      temp = newNode;
    }
    assertEquals(10, root.getHeight());
  }
}
