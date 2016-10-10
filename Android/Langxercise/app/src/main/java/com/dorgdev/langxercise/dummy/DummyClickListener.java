package com.dorgdev.langxercise.dummy;

import android.content.DialogInterface;

/**
 * Created by dor on 7/19/16.
 */
public class DummyClickListener implements DialogInterface.OnClickListener {

  private static DummyClickListener instance = new DummyClickListener();

  public static DummyClickListener getInstance() {
    return instance;
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    // Nothing.
  }
}
