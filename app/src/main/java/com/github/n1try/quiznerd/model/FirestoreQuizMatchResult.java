package com.github.n1try.quiznerd.model;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class FirestoreQuizMatchResult extends QuizMatch {
    @Builder
    private FirestoreQuizMatchResult(String id, QuizCategory quizCategory, int round, boolean active, Date updated, DocumentReference player1Reference, DocumentReference player2Reference, List<DocumentReference> questionsReference) {
        super(id, quizCategory, round, active, updated);
        this.player1Reference = player1Reference;
        this.player2Reference = player2Reference;
        this.questionsReference = questionsReference;
    }

    private DocumentReference player1Reference;
    private DocumentReference player2Reference;
    private List<DocumentReference> questionsReference;
}
