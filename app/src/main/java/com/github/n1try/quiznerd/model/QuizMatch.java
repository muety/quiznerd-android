package com.github.n1try.quiznerd.model;

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
    private List<QuizQuestion> questions;
    private List<QuizUserAnswer> answers1;
    private List<QuizUserAnswer> answers2;
}
