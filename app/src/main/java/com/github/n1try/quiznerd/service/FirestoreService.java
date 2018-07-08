package com.github.n1try.quiznerd.service;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.n1try.quiznerd.model.FirestoreQuizMatchResult;
import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizRound;
import com.github.n1try.quiznerd.model.QuizUser;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
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
    private QuizCacheService mQuizCache;
    private Gson mGson;

    public static FirestoreService getInstance() {
        return ourInstance;
    }

    private FirestoreService() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mFirestore = FirebaseFirestore.getInstance();
        mFirestore.setFirestoreSettings(settings);
        mGson = new Gson();
        mQuizCache = QuizCacheService.getInstance();
    }

    public interface FirestoreCallbacks {
        void onMatchesFetched(List<QuizMatch> matches);

        void onUsersFetched(List<QuizUser> users);

        void onError(Exception e);
    }

    public void fetchUserByMail(String emailQuery, final FirestoreCallbacks callback) {
        mFirestore.collection(COLL_USERS)
                .whereEqualTo("email", emailQuery)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        if (docs.size() != 1) {
                            callback.onError(new Resources.NotFoundException("Couldn't fetch user."));
                        }
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

    public void fetchUserByAuthentication(String authentication, final FirestoreCallbacks callback) {
        mFirestore.collection(COLL_USERS)
                .whereEqualTo("authentication", authentication)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        if (docs.size() != 1) {
                            callback.onError(new Resources.NotFoundException("Couldn't fetch user."));
                        }
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
        mFirestore.collection(COLL_MATCHES)
                .whereEqualTo("active", true)
                .get()
                .continueWith(new CreateQuizMatches())
                .continueWithTask(new FetchPlayers())
                .continueWith(new Cast())
                .addOnSuccessListener(new OnSuccessListener<List<QuizMatch>>() {
                    @Override
                    public void onSuccess(List<QuizMatch> firestoreQuizMatchResults) {
                        for (QuizMatch m : firestoreQuizMatchResults) {
                            mQuizCache.matchCache.put(m.getId(), m);
                        }
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

    public void updateQuizRounds(QuizMatch match) {
        mFirestore.collection(COLL_MATCHES)
                .document(match.getId())
                .update(
                        "rounds", mGson.fromJson(mGson.toJsonTree(match.getRounds()), List.class),
                        "round", match.getRound(),
                        "active", match.isActive()
                );
    }

    public void updateQuizState(QuizMatch match) {
        mFirestore.collection(COLL_MATCHES)
                .document(match.getId())
                .update(
                        "active", match.isActive()
                );
    }

    private class CreateQuizMatches implements Continuation<QuerySnapshot, List<FirestoreQuizMatchResult>> {
        @Override
        public List<FirestoreQuizMatchResult> then(@NonNull Task<QuerySnapshot> task) {
            List<FirestoreQuizMatchResult> matches = new ArrayList<>(task.getResult().size());
            for (QueryDocumentSnapshot doc : task.getResult()) {
                List<Map> roundReferences = (List<Map>) doc.get("rounds");
                List<QuizRound> rounds = new ArrayList<>(roundReferences.size());
                for (Map ref : roundReferences) {
                    rounds.add(mGson.fromJson(mGson.toJsonTree(ref), QuizRound.class));
                }

                matches.add(FirestoreQuizMatchResult.builder()
                        .id(doc.getId())
                        .player1Reference(doc.getDocumentReference("player1"))
                        .player2Reference(doc.getDocumentReference("player2"))
                        .rounds(rounds)
                        .quizCategory(QuizCategory.valueOf(doc.getString("category")))
                        .round(doc.getLong("round").intValue())
                        .active(doc.getBoolean("active"))
                        .updated(doc.getDate("updated"))
                        .build());
            }
            return matches;
        }
    }

    private class FetchPlayers implements Continuation<List<FirestoreQuizMatchResult>, Task<List<FirestoreQuizMatchResult>>> {
        @Override
        public Task<List<FirestoreQuizMatchResult>> then(@NonNull Task<List<FirestoreQuizMatchResult>> task) {
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
        public FirestoreQuizMatchResult then(@NonNull Task<List<DocumentSnapshot>> task) {
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
        public List<QuizMatch> then(@NonNull Task<List<FirestoreQuizMatchResult>> task) {
            List<QuizMatch> matches = new ArrayList<>();
            for (FirestoreQuizMatchResult match : task.getResult()) {
                matches.add(match);
            }
            return matches;
        }
    }
}
