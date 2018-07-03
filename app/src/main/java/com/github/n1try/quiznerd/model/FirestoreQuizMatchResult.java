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
    private FirestoreQuizMatchResult(String id, QuizCategory quizCategory, int round, boolean active, List<QuizRound> rounds, Date updated, DocumentReference player1Reference, DocumentReference player2Reference) {
        super(id, quizCategory, round, active, updated, rounds);
        this.player1Reference = player1Reference;
        this.player2Reference = player2Reference;
    }
}
