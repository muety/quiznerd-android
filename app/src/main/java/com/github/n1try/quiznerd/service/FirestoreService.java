package com.github.n1try.quiznerd.service;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.n1try.quiznerd.model.FirestoreQuizMatchResult;
import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUser;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FirestoreService {
    public static final String COLL_MATCHES = "matches";
    public static final String COLL_USERS = "users";

    private static final String TAG = "FirestoreService";
    private static final FirestoreService ourInstance = new FirestoreService();

    private FirebaseFirestore mFirestore;
    private Gson mGson;

    public static FirestoreService getInstance() {
        return ourInstance;
    }

    private FirestoreService() {
        mFirestore = FirebaseFirestore.getInstance();
        mGson = new Gson();
    }

    public interface FirestoreCallbacks {
        void onMatchesFetched(List<QuizMatch> matches);

        void onUsersFetched(List<QuizUser> users);

        void onError(Exception e);
    }

    public void fetchOwnUser(FirebaseUser me, final FirestoreCallbacks callback) {
        mFirestore.collection(COLL_USERS).whereEqualTo("authentication", me.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        if (docs.size() != 1)
                            callback.onError(new Resources.NotFoundException("Couldn't fetch user."));
                        QuizUser user = docs.get(0).toObject(QuizUser.class);
                        user.setId(docs.get(0).getId());
                        callback.onUsersFetched(Arrays.asList(user));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    public void fetchActiveMatches(final FirestoreCallbacks callback) {
        mFirestore.collection(COLL_MATCHES).whereEqualTo("active", true).get()
                .continueWith(new CreateQuizMatches())
                .continueWithTask(new FetchPlayers())
                .continueWith(new Cast())
                .addOnSuccessListener(new OnSuccessListener<List<QuizMatch>>() {
                    @Override
                    public void onSuccess(List<QuizMatch> firestoreQuizMatchResults) {
                        callback.onMatchesFetched(firestoreQuizMatchResults);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                        callback.onError(e);
                    }
                });
    }

    private class CreateQuizMatches implements Continuation<QuerySnapshot, List<FirestoreQuizMatchResult>> {
        @Override
        public List<FirestoreQuizMatchResult> then(@NonNull Task<QuerySnapshot> task) throws Exception {
            List<FirestoreQuizMatchResult> matches = new ArrayList<>(task.getResult().size());
            for (QueryDocumentSnapshot doc : task.getResult()) {
                List<Map> questionReferences = (List<Map>) doc.get("questions");
                List<QuizQuestion> questions = new ArrayList<>(questionReferences.size());
                for (Map ref : questionReferences) {
                    questions.add(mGson.fromJson(mGson.toJsonTree(ref), QuizQuestion.class));
                }

                matches.add(FirestoreQuizMatchResult.builder()
                        .id(doc.getId())
                        .player1Reference(doc.getDocumentReference("player1"))
                        .player2Reference(doc.getDocumentReference("player2"))
                        .questions(questions)
                        .quizCategory(QuizCategory.valueOf(doc.getString("category")))
                        .round(doc.getLong("round").intValue())
                        .active(doc.getBoolean("active"))
                        .updated(doc.getDate("updated"))
                        .answers1((List<Long>) doc.get("answers1"))
                        .answers2((List<Long>) doc.get("answers2"))
                        .build());
            }
            return matches;
        }
    }

    private class FetchPlayers implements Continuation<List<FirestoreQuizMatchResult>, Task<List<FirestoreQuizMatchResult>>> {
        @Override
        public Task<List<FirestoreQuizMatchResult>> then(@NonNull Task<List<FirestoreQuizMatchResult>> task) throws Exception {
            List<Task<FirestoreQuizMatchResult>> tasks = new ArrayList<>();
            for (FirestoreQuizMatchResult match : task.getResult()) {
                Task<DocumentSnapshot> t1 = match.getPlayer1Reference().get();
                Task<DocumentSnapshot> t2 = match.getPlayer2Reference().get();
                tasks.add(Tasks.<DocumentSnapshot>whenAllSuccess(t1, t2).continueWith(new AddPlayers(match)));
            }
            return Tasks.whenAllSuccess(tasks);
        }
    }

    private class AddPlayers implements Continuation<List<DocumentSnapshot>, FirestoreQuizMatchResult> {
        private FirestoreQuizMatchResult match;

        public AddPlayers(FirestoreQuizMatchResult match) {
            this.match = match;
        }

        @Override
        public FirestoreQuizMatchResult then(@NonNull Task<List<DocumentSnapshot>> task) throws Exception {
            List<DocumentSnapshot> docs = task.getResult();
            QuizUser player1 = docs.get(0).toObject(QuizUser.class);
            QuizUser player2 = docs.get(1).toObject(QuizUser.class);
            player1.setId(docs.get(0).getId());
            player2.setId(docs.get(1).getId());
            match.setPlayer1(player1);
            match.setPlayer2(player2);

            return match;
        }
    }

    private class Cast implements Continuation<List<FirestoreQuizMatchResult>, List<QuizMatch>> {
        @Override
        public List<QuizMatch> then(@NonNull Task<List<FirestoreQuizMatchResult>> task) throws Exception {
            List<QuizMatch> matches = new ArrayList<>();
            for (FirestoreQuizMatchResult match : task.getResult()) {
                matches.add(match);
            }
            return matches;
        }
    }
}
