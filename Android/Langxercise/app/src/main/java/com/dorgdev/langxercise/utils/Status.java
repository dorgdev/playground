package com.dorgdev.langxercise.utils;

/**
 * Created by dor on 7/17/16.
 */
public class Status implements Cloneable {

  public static enum ErrorCode {
    OK("OK"),
    BASE_LANG_DUP("Found a Dup"),
    LEARNT_LANG_DUP("Found a Dup"),
    BASE_LANG_EMPTY("Empty Value"),
    LEARNT_LANG_EMPTY("Empty Value"),
    UNKNOWN_DB_ERROR("DB Error");

    private final String title;

    ErrorCode(String title) {
      this.title = title;
    }

    public String getTitle() {
      return title;
    }
  }

  public static final Status OK = new Status();

  private String errorMessage = "";
  private ErrorCode errorCode = ErrorCode.OK;

  /**
   * OK (no error).
   */
  public Status() {}

  /**
   * Not OK.
   */
  public Status(ErrorCode errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public Status clone() {
    return new Status(errorCode, errorMessage);
  }

  public boolean isOK() {
    return errorCode == ErrorCode.OK;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
