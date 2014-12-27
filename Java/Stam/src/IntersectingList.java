import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class IntersectingList {

  private static class UserTime implements Comparable<UserTime> {
    public UserTime(int start, int end) {
      this.byEnd = true;
      this.start = start;
      this.end = end;
    }

    public int compareTo(UserTime other) {
      return byEnd ? this.end - other.end : this.start - other.start;
    }
    
    public boolean byEnd;
    public int start;
    public int end;
  }

  public static int users(int time, List<Integer> startTime, List<Integer> endTime) {
    // Sort the lists (together).
    List<UserTime> times = new ArrayList<UserTime>(startTime.size());
    for (int i = 0; i < startTime.size(); ++i) {
      times.add(new UserTime(startTime.get(i), endTime.get(i)));
    }
    Collections.sort(times);
    // Binary search something smaller than time (first one).
    int hi = times.size();
    int lo = 0;
    int mid = (hi + lo) / 2;
    while (hi > lo) {
      if (times.get(mid).end >= time) {
        hi = mid;
      } else if (times.size() > mid + 1 && times.get(mid + 1).end >= time) {
        break;
      } else {
        lo = mid;
      }
      mid = lo + (hi - lo) / 2;
      if (mid == lo) break;
    }
    int endBeforeTime = mid + (times.get(mid).end < time ? 1 : 0);
    
    for (UserTime ut : times) {
      ut.byEnd = false;
    }
    Collections.sort(times);
    
    hi = times.size();
    lo = 0;
    mid = (hi + lo) / 2;
    while (hi > lo) {
      if (times.get(mid).start <= time) {
        lo = mid == lo ? mid + 1 : mid;
      } else if (mid > 0 && times.get(mid - 1).start <= time) {
        break;
      } else {
        hi = mid;
      }
      mid = lo + (hi - lo) / 2;
    }
    int startAfterTime = times.size() - mid;
    return times.size() - endBeforeTime - startAfterTime;
  }  
}
