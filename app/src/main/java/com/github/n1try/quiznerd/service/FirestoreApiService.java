package com.github.n1try.quiznerd.service;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.n1try.quiznerd.model.QuizAnswer;
import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizRound;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.Constants;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class FirestoreApiService extends QuizApiService {
    public static final String COLL_MATCHES = "matches";
    public static final String COLL_USERS = "users";
    public static final String COLL_QUESTIONS = "questions";

    private static final String TAG = "FirestoreApiService";
    private static final FirestoreApiService ourInstance = new FirestoreApiService();
    private final int RETRY_FETCH_QUESTIONS = 10;

    private final FirebaseFirestore mFirestore;
    private final FirebaseFunctions mFunctions;
    private final Gson mGson;

    public static FirestoreApiService getInstance() {
        return ourInstance;
    }

    private FirestoreApiService() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mFirestore = FirebaseFirestore.getInstance();
        mFirestore.setFirestoreSettings(settings);
        mFunctions = FirebaseFunctions.getInstance();
        mGson = new Gson();
    }

    private Task<QuerySnapshot> tryFetchRandomQuestions(int n, QuizCategory category, Map<QuizCategory, Integer> categoryCount) {
        int rand = new Random().nextInt(categoryCount.get(category)) - 1;
        return mFirestore.collection(COLL_QUESTIONS)
                .whereEqualTo("category", category.name())
                .whereGreaterThan("catInc", rand)
                .orderBy("catInc")
                .limit(n)
                .get();
    }

    /* Based on https://stackoverflow.com/a/46801925/3112139 */
    public void fetchRandomQuestions(final int n, final QuizCategory category, final Map<QuizCategory, Integer> categoryCount, final QuizApiCallbacks callback) {
        final List<QuizQuestion> questions = new ArrayList<>(n);
        final Task fetchTask = tryFetchRandomQuestions(n, category, categoryCount);
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
                    final QuizQuestion q = doc.toObject(QuizQuestion.class);
                    q.setId(doc.getId());
                    if (!questions.contains(q)) {
                        questions.add(q);
                    }
                }

                if (questions.size() < n && tries.incrementAndGet() <= RETRY_FETCH_QUESTIONS) {
                    fetchTask.continueWithTask((Continuation<QuerySnapshot, Task<QuerySnapshot>>) task -> tryFetchRandomQuestions(n, category, categoryCount)).addOnSuccessListener(new OnQuestionsFetchedSuccess()).addOnFailureListener(new OnQuestionsFetchedFailed());
                } else {
                    callback.onRandomQuestionsFetched(questions);
                }
            }
        }

        fetchTask.addOnSuccessListener(new OnQuestionsFetchedSuccess());
        fetchTask.addOnFailureListener(new OnQuestionsFetchedFailed());
    }

    public void getUserById(String id, final QuizApiCallbacks callback) {
        mFirestore.collection(COLL_USERS)
                .document(id)
                .get()
                .addOnSuccessListener(snapshot -> {
                    QuizUser user = snapshot.toObject(QuizUser.class);
                    if (user == null) {
                        callback.onError(new Resources.NotFoundException("User not found"));
                    } else {
                        user.setId(snapshot.getId());
                        userCache.put(user.getId(), user);
                        callback.onUsersFetched(Collections.singletonList(user));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void getUserByAuthentication(String authentication, final QuizApiCallbacks callback) {
        mFirestore.collection(COLL_USERS)
                .whereEqualTo("authentication", authentication)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    final List<DocumentSnapshot> docs = snapshot.getDocuments();
                    if (docs.isEmpty()) {
                        callback.onError(new Resources.NotFoundException("User not found"));
                        return;
                    }
                    final QuizUser user = docs.get(0).toObject(QuizUser.class);
                    user.setId(docs.get(0).getId());
                    userCache.put(user.getId(), user);
                    callback.onUsersFetched(Collections.singletonList(user));
                })
                .addOnFailureListener(callback::onError);
    }

    public void fetchActiveMatches(String userId, QuizApiCallbacks callback) {
        fetchMatches(true, userId, callback);
    }

    public void fetchPastMatches(String userId, QuizApiCallbacks callback) {
        fetchMatches(false, userId, callback);
    }

    private void fetchMatches(boolean active, final String userId, final QuizApiCallbacks callback) {
        Task<QuerySnapshot> task;
        if (active) {
            task = mFirestore.collection(COLL_MATCHES)
                    .whereEqualTo(String.format("acknowledge.%s", userId), false)
                    .get(Source.DEFAULT);
        } else {
            task = mFirestore.collection(COLL_MATCHES)
                    .whereEqualTo(String.format("acknowledge.%s", userId), true)
                    .whereEqualTo("active", false)
                    .get(Source.CACHE);
        }

        task
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final List<QuizMatch> matches = new ArrayList<>(queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        final QuizMatch m = doc.toObject(QuizMatch.class);
                        m.setId(doc.getId());

                        if (!isValid(m)) {
                            Log.w(TAG, String.format("Found invalid match with id %s.", m.getId()));
                            continue;
                        }

                        userCache.put(m.getPlayer1().getId(), m.getPlayer1());
                        userCache.put(m.getPlayer2().getId(), m.getPlayer2());
                        matchCache.put(m.getId(), m);
                        matches.add(m);

                        if (!m.getAcknowledge().get(userId) && !m.isActive()) {
                            m.acknowledge(QuizUser.builder().id(userId).build());
                            updateQuizState(m);
                        }
                    }

                    Collections.sort(matches);
                    callback.onMatchesFetched(matches);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, e.getMessage());
                    callback.onError(e);
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
                        "active", match.isActive(),
                        "acknowledge", match.getAcknowledge()
                );
    }

    @Override
    public void deleteMatch(final QuizMatch match, final QuizApiCallbacks callback) {
        mFirestore.collection(COLL_MATCHES)
                .document(match.getId())
                .delete()
        .addOnSuccessListener(aVoid -> {
            matchCache.remove(match);
            callback.onMatchDeleted(match);
        })
        .addOnFailureListener(callback::onError);
    }

    @Override
    public void pokeOpponent(QuizMatch match, QuizUser me) {
        Map<String, String> data = new HashMap<>();
        data.put("playerId", me.getId());
        data.put("opponentId", match.getOpponent(me).getId());
        data.put("category", match.getCategory().getDisplayName());
        mFunctions.getHttpsCallable("poke").call(data);
    }

    public void createMatch(final QuizMatch match, final QuizApiCallbacks callback) {
        final Map<String, Object> dto = mGson.fromJson(mGson.toJsonTree(match), new TypeToken<Map<String, Object>>(){}.getType());
        dto.remove("id");
        dto.put("updated", match.getUpdated()); // Firestore understands java.util.Date

        mFirestore.collection(COLL_MATCHES)
                .add(dto)
                .addOnSuccessListener(documentReference -> {
                    match.setId(documentReference.getId());
                    callback.onMatchCreated(match);
                })
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void createUser(final QuizUser user, final QuizApiCallbacks callback) {
        Map<String, Object> dto = mGson.fromJson(mGson.toJsonTree(user), new TypeToken<Map<String, Object>>(){}.getType());
        dto.remove("id");

        mFirestore.collection(COLL_USERS)
                .document(user.getId())
                .set(dto)
                .addOnSuccessListener(v -> callback.onUserCreated(user))
                .addOnFailureListener(callback::onError);
    }

    private static boolean isValid(QuizMatch match) {
        if (TextUtils.isEmpty(match.getPlayer1().getId())) return false;
        if (TextUtils.isEmpty(match.getPlayer2().getId())) return false;
        if (TextUtils.isEmpty(match.getPlayer1().getAuthentication())) return false;
        if (TextUtils.isEmpty(match.getPlayer2().getAuthentication())) return false;
        if (match.getPlayer1().getGender() == null) return false;
        if (match.getPlayer2().getGender() == null) return false;
        if (match.getRound() < 1 || match.getRound() > Constants.NUM_ROUNDS) return false;
        if (match.getRound() > match.getRounds().size()) return false;
        if (!match.isActive() && !match.isOver()) return false;
        if (!match.getAcknowledge().containsKey(match.getPlayer1().getId())) return false;
        if (!match.getAcknowledge().containsKey(match.getPlayer2().getId())) return false;
        if (match.getAcknowledge().size() != 2) return false;
        if (match.getCategory() == null) return false;

        for (int i = 0; i < match.getRounds().size(); i++) {
            QuizRound r = match.getRounds().get(i);
            if (r.getQuestions().size() != Constants.NUM_QUESTIONS_PER_ROUND) return false;
            if (r.getAnswers1().size() != r.getQuestions().size()) return false;
            if (r.getAnswers2().size() != r.getQuestions().size()) return false;
            if (r.getId() != i + 1) return false;

            for (int j = 0; j < r.getQuestions().size(); j++) {
                QuizQuestion q = r.getQuestion(j);
                if (TextUtils.isEmpty(q.getText())) return false;
                if (q.getCategory() == null) return false;

                for (int k = 0; k < q.getAnswers().size(); k++) {
                    QuizAnswer a = q.getAnswer(k);
                    if (TextUtils.isEmpty(a.getText())) return false;
                }
            }

            for (int l = 0; l < r.getAnswers1().size(); l++) {
                Long ua1 = r.getAnswers1().get(l);
                Long ua2 = r.getAnswers2().get(l);
                if (ua1 > r.getQuestions().size() || (ua1 < 0 && ua1 != QuizAnswer.EMPTY_ANSWER_ID && ua1 != QuizAnswer.TIMEOUT_ANSWER_ID)) return false;
                if (ua2 > r.getQuestions().size() || (ua2 < 0 && ua2 != QuizAnswer.EMPTY_ANSWER_ID && ua2 != QuizAnswer.TIMEOUT_ANSWER_ID)) return false;
            }
        }
        return true;
    }
}
