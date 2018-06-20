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
    private String question;
    private String code;
    private QuizCategory category;
    private String creatorId;
    private List<QuizAnswer> answers;
}
