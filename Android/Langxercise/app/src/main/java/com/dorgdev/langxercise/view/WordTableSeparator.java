package com.dorgdev.langxercise.view;

import android.content.Context;
import android.view.View;
import android.widget.TableRow;

import com.dorgdev.langxercise.R;

/**
 * Created by dor on 7/18/16.
 */
public class WordTableSeparator extends TableRow {

  public WordTableSeparator(final Context context, final int columnsCount) {
    super(context);

    for (int i = 0; i < columnsCount; ++i) {
      View v = new View(context);
      v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 2));
      v.setBackgroundResource(R.color.table_separator);
      addView(v);
    }
  }
}
