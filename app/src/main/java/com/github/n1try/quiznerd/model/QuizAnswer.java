package com.github.n1try.quiznerd.model;

import lombok.Data;

@Data
public class QuizAnswer {
    private int id;
    private String text;
    private boolean correct;
}
