package com.dorgdev.langxercise.utils.dict;

import com.dorgdev.langxercise.utils.Dictionary;

public final class KnownRussianDictionary {

  private KnownRussianDictionary(final Dictionary dictionary) { }

  public static void buildContent(Dictionary dictionary) {
    dictionary.clearWords();

    // Level 1
    int level = 1;
    dictionary.addWord("Harry Potter", "Гарри Поттер", level);
    dictionary.addWord("David Hasselhoff", "Давид Хасселхофф", level);
    dictionary.addWord("Dor Gross", "Дор Гросс", level);

    // Level 2
    level = 2;
    dictionary.addWord("He", "Он", level);
    dictionary.addWord("She", "Она", level);
    dictionary.addWord("You", "Ты", level);
    dictionary.addWord("I", "Я", level);

    // Level 3
    level = 3;
    dictionary.addWord("Hi", "Привет", level);
    dictionary.addWord("Hello", "Здравствуйте", level);
    dictionary.addWord("Thank you", "Спасибо", level);
    dictionary.addWord("Goodbye", "До свидания", level);
    dictionary.addWord("I speak a little Russian", "Я могу немного говорить по-русский", level);
    dictionary.addWord("You speak Russian very well", "Ты говорить по-русский очень хорошо", level);
    dictionary.addWord("You are wonderful!", "Ты прекрасный!", level);

    // Level 10
    level = 10;
    dictionary.addWord("I am waiting", "Я жду", level);
  }

}
