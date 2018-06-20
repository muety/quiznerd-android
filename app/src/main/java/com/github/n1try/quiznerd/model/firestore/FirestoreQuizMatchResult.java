package com.github.n1try.quiznerd.model.firestore;

import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUserAnswer;
import com.github.n1try.quiznerd.model.User;
import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class FirestoreQuizMatchResult extends QuizMatch {
    @Builder
    private FirestoreQuizMatchResult(String id, QuizCategory quizCategory, User player1, User player2, int round, boolean active, Date updated, List<QuizQuestion> questions, List<QuizUserAnswer> answers1, List<QuizUserAnswer> answers2, DocumentReference player1Reference, DocumentReference player2Reference, List<DocumentReference> questionsReference) {
        super(id, quizCategory, player1, player2, round, active, updated, questions, answers1, answers2);
        this.player1Reference = player1Reference;
        this.player2Reference = player2Reference;
        this.questionsReference = questionsReference;
    }

    private DocumentReference player1Reference;
    private DocumentReference player2Reference;
    private List<DocumentReference> questionsReference;
}
