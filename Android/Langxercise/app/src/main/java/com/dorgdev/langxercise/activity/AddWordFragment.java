package com.dorgdev.langxercise.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.dorgdev.langxercise.R;


public class AddWordFragment extends Fragment {

  public AddWordFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_add_word, container, false);
  }

  @Override
  public void onStart() {
    super.onStart();

    NumberPicker picker = (NumberPicker) getActivity().findViewById(R.id.class_num_picker);
    picker.setMaxValue(20);
    picker.setMinValue(1);
    picker.setValue(1);
  }
}
