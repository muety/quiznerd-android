package com.github.n1try.quiznerd.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizQuestion {
    private String id;
    private String text;
    private String code;
    private QuizCategory category;
    private List<QuizAnswer> answers;

    public QuizAnswer getCorrectAnswer() {
        for (QuizAnswer answer : answers) {
            if (answer.isCorrect()) return answer;
        }
        return null;
    }
}
