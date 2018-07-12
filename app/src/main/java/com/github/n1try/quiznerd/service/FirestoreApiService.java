package com.github.n1try.quiznerd.service;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.n1try.quiznerd.model.FirestoreQuizMatchDto;
import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizRound;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.RandomString;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FirestoreApiService extends QuizApiService {
    public static final String COLL_MATCHES = "matches";
    public static final String COLL_USERS = "users";
    public static final String COLL_QUESTIONS = "questions";

    private static final String TAG = "FirestoreApiService";
    private static final FirestoreApiService ourInstance = new FirestoreApiService();
    private final int RETRY_FETCH_QUESTIONS = 10;

    private FirebaseFirestore mFirestore;
    private Gson mGson;

    public static FirestoreApiService getInstance() {
        return ourInstance;
    }

    private FirestoreApiService() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mFirestore = FirebaseFirestore.getInstance();
        mFirestore.setFirestoreSettings(settings);
        mGson = new Gson();
    }

    private Task<QuerySnapshot> tryFetchRandomQuestions(int n, QuizCategory category) {
        String rand = new RandomString(Constants.RANDOM_ID_LENGTH).nextString();
        return mFirestore.collection(COLL_QUESTIONS)
                .whereEqualTo("category", category.name())
                .whereGreaterThan("random", rand)
                .orderBy("random")
                .limit(n)
                .get();
    }

    /* Based on https://stackoverflow.com/a/46801925/3112139 */
    public void fetchRandomQuestions(final int n, final QuizCategory category, final QuizApiCallbacks callback) {
        final List<QuizQuestion> questions = new ArrayList<>(n);
        final Task fetchTask = tryFetchRandomQuestions(n, category);
        final AtomicInteger tries = new AtomicInteger(0);

        class OnQuestionsFetchedFailed implements OnFailureListener {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
                callback.onError(e);
            }
        }

        class OnQuestionsFetchedSuccess implements OnSuccessListener<QuerySnapshot> {
            @Override
            public void onSuccess(QuerySnapshot o) {
                for (DocumentSnapshot doc : o.getDocuments()) {
                    QuizQuestion q = doc.toObject(QuizQuestion.class);
                    q.setId(doc.getId());
                    questions.add(q);
                }

                if (questions.size() < n && tries.incrementAndGet() <= RETRY_FETCH_QUESTIONS) {
                    Log.d(TAG, String.format("Retry %s for category %s", tries.get(), category));
                    fetchTask.continueWithTask(new Continuation<QuerySnapshot, Task<QuerySnapshot>>() {
                        @Override
                        public Task<QuerySnapshot> then(@NonNull Task<QuerySnapshot> task) {
                            return tryFetchRandomQuestions(n, category);
                        }
                    }).addOnSuccessListener(new OnQuestionsFetchedSuccess()).addOnFailureListener(new OnQuestionsFetchedFailed());
                } else {
                    callback.onRandomQuestionsFetched(questions);
                }
            }
        }

        fetchTask.addOnSuccessListener(new OnQuestionsFetchedSuccess());
        fetchTask.addOnFailureListener(new OnQuestionsFetchedFailed());
    }

    public void fetchUserByNickname(String nickname, final QuizApiCallbacks callback) {
        mFirestore.collection(COLL_USERS)
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        if (docs.size() != 1) {
                            callback.onError(new Resources.NotFoundException("Couldn't find user."));
                            return;
                        }
                        QuizUser user = docs.get(0).toObject(QuizUser.class);
                        user.setId(docs.get(0).getId());
                        userCache.put(user.getId(), user);
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

    public void fetchUserById(String id, final QuizApiCallbacks callback) {
        mFirestore.collection(COLL_USERS)
                .document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        QuizUser user = snapshot.toObject(QuizUser.class);
                        user.setId(snapshot.getId());
                        userCache.put(user.getId(), user);
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

    public void fetchActiveMatches(String userId, QuizApiCallbacks callback) {
        fetchMatches(true, userId, callback);
    }

    public void fetchPastMatches(String userId, QuizApiCallbacks callback) {
        fetchMatches(false, userId, callback);
    }

    private void fetchMatches(boolean active, String userId, final QuizApiCallbacks callback) {
        Task<QuerySnapshot> task;
        if (active) {
            task = mFirestore.collection(COLL_MATCHES)
                    .whereEqualTo(String.format("players.%s", userId), true)
                    .whereEqualTo("active", true)
                    .get(Source.DEFAULT);
        } else {
            task = mFirestore.collection(COLL_MATCHES)
                    .whereEqualTo(String.format("players.%s", userId), true)
                    .whereEqualTo("active", false)
                    .whereEqualTo("archived", false)
                    .limit(Constants.NUM_PAST_MATCHES)
                    //.get(Source.CACHE);
                    .get(Source.DEFAULT);
        }

        task
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(1);
                    }
                })
                .continueWith(new CreateQuizMatches())
                .continueWithTask(new FetchPlayers())
                .continueWith(new Cast())
                .addOnSuccessListener(new OnSuccessListener<List<QuizMatch>>() {
                    @Override
                    public void onSuccess(List<QuizMatch> firestoreQuizMatchResults) {
                        for (QuizMatch m : firestoreQuizMatchResults) {
                            matchCache.put(m.getId(), m);
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
                        "rounds", mGson.fromJson(mGson.toJsonTree(match.getRounds()), List.class)
                );
    }

    public void updateQuizRound(QuizMatch match) {
        mFirestore.collection(COLL_MATCHES)
                .document(match.getId())
                .update(
                        "round", match.getRound(),
                        "updated", new Date()
                );
    }

    public void updateQuizState(QuizMatch match) {
        mFirestore.collection(COLL_MATCHES)
                .document(match.getId())
                .update(
                        "active", match.isActive()
                );
    }

    public void createMatch(final QuizMatch match, final QuizApiCallbacks callback) {
        DocumentReference ref1 = mFirestore.collection(COLL_USERS).document(match.getPlayer1().getId());
        DocumentReference ref2 = mFirestore.collection(COLL_USERS).document(match.getPlayer2().getId());

        Map dto = mGson.fromJson(mGson.toJsonTree(match), Map.class);
        dto.remove("player1");
        dto.remove("player2");
        dto.remove("id");
        dto.put("player1Reference", ref1);
        dto.put("player2Reference", ref2);
        dto.put("updated", match.getUpdated()); // Firestore understands java.util.Date

        mFirestore.collection(COLL_MATCHES)
                .add(dto)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        match.setId(documentReference.getId());
                        callback.onMatchCreated(match);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    private class CreateQuizMatches implements Continuation<QuerySnapshot, List<FirestoreQuizMatchDto>> {
        @Override
        public List<FirestoreQuizMatchDto> then(@NonNull Task<QuerySnapshot> task) {
            List<FirestoreQuizMatchDto> matches = new ArrayList<>(task.getResult().size());
            for (QueryDocumentSnapshot doc : task.getResult()) {
                List<Map> roundReferences = (List<Map>) doc.get("rounds");
                List<QuizRound> rounds = new ArrayList<>(roundReferences.size());
                for (Map ref : roundReferences) {
                    rounds.add(mGson.fromJson(mGson.toJsonTree(ref), QuizRound.class));
                }

                matches.add(FirestoreQuizMatchDto.builder()
                        .id(doc.getId())
                        .player1Reference(doc.getDocumentReference("player1Reference"))
                        .player2Reference(doc.getDocumentReference("player2Reference"))
                        .rounds(rounds)
                        .quizCategory(QuizCategory.valueOf(doc.getString("category")))
                        .round(doc.getLong("round").intValue())
                        .active(doc.getBoolean("active"))
                        .updated(doc.getDate("updated"))
                        .build());
            }

            Collections.sort(matches);
            return matches;
        }
    }

    private class FetchPlayers implements Continuation<List<FirestoreQuizMatchDto>, Task<List<FirestoreQuizMatchDto>>> {
        @Override
        public Task<List<FirestoreQuizMatchDto>> then(@NonNull Task<List<FirestoreQuizMatchDto>> task) {
            List<Task<FirestoreQuizMatchDto>> tasks = new ArrayList<>();
            for (FirestoreQuizMatchDto match : task.getResult()) {
                Map<String, Integer> playerIndexes = new HashMap<>();
                List<Task<DocumentSnapshot>> fetchPlayerTasks = new ArrayList<>();
                if (!userCache.containsKey(match.getPlayer1Reference().getId())) {
                    Log.d(TAG, String.format("Cache hit for player %s", match.getPlayer1Reference().getId()));
                    fetchPlayerTasks.add(match.getPlayer1Reference().get());
                }
                if (!userCache.containsKey(match.getPlayer2Reference().getId())) {
                    Log.d(TAG, String.format("Cache hit for player %s", match.getPlayer2Reference().getId()));
                    fetchPlayerTasks.add(match.getPlayer2Reference().get());
                }
                playerIndexes.put(match.getPlayer1Reference().getId(), 1);
                playerIndexes.put(match.getPlayer2Reference().getId(), 2);
                tasks.add(Tasks.<DocumentSnapshot>whenAllSuccess(fetchPlayerTasks).continueWith(new AddPlayers(match, playerIndexes)));
            }
            return Tasks.whenAllSuccess(tasks);
        }
    }

    private class AddPlayers implements Continuation<List<DocumentSnapshot>, FirestoreQuizMatchDto> {
        private FirestoreQuizMatchDto match;
        private Map<String, Integer> playerIndexes;

        public AddPlayers(FirestoreQuizMatchDto match, @Nullable Map<String, Integer> playerIndexes) {
            this.match = match;
            this.playerIndexes = playerIndexes;
        }

        @Override
        public FirestoreQuizMatchDto then(@NonNull Task<List<DocumentSnapshot>> task) {
            List<DocumentSnapshot> docs = task.getResult();
            QuizUser player1 = null;
            QuizUser player2 = null;

            for (Map.Entry<String, Integer> e : playerIndexes.entrySet()) {
                if (e.getValue() == 1) player1 = userCache.get(e.getKey());
                else if (e.getValue() == 2) player2 = userCache.get(e.getKey());
            }

            if (docs.size() == 2) {
                player1 = docs.get(0).toObject(QuizUser.class);
                player2 = docs.get(1).toObject(QuizUser.class);
                player1.setId(docs.get(0).getId());
                player2.setId(docs.get(1).getId());
            } else if (docs.size() == 1) {
                if (player1 == null) {
                    player1 = docs.get(0).toObject(QuizUser.class);
                    player1.setId(docs.get(0).getId());
                } else if (player2 == null) {
                    player2 = docs.get(0).toObject(QuizUser.class);
                    player2.setId(docs.get(0).getId());
                }
            }

            match.setPlayer1(player1);
            match.setPlayer2(player2);
            if (!userCache.containsKey(player1.getId())) userCache.put(player1.getId(), player1);
            if (!userCache.containsKey(player2.getId())) userCache.put(player2.getId(), player2);

            return match;
        }
    }

    private class Cast implements Continuation<List<FirestoreQuizMatchDto>, List<QuizMatch>> {
        @Override
        public List<QuizMatch> then(@NonNull Task<List<FirestoreQuizMatchDto>> task) {
            List<QuizMatch> matches = new ArrayList<>();
            for (FirestoreQuizMatchDto match : task.getResult()) {
                matches.add(match);
            }
            return matches;
        }
    }
}
