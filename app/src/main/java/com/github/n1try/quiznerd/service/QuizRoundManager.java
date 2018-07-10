package com.github.n1try.quiznerd.service;

import com.github.n1try.quiznerd.model.QuizAnswer;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizRound;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.Constants;

public class QuizRoundManager {
    private QuizApiService apiService;
    private QuizMatch match;
    private QuizUser user;
    private QuizRound round;
    private int playerIndex;
    private int questionIndex;

    public QuizRoundManager(QuizMatch match, QuizUser user) {
        this.apiService = QuizApiService.getInstance();
        this.match = match;
        this.user = user;
        this.playerIndex = match.getMyPlayerIndex(user);
    }

    public QuizRoundManager playCurrentRound() {
        round = match.getCurrentRound();
        questionIndex = round.getMyNextQuestionIndex(playerIndex);
        return this;
    }

    public QuizRoundManager answer(QuizAnswer answer) {
        round.answerQuestionAt(questionIndex, answer, match.getMyPlayerIndex(user));
        apiService.matchCache.put(match.getId(), match);
        apiService.updateQuizRounds(match);
        return this;
    }

    public QuizRoundManager timeoutAllPending() {
        while (questionIndex >= 0) {
            answer(QuizAnswer.TIMEOUT_ANSWER);
            next();
        }
        return this;
    }

    public QuizRoundManager next() {
        if (questionIndex < round.getQuestions().size() - 1) {
            questionIndex++;
        } else {
            if (round.getId() < Constants.NUM_ROUNDS && round.getId() < match.getRounds().size() - 1) {
                match.nextRound();
                apiService.updateQuizRound(match);
                round = match.getCurrentRound();
            } else {
                match.setActive(false);
                apiService.updateQuizState(match);
                round = null;
            }
            questionIndex = -1;
            apiService.matchCache.put(match.getId(), match);
        }
        return this;
    }

    public QuizQuestion getCurrentQuestion() {
        return questionIndex < 0 ? null : round.getQuestion(questionIndex);
    }
}
