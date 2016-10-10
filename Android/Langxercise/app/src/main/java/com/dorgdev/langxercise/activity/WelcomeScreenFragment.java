package com.dorgdev.langxercise.activity;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dorgdev.langxercise.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class WelcomeScreenFragment extends Fragment {

  public WelcomeScreenFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_welcome_screen, container, false);
  }
}
