package com.github.n1try.quiznerd.model;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class FirestoreQuizMatchResult extends QuizMatch {
    private transient DocumentReference player1Reference;
    private transient DocumentReference player2Reference;

    @Builder
    private FirestoreQuizMatchResult(String id, QuizCategory quizCategory, int round, boolean active, List<Long> answers1, List<Long> answers2, Date updated, DocumentReference player1Reference, DocumentReference player2Reference, List<QuizQuestion> questions) {
        super(id, quizCategory, round, active, answers1, answers2, updated, questions);
        this.player1Reference = player1Reference;
        this.player2Reference = player2Reference;
    }
}
