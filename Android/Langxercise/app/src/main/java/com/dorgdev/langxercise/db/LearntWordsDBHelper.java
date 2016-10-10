package com.dorgdev.langxercise.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dor on 7/17/16.
 */
public class LearntWordsDBHelper extends SQLiteOpenHelper {
  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "LearntWords.db";

  public LearntWordsDBHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public void onCreate(SQLiteDatabase db) {
    db.execSQL(LearntWordsContract.SQL_CREATE_ENTRIES);
  }
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // Do nothing for now.
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  public void recreateTable() {
    SQLiteDatabase db = getWritableDatabase();
    db.execSQL(LearntWordsContract.SQL_DELETE_ENTRIES);
    onCreate(db);
  }
}
