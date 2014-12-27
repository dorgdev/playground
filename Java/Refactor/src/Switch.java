import java.util.HashMap;
import java.util.Map;


public class Switch {

  public static final int ERR_FILE_NOT_FOUND_CODE = 0;
  public static final String ERR_FILE_NOT_FOUND_MSG = "not found";
  public static final int ERR_PERMISSION_DENIED_CODE = 1;
  public static final String ERR_PERMISSION_DENIED_MSG = "permission denied";
  public static final int ERR_NOT_ENOUGH_SPACE_CODE = 2;
  public static final String ERR_NOT_ENOUGH_SPACE_MSG = "no space";
  public static final String ERR_UNKNOWN_MSG = "unknwon";
  /* ... */
  
  public static Map<Integer, String> errorToMsg = new HashMap<Integer, String>();
  static {
    errorToMsg.put(ERR_FILE_NOT_FOUND_CODE, ERR_FILE_NOT_FOUND_MSG);
    errorToMsg.put(ERR_PERMISSION_DENIED_CODE, ERR_PERMISSION_DENIED_MSG);
    errorToMsg.put(ERR_NOT_ENOUGH_SPACE_CODE, ERR_NOT_ENOUGH_SPACE_MSG);
    /* ... */
  }
  
  public String getErrorName(int val) {
    if (errorToMsg.containsKey(val)) {
      return errorToMsg.get(val);
    }
    return ERR_UNKNOWN_MSG;
  }
  
}
