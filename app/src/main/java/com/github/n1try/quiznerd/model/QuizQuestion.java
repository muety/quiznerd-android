package com.github.n1try.quiznerd.model;

import java.util.List;

import lombok.Data;

@Data
public class QuizQuestion {
    private String id;
    private String question;
    private String code;
    private QuizQuestionType type;
    private QuizCategory quizCategory;
    private User creator;
    private List<QuizAnswer> answers;
}
