package com.dorgdev.langxercise.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dorgdev.langxercise.db.LearntWordsContract;
import com.dorgdev.langxercise.db.LearntWordsDBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dictionary {

  public static final String DUP_SEARCH_QUERY =
      "SELECT " + LearntWordsContract.LearntWords.COL_BASE_WORD_LC +
            "," + LearntWordsContract.LearntWords.COL_LEARNT_WORD_LC +
       " FROM " + LearntWordsContract.LearntWords.TABLE_NAME +
      " WHERE " + LearntWordsContract.LearntWords.COL_BASE_WORD_LC + "= ?" +
         " OR " + LearntWordsContract.LearntWords.COL_LEARNT_WORD_LC + "= ?";

  public static final String WARD_RECORD_SELECTION_CLAUSE =
      LearntWordsContract.LearntWords.COL_BASE_WORD_LC + " = ? AND " +
      LearntWordsContract.LearntWords.COL_LEARNT_WORD_LC + " = ?";

  private final LearntWordsDBHelper dbHelper;
  private final String baseLang;
  private final String learntLang;
  private Map<String, Word> learntWords = null;
  private Map<String, Word> baseWords = null;
  private List<Word> wordsList = null;

  public Dictionary(Activity activity, String baseLang, String learntLang) {
    this.dbHelper = new LearntWordsDBHelper(activity);
    this.baseLang = baseLang;
    this.learntLang = learntLang;
  }

  public String getLearntLang() {
    return learntLang;
  }

  public void clearWords() {
    dbHelper.recreateTable();
    learntWords = null;
    baseWords = null;
    wordsList = null;
  }

  private List<Word> filterWordsByClass(Integer minClassNum, Integer maxClassNum) {
    if (wordsList == null || wordsList.isEmpty()) {
      return new ArrayList<>();
    }
    if (wordsList.get(0).getClassNum() >= minClassNum &&
        wordsList.get(wordsList.size() - 1).getClassNum() <= maxClassNum) {
      return wordsList;
    }
    int first = Integer.MAX_VALUE;
    for (int i = 0; i < wordsList.size(); ++i) {
      if (wordsList.get(i).getClassNum() >= minClassNum) {
        first = i;
        break;
      }
    }
    int last = Integer.MIN_VALUE;
    for (int i = wordsList.size() - 1; i >= 0; --i) {
      if (wordsList.get(i).getClassNum() <= maxClassNum) {
        last = i;
        break;
      }
    }
    if (first > last) {
      return new ArrayList<>(0);
    }
    return wordsList.subList(first, last + 1);
  }

  private void buildCache() {
    final SQLiteDatabase db = dbHelper.getReadableDatabase();

    String where =
        LearntWordsContract.LearntWords.COL_LANG + " = ? AND " +
            LearntWordsContract.LearntWords.COL_CLASS_NUM + " >= ? AND " +
            LearntWordsContract.LearntWords.COL_CLASS_NUM + " <= ?";

    String[] whereArgs = new String[] {
        learntLang, "0", String.valueOf(Integer.MAX_VALUE) };

    Cursor cursor = db.query(
        LearntWordsContract.LearntWords.TABLE_NAME,
        new String[]{
            LearntWordsContract.LearntWords.COL_BASE_WORD,
            LearntWordsContract.LearntWords.COL_LEARNT_WORD,
            LearntWordsContract.LearntWords.COL_CLASS_NUM},
        where,
        whereArgs,
        null,
        null,
        LearntWordsContract.LearntWords.COL_CLASS_NUM + "," +
            LearntWordsContract.LearntWords._ID);

    final int count = cursor.getCount();
    if (count == 0) {
      wordsList = new ArrayList<>();
      baseWords = new HashMap<>();
      learntWords = new HashMap<>();
      return;
    }
    wordsList = new ArrayList<>(count);
    cursor.moveToFirst();
    final int baseIndex = cursor.getColumnIndex(LearntWordsContract.LearntWords.COL_BASE_WORD);
    final int learntIndex = cursor.getColumnIndex(LearntWordsContract.LearntWords.COL_LEARNT_WORD);
    final int classNumIndex = cursor.getColumnIndex(LearntWordsContract.LearntWords.COL_CLASS_NUM);

    do {
      final String baseWord = cursor.getString(baseIndex);
      final String learntWord = cursor.getString(learntIndex);
      final int classNumValue = cursor.getInt(classNumIndex);

      final Word word = new Word(learntLang, baseWord, learntWord, classNumValue);
      wordsList.add(word);
    } while (cursor.moveToNext());
    cursor.close();

    baseWords = new HashMap<>(count);
    learntWords = new HashMap<>(count);

    for (final Word word : wordsList) {
      learntWords.put(word.getLearntWord().toLowerCase(), word);
      baseWords.put(word.getBaseWord().toLowerCase(), word);
    }
  }

  public List<Word> getAllWords(Integer minClassNum, Integer maxClassNum) {
    minClassNum = minClassNum != null ? minClassNum : 0;
    maxClassNum = maxClassNum != null ? maxClassNum : Integer.MAX_VALUE;
    if (wordsList == null) {
      buildCache();
    }
    return filterWordsByClass(minClassNum, maxClassNum);
  }

  private void addWordToWordsList(Word word) {
    int lastIndex = -1;
    for (int i = 0; i < wordsList.size(); ++i) {
      if (wordsList.get(i).getClassNum() <= word.getClassNum()) {
        lastIndex = i;
      } else {
        break;
      }
    }
    wordsList.add(lastIndex + 1, word);
  }

  public Status addWord(final String baseWord, final String learntWord, final int classNum) {
    if (wordsList == null) {
      buildCache();
    }

    if (baseWord.isEmpty()) {
      return new Status(
          Status.ErrorCode.BASE_LANG_EMPTY,
          "Found an empty word (" + baseLang + ").");
    } else if (learntWord.isEmpty()) {
      return new Status(
          Status.ErrorCode.LEARNT_LANG_EMPTY,
          "Found an empty word (" + learntLang + ").");
    }
    final Status status = validateNotDup(baseWord, learntWord);
    if (!status.isOK()) {
      return status;
    }

    ContentValues values = new ContentValues();
    values.put(LearntWordsContract.LearntWords.COL_LANG, learntLang);
    values.put(LearntWordsContract.LearntWords.COL_BASE_WORD, baseWord);
    values.put(LearntWordsContract.LearntWords.COL_BASE_WORD_LC, baseWord.toLowerCase());
    values.put(LearntWordsContract.LearntWords.COL_LEARNT_WORD, learntWord);
    values.put(LearntWordsContract.LearntWords.COL_LEARNT_WORD_LC, learntWord.toLowerCase());
    values.put(LearntWordsContract.LearntWords.COL_CLASS_NUM, classNum);

    // Insert the new row, returning the primary key value of the new row
    long newRowId;
    newRowId = dbHelper.getWritableDatabase().insert(
        LearntWordsContract.LearntWords.TABLE_NAME, null, values);

    if (newRowId < 0) {
      return new Status(Status.ErrorCode.UNKNOWN_DB_ERROR, "Unknown DB Error");
    }

    final Word newWord = new Word(learntLang, baseWord, learntWord, classNum);
    baseWords.put(baseWord.toLowerCase(), newWord);
    learntWords.put(learntWord.toLowerCase(), newWord);
    addWordToWordsList(newWord);

    return Status.OK;
  }

  private Status validateNotDup(final String baseWord, final String learntWord) {
    if (baseWords.containsKey(baseWord.toLowerCase())) {
      return new Status(
          Status.ErrorCode.BASE_LANG_DUP,
          "Word already exists (" + baseLang + ").");
    }
    if (learntWords.containsKey(learntWord.toLowerCase())) {
      return new Status(
          Status.ErrorCode.LEARNT_LANG_DUP,
          "Word already exists (" + learntLang + ").");
    }
    return Status.OK;
  }

  public Status deleteWord(String baseWord, String learntWord) {
    final SQLiteDatabase db = dbHelper.getReadableDatabase();

    final int deletedCount = db.delete(
        LearntWordsContract.LearntWords.TABLE_NAME,
        WARD_RECORD_SELECTION_CLAUSE,
        new String[]{baseWord.toLowerCase(), learntWord.toLowerCase()});

    if (deletedCount == 1) {
      learntWords.remove(learntWord.toLowerCase());
      baseWords.remove(baseWord.toLowerCase());
      for (int i = 0; i < wordsList.size(); ++i) {
        if (wordsList.get(i).getLearntWord().equalsIgnoreCase(learntWord)) {
          wordsList.remove(i);
          break;
        }
      }
      return Status.OK;
    }
    return new Status(
        Status.ErrorCode.UNKNOWN_DB_ERROR,
        "Failed deleting record, unknown error occurred.");
  }

  public Status updateClassNum(String baseWord, String learntWord, int newClassNum) {
    final Status status = deleteWord(baseWord, learntWord);
    if (!status.isOK()) {
      return status;
    }
    return addWord(baseWord, learntWord, newClassNum);
  }

}
