package com.github.n1try.quiznerd.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizUser {
    private String id;
    private String displayName;
    private String email;
}
