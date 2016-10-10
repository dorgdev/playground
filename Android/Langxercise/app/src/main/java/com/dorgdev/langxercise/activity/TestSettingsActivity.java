package com.dorgdev.langxercise.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.dorgdev.langxercise.R;
import com.dorgdev.langxercise.utils.Dictionary;
import com.dorgdev.langxercise.utils.DictionaryManager;
import com.dorgdev.langxercise.utils.Word;

import java.util.List;

public class TestSettingsActivity extends Activity {

  public static final String LEARNT_LANGUAGE_VALUE =
      "com.dorgdev.langxercise.TestSettingsActivity.LEARNT_LANGUAGE_VALUE";

  private Dictionary dictionary;
  private String learntLang;
  private NumberPicker minClassNumPicker;
  private NumberPicker maxClassNumPicker;
  private Spinner numQuestionsSpinner;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_settings);

    learntLang = getIntent().getExtras().getString(LEARNT_LANGUAGE_VALUE);
    dictionary = DictionaryManager.getInstance().getDictionaryAssumeExisting(learntLang);
    List<Word> words = dictionary.getAllWords(null, null);
    int minClassNum = Integer.MAX_VALUE;
    int maxClassNum = Integer.MIN_VALUE;
    for (Word word : words) {
      final int classNum = word.getClassNum();
      if (classNum < minClassNum) {
        minClassNum = classNum;
      }
      if (classNum > maxClassNum) {
        maxClassNum = classNum;
      }
    }
    maxClassNumPicker = (NumberPicker) findViewById(R.id.test_settings_max_class_num);
    minClassNumPicker = (NumberPicker) findViewById(R.id.test_settings_min_class_num);

    minClassNumPicker.setMinValue(minClassNum);
    minClassNumPicker.setMaxValue(maxClassNum);
    minClassNumPicker.setValue(minClassNum);
    minClassNumPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
      @Override
      public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (newVal > maxClassNumPicker.getValue()) {
          minClassNumPicker.setValue(oldVal);
        }
      }
    });

    maxClassNumPicker.setMinValue(minClassNum);
    maxClassNumPicker.setMaxValue(maxClassNum);
    maxClassNumPicker.setValue(maxClassNum);
    maxClassNumPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
      @Override
      public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (newVal < minClassNumPicker.getValue()) {
          maxClassNumPicker.setValue(oldVal);
        }
      }
    });

    numQuestionsSpinner = (Spinner) findViewById(R.id.test_settings_num_questions);
    final ArrayAdapter<CharSequence> choices = ArrayAdapter.createFromResource(
        this, R.array.num_questions_options, R.layout.num_questions_item);
    numQuestionsSpinner.setAdapter(choices);
  }

  public void startTest(View view) {
    final Intent intent = new Intent(this, TestActivity.class);
    intent.putExtra(TestActivity.LEARNT_LANGUAGE_VALUE, learntLang);
    intent.putExtra(TestActivity.MIN_CLASS_NUM, minClassNumPicker.getValue());
    intent.putExtra(TestActivity.MAX_CLASS_NUM, maxClassNumPicker.getValue());
    intent.putExtra(TestActivity.MAX_NUM_QUESTIONS,
        Integer.parseInt(numQuestionsSpinner.getSelectedItem().toString()));
    startActivity(intent);
    finish();
  }
}
