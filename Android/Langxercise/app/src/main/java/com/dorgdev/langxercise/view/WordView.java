package com.dorgdev.langxercise.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;

import com.dorgdev.langxercise.R;
import com.dorgdev.langxercise.dummy.DummyClickListener;
import com.dorgdev.langxercise.utils.Word;

/**
 * Created by dor on 7/18/16.
 */
public class WordView extends TableRow {

  private Word word;
  private WordUpdateListener listener;
  private int columnCount;

  public interface WordUpdateListener {
    public void WordDeleted(Word word);
  }

  public WordView(
      final Context context,
      final WordUpdateListener listener,
      final Word word,
      final boolean isMutable,
      final int position) {
    super(context);

    this.listener = listener;
    this.word = word;
    this.columnCount = 0;

    final int bgResource = position % 2 == 0 ? R.color.table_even_bg : R.color.table_odd_bg;
    setBackgroundResource(bgResource);

    final TextView learntWord = new TextView(context);
    learntWord.setPadding(2, 2, 2, 2);
    learntWord.setGravity(Gravity.CENTER);
    learntWord.setTextAlignment(TEXT_ALIGNMENT_CENTER);
    learntWord.setText(word.getLearntWord());
    addView(learntWord, new TableRow.LayoutParams(
        0, ViewGroup.LayoutParams.MATCH_PARENT, 3.0f));
    columnCount++;

    final TextView baseWord = new TextView(context);
    baseWord.setPadding(2, 2, 2, 2);
    baseWord.setGravity(Gravity.CENTER);
    baseWord.setTextAlignment(TEXT_ALIGNMENT_CENTER);
    baseWord.setText(word.getBaseWord());
    addView(baseWord, new TableRow.LayoutParams(
        0, ViewGroup.LayoutParams.MATCH_PARENT, 3.0f));
    columnCount++;

    final TextView classNum = new TextView(context);
    classNum.setPadding(2, 2, 2, 2);
    classNum.setGravity(Gravity.CENTER);
    classNum.setText(String.valueOf(word.getClassNum()));
    addView(classNum, new TableRow.LayoutParams(
        0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
    columnCount++;

    if (isMutable) {
      final ImageButton deleteButton = new ImageButton(context);
      deleteButton.setImageResource(R.drawable.delete_icon);
      deleteButton.setPadding(2, 2, 2, 2);
      deleteButton.setBackgroundResource(bgResource);
      addView(deleteButton, new TableRow.LayoutParams(
          0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
      columnCount++;
      deleteButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          AlertDialog.Builder builder = new AlertDialog.Builder(context);
          builder
              .setTitle("Delete Word?")
              .setMessage("Are you sure you want to delete this word?")
              .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  listener.WordDeleted(word);
                }
              })
              .setNegativeButton("Cancel", DummyClickListener.getInstance());
          AlertDialog alertDialog = builder.create();
          alertDialog.show();

        }
      });
    }
  }

  public int getColumnsCount() {
    return columnCount;
  }
}
