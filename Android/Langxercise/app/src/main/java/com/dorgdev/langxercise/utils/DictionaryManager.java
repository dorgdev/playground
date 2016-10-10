package com.dorgdev.langxercise.utils;

import android.app.Activity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dor on 7/17/16.
 */
public class DictionaryManager implements Serializable {

  private static DictionaryManager instance = new DictionaryManager();

  private Map<String, Dictionary> dictionaries;

  public static DictionaryManager getInstance() {
    return instance;
  }

  public Dictionary getDictionaryAssumeExisting(String learntLang) {
    if (dictionaries.containsKey(learntLang)) {
      return dictionaries.get(learntLang);
    }
    throw new IllegalArgumentException(
        "Dictionary for specified language (" + learntLang + ") does not exist!");
  }

  public Dictionary getDictionary(Activity activity, String baseLang, String learntLang) {
    if (!dictionaries.containsKey(learntLang)) {
      dictionaries.put(learntLang, new Dictionary(activity, baseLang, learntLang));
    }
    return dictionaries.get(learntLang);
  }

  private DictionaryManager() {
    dictionaries = new HashMap<>();
  }

}
