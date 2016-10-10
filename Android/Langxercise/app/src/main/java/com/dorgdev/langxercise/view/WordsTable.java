package com.dorgdev.langxercise.view;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.dorgdev.langxercise.utils.Word;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dor on 7/30/16.
 */
public class WordsTable {

  private Context context;
  private WordView.WordUpdateListener wordUpdateListener;
  private TableLayout table;

  private boolean isMutable = true;

  public WordsTable(
      final Context context,
      final WordView.WordUpdateListener wordUpdateListener,
      final TableLayout table) {
    this.context = context;
    this.wordUpdateListener = wordUpdateListener;
    this.table = table;
    setEmptyContent();
  }

  public void setIsMutable(boolean isMutable ) {
    this.isMutable = isMutable ;
  }

  public void clear() {
    table.removeAllViews();
    setEmptyContent();
  }

  public void buildTableContent(final List<Word> words) {
    if (words.size() == 0) {
      clear();
      return;
    }
    table.removeAllViews();
    int i = 0;
    int columnsCount = 1;
    List<WordView> wordViews = new ArrayList<>(words.size());
    for (Word word : words) {
      final WordView wordView = new WordView(context, wordUpdateListener, word, isMutable , i++);
      wordViews.add(wordView);
      columnsCount = Math.max(columnsCount, wordView.getColumnsCount());
    }
    for (final WordView wordView : wordViews) {
      table.addView(new WordTableSeparator(context, columnsCount));
      table.addView(wordView);
    }
    if (i > 0) {
      table.addView(new WordTableSeparator(context, columnsCount));
    }
  }

  private void setEmptyContent() {
    table.addView(new WordTableSeparator(context, 1));

    final TableRow row = new TableRow(context);
    final TextView empty = new TextView(context);
    empty.setPadding(2, 2, 2, 2);
    empty.setGravity(Gravity.CENTER);
    empty.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
    empty.setText("- None -");
    row.addView(empty, new TableRow.LayoutParams(
        0, ViewGroup.LayoutParams.MATCH_PARENT, 3.0f));
    table.addView(row);

    table.addView(new WordTableSeparator(context, 1));
  }
}
