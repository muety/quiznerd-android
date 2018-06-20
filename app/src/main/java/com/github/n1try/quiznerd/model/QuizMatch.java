package com.github.n1try.quiznerd.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizMatch {
    private String id;
    private QuizCategory quizCategory;
    private QuizUser player1;
    private QuizUser player2;
    private int round;
    private boolean active;
    private Date updated;
    private List<QuizQuestion> questions = new ArrayList<>();
    private List<Long> answers1 = new ArrayList<>();
    private List<Long> answers2 = new ArrayList<>();

    public QuizMatch(String id, QuizCategory quizCategory, int round, boolean active, List<Long> answers1, List<Long> answers2, Date updated) {
        this.id = id;
        this.quizCategory = quizCategory;
        this.round = round;
        this.active = active;
        this.updated = updated;
        this.answers1 = answers1;
        this.answers2 = answers2;
    }

    public QuizUser getOpponent(QuizUser me) {
        if (getPlayer1().getAuthentication().equals(me.getAuthentication())) return getPlayer2();
        return getPlayer1();
    }

    public boolean isInitiator(QuizUser user) {
        return getPlayer1().getAuthentication().equals(user.getAuthentication());
    }

    public boolean isMyTurn(QuizUser me) {
        if (isInitiator(me) && getRound() % 2 == 0) return true;
        return false;
    }

    public int[] getScores() {
        int[] scores = new int[] {0, 0};
        for (int i = 0; i < questions.size(); i++) {
            int correctAnswer = questions.get(i).getCorrectAnswer().getId();
            int user1Answer = answers1.size() > i ? answers1.get(i).intValue() : -1;
            int user2Answer = answers2.size() > i ? answers2.get(i).intValue() : -1;
            if (correctAnswer == user1Answer) scores[0]++;
            if (correctAnswer == user2Answer) scores[1]++;
        }
        return scores;
    }
}
