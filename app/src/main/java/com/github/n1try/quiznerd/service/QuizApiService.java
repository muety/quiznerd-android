package com.github.n1try.quiznerd.service;

import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class QuizApiService {
    public static QuizApiService getInstance() {
        // Abstract class and factory both in one
        return FirestoreApiService.getInstance();
    }

    public Map<String, QuizMatch> matchCache = new ConcurrentHashMap<>();

    public Map<String, QuizUser> userCache = new ConcurrentHashMap<>();

    public abstract void fetchUserByNickname(String nickname, QuizApiCallbacks callback);

    public abstract void fetchUserById(String authentication, QuizApiCallbacks callback);

    public abstract void fetchActiveMatches(String userId, QuizApiCallbacks callback);

    public abstract void fetchPastMatches(String userId, QuizApiCallbacks callback);

    public abstract void fetchRandomQuestions(int n, QuizCategory category, QuizApiCallbacks callback);

    public abstract void createMatch(QuizMatch match, QuizApiCallbacks callback);

    public abstract void updateQuizRound(QuizMatch match);

    public abstract void updateQuizRounds(QuizMatch match);

    public abstract void updateQuizState(QuizMatch match);
}
