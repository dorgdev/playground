package com.dorgdev.langxercise.utils;

import java.io.Serializable;

public class Word implements Serializable {
  protected String lang;
  protected String baseWord;
  protected String learntWord;
  protected int classNum;

  protected Word(
      String lang,
      String baseWord,
      String learntWord,
      int classNum) {
    this.lang = lang;
    this.baseWord = baseWord;
    this.learntWord = learntWord;
    this.classNum = classNum;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Word)) {
      return false;
    }
    final Word other = (Word)o;
    return
        this.lang.equals(other.lang) &&
            this.baseWord.equals(other.baseWord) &&
            this.learntWord.equals(other.learntWord) &&
            this.classNum == other.classNum;
  }

  public String getBaseWord() {
    return baseWord;
  }

  public String getLearntWord() {
    return learntWord;
  }

  public int getClassNum() {
    return classNum;
  }
}
