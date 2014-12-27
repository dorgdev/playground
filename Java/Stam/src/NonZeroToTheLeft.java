
public class NonZeroToTheLeft {

  public static int nonZeroToTheLeft(int[] arr) {
    if (arr == null || arr.length == 0) {
      return 0;
    }
    int left = 0;
    int right = arr.length - 1;
    int count = 0;
    while (left < right) {
      while (left < right && arr[left] != 0) {
        left++;
      }
      while (right >= left && arr[right] == 0) {
        right--;
        count++;
      }
      // Swap
      if (right > left) { 
        arr[left++] = arr[right];
        arr[right--] = 0;
        count++;
      }
    }
    return count;
  }
}
