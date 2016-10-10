package com.dorgdev.langxercise.db;

import android.provider.BaseColumns;

/**
 * Created by dor on 7/17/16.
 */
public class LearntWordsContract {

  public static abstract class LearntWords implements BaseColumns {
    public static final String TABLE_NAME = "words";
    public static final String COL_LANG = "lang";
    public static final String COL_BASE_WORD = "baseWord";
    public static final String COL_BASE_WORD_LC = "baseWordLowerCase";
    public static final String COL_LEARNT_WORD = "LearntWord";
    public static final String COL_LEARNT_WORD_LC = "LearntWordLowerCase";
    public static final String COL_CLASS_NUM = "classNum";
  }

  private static final String TEXT_TYPE = " TEXT";
  private static final String INTEGER_TYPE = " INTEGER";
  private static final String COMMA_SEP = ",";

  public static final String SQL_CREATE_ENTRIES =
      "CREATE TABLE " + LearntWords.TABLE_NAME + " (" +
          LearntWords._ID + " INTEGER PRIMARY KEY," +
          LearntWords.COL_LANG + TEXT_TYPE + COMMA_SEP +
          LearntWords.COL_BASE_WORD + TEXT_TYPE + COMMA_SEP +
          LearntWords.COL_BASE_WORD_LC + TEXT_TYPE + COMMA_SEP +
          LearntWords.COL_LEARNT_WORD + TEXT_TYPE + COMMA_SEP +
          LearntWords.COL_LEARNT_WORD_LC + TEXT_TYPE + COMMA_SEP +
          LearntWords.COL_CLASS_NUM + INTEGER_TYPE +
      " )";

  public static final String SQL_DELETE_ENTRIES =
      "DROP TABLE IF EXISTS " + LearntWords.TABLE_NAME;

  public LearntWordsContract() {
  }

}
