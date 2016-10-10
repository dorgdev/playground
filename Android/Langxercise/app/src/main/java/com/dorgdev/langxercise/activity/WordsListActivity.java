package com.dorgdev.langxercise.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TableLayout;

import com.dorgdev.langxercise.R;
import com.dorgdev.langxercise.utils.Dictionary;
import com.dorgdev.langxercise.utils.DictionaryManager;
import com.dorgdev.langxercise.utils.Word;
import com.dorgdev.langxercise.view.WordView;
import com.dorgdev.langxercise.view.WordsTable;

public class WordsListActivity extends Activity implements WordView.WordUpdateListener {

  public static final String LEARNT_LANGUAGE_VALUE =
      "com.dorgdev.langxercise.WrodsListActivity.LEARNT_LANGUAGE";

  private String learntLang;
  private Dictionary dictionary;
  private WordsTable table;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_words_list);

    final Intent intent = getIntent();
    learntLang = intent.getStringExtra(LEARNT_LANGUAGE_VALUE);
    dictionary = DictionaryManager.getInstance().getDictionaryAssumeExisting(learntLang);

    table = new WordsTable(this, this, (TableLayout) findViewById(R.id.words_list_view));
    table.buildTableContent(dictionary.getAllWords(null, null));
  }

  @Override
  public void WordDeleted(Word word) {
    dictionary.deleteWord(word.getBaseWord(), word.getLearntWord());
    table.clear();
    table.buildTableContent(dictionary.getAllWords(null, null));
  }
}
