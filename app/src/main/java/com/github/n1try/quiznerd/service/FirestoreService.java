package com.github.n1try.quiznerd.service;

import android.support.annotation.NonNull;

import com.github.n1try.quiznerd.model.FirestoreQuizMatchResult;
import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreService {
    public static final String COLL_MATCHES = "matches";

    private static final String TAG = "FirestoreService";
    private static final FirestoreService ourInstance = new FirestoreService();

    private FirebaseFirestore mFirestore;

    public static FirestoreService getInstance() {
        return ourInstance;
    }

    private FirestoreService() {
        mFirestore = FirebaseFirestore.getInstance();
    }

    public void fetchMatches() {
        mFirestore.collection(COLL_MATCHES).get()
                .continueWith(new CreateQuizMatches())
                .continueWithTask(new FetchPlayers())
                .continueWithTask(new FetchQuestions())
                .addOnSuccessListener(new OnSuccessListener<List<FirestoreQuizMatchResult>>() {
                    @Override
                    public void onSuccess(List<FirestoreQuizMatchResult> firestoreQuizMatchResults) {
                        System.out.println(1);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(1);
                    }
                });
    }

    private class CreateQuizMatches implements Continuation<QuerySnapshot, List<FirestoreQuizMatchResult>> {
        @Override
        public List<FirestoreQuizMatchResult> then(@NonNull Task<QuerySnapshot> task) throws Exception {
            List<FirestoreQuizMatchResult> matches = new ArrayList<>(task.getResult().size());
            for (QueryDocumentSnapshot doc : task.getResult()) {
                matches.add(FirestoreQuizMatchResult.builder()
                        .id(doc.getId())
                        .player1Reference(doc.getDocumentReference("player1"))
                        .player2Reference(doc.getDocumentReference("player2"))
                        .questionsReference((List<DocumentReference>) doc.getData().get("questions"))
                        .quizCategory(QuizCategory.valueOf(doc.getString("category")))
                        .round(doc.getLong("round").intValue())
                        .active(doc.getBoolean("active"))
                        .updated(doc.getDate("updated"))
                        .build());
            }
            return matches;
        }
    }

    private class FetchQuestions implements Continuation<List<FirestoreQuizMatchResult>, Task<List<FirestoreQuizMatchResult>>> {
        @Override
        public Task<List<FirestoreQuizMatchResult>> then(@NonNull Task<List<FirestoreQuizMatchResult>> task) throws Exception {
            List<Task<?>> tasks = new ArrayList<>();
            for (FirestoreQuizMatchResult match : task.getResult()) {
                List<Task<?>> subtasks = new ArrayList<>();
                for (DocumentReference question : match.getQuestionsReference()) {
                    subtasks.add(question.get().continueWith(new AddQuestion(match)));
                }
                tasks.add(Tasks.whenAllSuccess(subtasks));
            }
            return Tasks.whenAllSuccess(tasks);
        }
    }

    private class FetchPlayers implements Continuation<List<FirestoreQuizMatchResult>, Task<List<FirestoreQuizMatchResult>>> {
        @Override
        public Task<List<FirestoreQuizMatchResult>> then(@NonNull Task<List<FirestoreQuizMatchResult>> task) throws Exception {
            List<Task<?>> tasks = new ArrayList<>();
            for (FirestoreQuizMatchResult match : task.getResult()) {
                Task<DocumentSnapshot> t1 = match.getPlayer1Reference().get();
                Task<DocumentSnapshot> t2 = match.getPlayer2Reference().get();
                tasks.add(Tasks.whenAllComplete(t1, t2).continueWith(new AddPlayers(match)));
            }
            return Tasks.whenAllSuccess(tasks);
        }
    }

    private class AddPlayers implements Continuation<List<Task<?>>, FirestoreQuizMatchResult> {
        private FirestoreQuizMatchResult match;

        public AddPlayers(FirestoreQuizMatchResult match) {
            this.match = match;
        }

        @Override
        public FirestoreQuizMatchResult then(@NonNull Task<List<Task<?>>> task) throws Exception {
            List<Task<?>> subtasks = task.getResult();
            DocumentSnapshot ref1 = ((DocumentSnapshot) subtasks.get(0).getResult());
            DocumentSnapshot ref2 = ((DocumentSnapshot) subtasks.get(0).getResult());
            User player1 = ref1.toObject(User.class);
            User player2 = ref2.toObject(User.class);
            player1.setId(ref1.getId());
            player2.setId(ref2.getId());
            match.setPlayer1(player1);
            match.setPlayer2(player2);
            return match;
        }
    }

    private class AddQuestion implements Continuation<DocumentSnapshot, FirestoreQuizMatchResult> {
        private FirestoreQuizMatchResult match;

        public AddQuestion(FirestoreQuizMatchResult match) {
            this.match = match;
        }

        @Override
        public FirestoreQuizMatchResult then(@NonNull Task<DocumentSnapshot> task) throws Exception {
            DocumentSnapshot ref = task.getResult();
            QuizQuestion question = ref.toObject(QuizQuestion.class);
            question.setId(ref.getId());
            match.getQuestions().add(question);
            return match;
        }
    }
}
