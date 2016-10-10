package com.dorgdev.langxercise.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.dorgdev.langxercise.R;
import com.dorgdev.langxercise.utils.Dictionary;
import com.dorgdev.langxercise.utils.DictionaryManager;
import com.dorgdev.langxercise.utils.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestActivity extends Activity {

  public static final String LEARNT_LANGUAGE_VALUE =
      "com.dorgdev.langxercise.TestActivity.LEARNT_LANGUAGE_VALUE";
  public static final String MIN_CLASS_NUM =
      "com.dorgdev.langxercise.TestActivity.MIN_CLASS_NUM";
  public static final String MAX_CLASS_NUM =
      "com.dorgdev.langxercise.TestActivity.MAX_CLASS_NUM";
  public static final String MAX_NUM_QUESTIONS =
      "com.dorgdev.langxercise.TestActivity.MAX_NUM_QUESTIONS";

  private List<Word> words;
  private int testedWordIndex = -1;
  private int maxNumQuestionLeft;
  private int correctAnswers = 0;
  private int totalQuestions = 0;

  private ArrayList<Word> correctWords;
  private ArrayList<Word> wrongWords;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);

    final Intent intent = getIntent();
    final String learntLang = intent.getExtras().getString(LEARNT_LANGUAGE_VALUE);
    final int minClassNum = intent.getExtras().getInt(MIN_CLASS_NUM);
    final int maxClassNum = intent.getExtras().getInt(MAX_CLASS_NUM);
    maxNumQuestionLeft = intent.getExtras().getInt(MAX_NUM_QUESTIONS);

    final Dictionary dict =
        DictionaryManager.getInstance().getDictionaryAssumeExisting(learntLang);

    List<Word> dictWords = dict.getAllWords(minClassNum, maxClassNum);

    words = new ArrayList<>(dictWords);
    Collections.shuffle(words);

    correctWords = new ArrayList<>(words.size());
    wrongWords = new ArrayList<>(words.size());

    nextWord();
  }

  private void nextWord() {
    testedWordIndex++;
    maxNumQuestionLeft--;
    if (testedWordIndex >= words.size() || maxNumQuestionLeft < 0) {
      final Intent intent = new Intent(this, TestResultActivity.class);
      intent.putExtra(TestResultActivity.CORRECT_WORDS, correctWords);
      intent.putExtra(TestResultActivity.WRONG_WORDS, wrongWords);
      startActivity(intent);
      finish();
      return;
    }
    final TextView base = (TextView) findViewById(R.id.test_base_word);
    final TextView input = (TextView) findViewById(R.id.test_input_word);
    final Word word = words.get(testedWordIndex);

    base.setText(word.getBaseWord());
    input.setText("");
    input.requestFocus();
  }

  private void displayResult(boolean success, Word word) {
    final View mainView = findViewById(R.id.test_main_layout);
    final String message =
        success ? "Correct!" : "Wrong. Answer was: " + word.getLearntWord();
    Snackbar snackBar = Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT);
    snackBar.show();

    final int origBgColor = mainView.getSolidColor();
    final int newColor = success ? R.color.bg_correct_answer : R.color.bg_wrong_answer;
    mainView.setBackgroundResource(success ? R.color.bg_correct_answer : R.color.bg_wrong_answer);
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        final ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(
            mainView,
            "backgroundColor",
            new ArgbEvaluator(),
            newColor,
            origBgColor);
        backgroundColorAnimator.setDuration(200);
        backgroundColorAnimator.start();
      }
    }, 1000);
  }

  public void checkTestInput(View view) {
    final TextView input = (TextView) findViewById(R.id.test_input_word);
    final Word word = words.get(testedWordIndex);

    final boolean result = equalWords(word.getLearntWord(), input.getText().toString());
    if (result) {
      correctAnswers++;
      correctWords.add(word);
    } else {
      wrongWords.add(word);
    }
    totalQuestions++;
    displayResult(result, word);
    nextWord();
  }

  private boolean equalWords(final String word1, final String word2) {
    int i1 = -1;
    int i2 = -1;
    final int len1 = word1.length();
    final int len2 = word2.length();
    while (i1 < len1 || i2 < len2) {
      i1 = findNextMeaningful(word1, i1 + 1);
      i2 = findNextMeaningful(word2, i2 + 1);
      if ((i1 == len1) != (i2 == len2)) {
        char c = (i1 == len1) ? word2.charAt(i2) : word1.charAt(i1);
        if (!isSpaceOrNewline(c)) {
          return false;
        }
      } else if (i1 == len1) {
        return true;
      }
      char c1 = word1.charAt(i1);
      char c2 = word2.charAt(i2);
      if (!(isSpaceOrNewline(c1) && isSpaceOrNewline(c2)) &&
          !String.valueOf(c1).equalsIgnoreCase(String.valueOf(c2))) {
        return false;
      }
    }
    return true;
  }

  private int findNextMeaningful(final String word, int index) {
    if (index >= word.length()) {
      return word.length();
    }
    while (index < word.length()) {
      char c = word.charAt(index);
      if (Character.isAlphabetic(c)) {
        return index;
      }
      if (isSpaceOrNewline(c)) {
        int j = index + 1;
        while (j < word.length()) {
          char cj = word.charAt(j);
          if (isSpaceOrNewline(cj)) {
            index = j;
          } else if (Character.isAlphabetic(cj)) {
            return index;
          }
          j++;
        }
      }
      index++;
    }
    return index;
  }

  private boolean isSpaceOrNewline(final char c) {
    return c == '\n' || Character.isSpaceChar(c);
  }
}
