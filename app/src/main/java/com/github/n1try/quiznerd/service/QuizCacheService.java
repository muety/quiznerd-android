package com.github.n1try.quiznerd.service;

import com.github.n1try.quiznerd.model.QuizMatch;

import java.util.HashMap;
import java.util.Map;

public class QuizCacheService {
    private static final QuizCacheService ourInstance = new QuizCacheService();
    public Map<String, QuizMatch> matchCache = new HashMap<>();

    public static QuizCacheService getInstance() {
        return ourInstance;
    }

    private QuizCacheService() {
    }
}
