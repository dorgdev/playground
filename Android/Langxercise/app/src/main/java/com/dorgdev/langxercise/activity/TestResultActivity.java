package com.dorgdev.langxercise.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.dorgdev.langxercise.R;
import com.dorgdev.langxercise.utils.Word;
import com.dorgdev.langxercise.view.WordsTable;

import java.util.ArrayList;

public class TestResultActivity extends Activity {

  public static final String CORRECT_WORDS =
      "com.dorgdev.langxercise.TestResultActivity.CORRECT_WORDS";
  public static final String WRONG_WORDS =
      "com.dorgdev.langxercise.TestResultActivity.WRONG_WORDS";

  private WordsTable correctWordsTable;
  private WordsTable wrongWordsTable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_result);

    final ArrayList<Word> correctWords = (ArrayList<Word>) getIntent().getSerializableExtra(CORRECT_WORDS);
    correctWordsTable = buildTable(R.id.correct_answers_test_result_table, correctWords);
    final ArrayList<Word> wrongWords = (ArrayList<Word>) getIntent().getSerializableExtra(WRONG_WORDS);
    wrongWordsTable = buildTable(R.id.wrong_answers_test_result_table, wrongWords);

    final int correctCount = correctWords.size();
    final int wrongCount = wrongWords.size();
    final int totalCount = correctCount + wrongCount;

    final String testResultTitle = "Test Results: " + correctCount + "/" + totalCount;
    ((TextView)findViewById(R.id.test_result_summary_text)).setText(testResultTitle);
  }

  private WordsTable buildTable(int resource, ArrayList<Word> words) {
    final TableLayout tableLayout = (TableLayout) findViewById(resource);
    WordsTable table = new WordsTable(this, null, tableLayout);
    table.setIsMutable(false);
    table.buildTableContent(words);

    return table;
  }

  public void backToMainMenu(View view) {
    onBackPressed();
    finish();
  }
}
