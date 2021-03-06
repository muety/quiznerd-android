package com.github.n1try.quiznerd.service;

import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class QuizApiService {
    public static QuizApiService getInstance() {
        // Abstract class and factory both in one
        return FirestoreApiService.getInstance();
    }

    public Map<String, QuizMatch> matchCache = new ConcurrentHashMap<>();

    public Map<String, QuizUser> userCache = new ConcurrentHashMap<>();

    public Map<QuizUser, Date> pokeCache = new ConcurrentHashMap<>();

    public abstract void getUserById(String id, QuizApiCallbacks callback);

    public abstract void getUserByAuthentication(String authentication, QuizApiCallbacks callback);

    public abstract void fetchActiveMatches(String userId, QuizApiCallbacks callback);

    public abstract void fetchPastMatches(String userId, QuizApiCallbacks callback);

    public abstract void fetchRandomQuestions(int n, QuizCategory category, Map<QuizCategory, Integer> categoryCount, QuizApiCallbacks callback);

    public abstract void createMatch(QuizMatch match, QuizApiCallbacks callback);

    public abstract void createUser(QuizUser user, QuizApiCallbacks callback);

    public abstract void updateQuizRound(QuizMatch match);

    public abstract void updateQuizRounds(QuizMatch match);

    public abstract void updateQuizState(QuizMatch match);

    public abstract void deleteMatch(QuizMatch match, QuizApiCallbacks callback);

    public abstract void pokeOpponent(QuizMatch match, QuizUser me);
}
