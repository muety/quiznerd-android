package com.github.n1try.quiznerd.model;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.Data;

@Data
public class QuizMatch {
    private String id;
    private QuizCategory quizCategory;
    private User player1;
    private User player2;
    private int round;
    private ZonedDateTime updated;
    private List<QuizQuestion> questions;
    private List<QuizUserAnswer> answers1;
    private List<QuizUserAnswer> answers2;
}
