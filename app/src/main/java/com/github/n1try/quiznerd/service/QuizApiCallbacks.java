package com.github.n1try.quiznerd.service;

import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;

import java.util.List;

public interface QuizApiCallbacks {
    void onMatchesFetched(List<QuizMatch> matches);

    void onUsersFetched(List<QuizUser> users);

    void onError(Exception e);
}
