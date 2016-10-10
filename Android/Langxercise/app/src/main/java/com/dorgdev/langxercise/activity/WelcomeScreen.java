package com.dorgdev.langxercise.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.dorgdev.langxercise.R;
import com.dorgdev.langxercise.dummy.DummyClickListener;
import com.dorgdev.langxercise.utils.Dictionary;
import com.dorgdev.langxercise.utils.DictionaryManager;
import com.dorgdev.langxercise.utils.dict.DummyRussianDictionary;
import com.dorgdev.langxercise.utils.dict.KnownRussianDictionary;
import com.dorgdev.langxercise.utils.Status;

public class WelcomeScreen extends AppCompatActivity {

  private final Dictionary dictionary =
      DictionaryManager.getInstance().getDictionary(this, "English", "Russian");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_welcome_screen);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });

    if (findViewById(R.id.main_screen_container) != null) {
      if (savedInstanceState != null) {
        return;
      }
      WelcomeScreenFragment welcomeScreenFragment = new WelcomeScreenFragment();
      getSupportFragmentManager().beginTransaction()
          .add(R.id.main_screen_container, welcomeScreenFragment).commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_welcome_screen, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    if (id == R.id.russian_dictionary_shortcut) {
      KnownRussianDictionary.buildContent(dictionary);
    } else  if (id == R.id.dummy_dictionary_shortcut) {
      DummyRussianDictionary.buildContent(dictionary);
    } else if (id == R.id.clear_dictionary_shortcut) {
      recreateDB();
    }

    return super.onOptionsItemSelected(item);
  }

  public void addWordScreen(View view) {
    AddWordFragment addWordFragment = new AddWordFragment();
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.main_screen_container, addWordFragment);
    transaction.addToBackStack(null);
    transaction.commit();
  }

  private Status addWordHelper(View view) {
    final EditText englishField = (EditText) findViewById(R.id.english_text);
    final EditText russianField = (EditText) findViewById(R.id.russian_text);
    final NumberPicker classPicker = (NumberPicker) findViewById(R.id.class_num_picker);

    String english = englishField.getText().toString();
    String russian = russianField.getText().toString();
    int classNum = classPicker.getValue();

    final Status status = dictionary.addWord(english, russian, classNum);
    if (status.isOK()) {
      Snackbar snackBar = Snackbar.make(
          findViewById(R.id.main_screen_container),
          R.string.word_added,
          Snackbar.LENGTH_SHORT);
      snackBar.show();
      return Status.OK;
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder
        .setTitle(status.getErrorCode().getTitle())
        .setMessage(status.getErrorMessage())
        .setPositiveButton("OK", DummyClickListener.getInstance());
    AlertDialog alertDialog = builder.create();
    alertDialog.show();

    return status;
  }

  public void addWord(View view) {
    if (addWordHelper(view).isOK()) {
      getSupportFragmentManager().popBackStack();
    }
  }

  public void addWordAndStay(View view) {
    if (!addWordHelper(view).isOK()) {
      return;
    }
    ((EditText) findViewById(R.id.english_text)).getText().clear();
    ((EditText) findViewById(R.id.russian_text)).getText().clear();
  }

  public void runTest(View view) {
    final Intent intent = new Intent(this, TestSettingsActivity.class);
    intent.putExtra(TestSettingsActivity.LEARNT_LANGUAGE_VALUE, dictionary.getLearntLang());
    startActivity(intent);
  }

  public void viewWords(View view) {
    final Intent intent = new Intent(this, WordsListActivity.class);
    intent.putExtra(WordsListActivity.LEARNT_LANGUAGE_VALUE, dictionary.getLearntLang());
    startActivity(intent);
  }

  public void recreateDB() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder
        .setTitle("Clear DB?")
        .setMessage("Are you sure you want to clear the DB from all its words?")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dictionary.clearWords();
          }
        })
        .setNegativeButton("Cancel", DummyClickListener.getInstance());
    AlertDialog alertDialog = builder.create();
    alertDialog.show();

  }
}
