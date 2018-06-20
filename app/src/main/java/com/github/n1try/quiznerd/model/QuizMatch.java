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
    private User player1;
    private User player2;
    private int round;
    private boolean active;
    private Date updated;
    private List<QuizQuestion> questions = new ArrayList<>();
    private List<QuizUserAnswer> answers1 = new ArrayList<>();
    private List<QuizUserAnswer> answers2 = new ArrayList<>();

    public QuizMatch(String id, QuizCategory quizCategory, int round, boolean active, Date updated) {
        this.id = id;
        this.quizCategory = quizCategory;
        this.round = round;
        this.active = active;
        this.updated = updated;
    }
}
