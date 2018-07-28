package com.github.n1try.quiznerd.utils;

import com.github.n1try.quiznerd.model.QuizCategory;
import com.google.common.collect.ImmutableMap;

public final class Constants {
    public static final int NUM_ROUNDS = 4;
    public static final int NUM_QUESTIONS_PER_ROUND = 3;
    public static final int INITIAL_SPLASH_SECS = 3;

    public static final String KEY_PREFERENCES = "prefs.prefs";
    public static final String KEY_ME = "me";
    public static final String KEY_MATCH_ID = "match_id";
    public static final String KEY_QUESTION = "question";
    public static final String KEY_POSITION = "position";
    public static final String KEY_COUNTDOWN = "countdown";
    public static final String KEY_RANDOM_QUESTIONS = "random_questions";
    public static final String KEY_INITIALIZED = "initialized";

    // TODO: Fetch actual count values from database
    public static final ImmutableMap CATEGORY_QUESTION_COUNT = new ImmutableMap.Builder()
            .put(QuizCategory.PYTHON, 108)
            .put(QuizCategory.SWIFT, 131)
            .put(QuizCategory.JAVA, 102)
            .put(QuizCategory.PHP, 91)
            .put(QuizCategory.JS, 62)
            .put(QuizCategory.ANDROID, 44)
            .put(QuizCategory.CPP, 110)
            .put(QuizCategory.HTML, 97)
            .put(QuizCategory.CSHARP, 106)
            .build();
}
