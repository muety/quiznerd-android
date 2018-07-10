package com.github.n1try.quiznerd.model;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class FirestoreQuizMatchDto extends QuizMatch {
    private transient DocumentReference player1Reference;
    private transient DocumentReference player2Reference;

    public FirestoreQuizMatchDto(QuizMatch match, DocumentReference player1Reference, DocumentReference player2Reference) {
        super(match.getId(), match.getCategory(), match.getRound(), match.isActive(), match.getUpdated(), match.getRounds());
        this.player1Reference = player1Reference;
        this.player2Reference = player2Reference;
    }

    @Builder
    private FirestoreQuizMatchDto(String id, QuizCategory quizCategory, int round, boolean active, List<QuizRound> rounds, Date updated, DocumentReference player1Reference, DocumentReference player2Reference) {
        super(id, quizCategory, round, active, updated, rounds);
        this.player1Reference = player1Reference;
        this.player2Reference = player2Reference;
    }
}
