import junit.framework.TestCase;


public class NonZeroToTheLeftTest extends TestCase {

  public void test() {
    int[] arr = {1, 3, 6, 7, 4, 0, 6, 4, 0, 4, 3, 1, 5, 9, 0, 5, 0, 3, 0};
    int expZeros = 5;
    assertEquals(expZeros, NonZeroToTheLeft.nonZeroToTheLeft(arr));
    for (int i = 0; i < arr.length - expZeros; ++i) {
      assertFalse(0 == arr[i]);
    }
    for (int i = expZeros; i > 0; --i) {
      assertEquals(0, arr[arr.length - i]);
    }
  }

}
