package com.github.n1try.quiznerd.service;

import android.support.annotation.NonNull;

import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.User;
import com.github.n1try.quiznerd.model.firestore.FirestoreQuizMatchResult;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public void fetchMatch() {
        mFirestore.collection(COLL_MATCHES).document("WnA5RZ7FKtj1Vr627G3o").get()
                .continueWith(new CreateQuizMatch())
                .continueWithTask(new FetchPlayers())
                .addOnCompleteListener(new OnCompleteListener<FirestoreQuizMatchResult>() {
                    @Override
                    public void onComplete(@NonNull Task<FirestoreQuizMatchResult> task) {
                        System.out.println(1);
                    }
                });
    }

    private class CreateQuizMatch implements Continuation<DocumentSnapshot, FirestoreQuizMatchResult> {
        @Override
        public FirestoreQuizMatchResult then(@NonNull Task<DocumentSnapshot> task) throws Exception {
            DocumentSnapshot doc = task.getResult();
            return FirestoreQuizMatchResult.builder()
                    .id(doc.getId())
                    .player1Reference(doc.getDocumentReference("player1"))
                    .player2Reference(doc.getDocumentReference("player2"))
                    .quizCategory(QuizCategory.valueOf(doc.getString("category")))
                    .round(doc.getLong("round").intValue())
                    .active(doc.getBoolean("active"))
                    .updated(doc.getDate("updated"))
                    .build();
        }
    }

    private class FetchPlayers implements Continuation<FirestoreQuizMatchResult, Task<FirestoreQuizMatchResult>> {
        @Override
        public Task<FirestoreQuizMatchResult> then(@NonNull Task<FirestoreQuizMatchResult> task) throws Exception {
            final FirestoreQuizMatchResult match = task.getResult();
            Task t1 = match.getPlayer1Reference().get();
            Task t2 = match.getPlayer2Reference().get();
            return Tasks.whenAllComplete(t1, t2)
                    .continueWith(new Continuation<List<Task<?>>, FirestoreQuizMatchResult>() {
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
                    });
        }
    }
}
