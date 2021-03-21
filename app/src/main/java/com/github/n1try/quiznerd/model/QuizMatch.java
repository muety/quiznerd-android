package com.github.n1try.quiznerd.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.github.n1try.quiznerd.utils.Constants;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class QuizMatch implements Parcelable, Comparable<QuizMatch> {
    private String id;
    private QuizCategory category;
    private QuizUser player1;
    private QuizUser player2;
    private int round; // starts at 1
    private boolean active;
    private Date updated;
    private List<QuizRound> rounds;
    private Map<String, Boolean> acknowledge; // Dirty hack; see https://firebase.google.com/docs/firestore/solutions/arrays

    /*
    Acknowledge state is false for both players until the end of the last round.
    The user, who sets active = false after the last round also sets acknowledge = true for himself, while leaving acknowledge = false
    for the opponent. After the opponent has fetched that match and noticed, that it's not active anymore, he also sets acknowledge = true
    for himself. Since users only request non-acknowledged matches from Firestore, he won't be fetching that match from the server again,
    but only from local cache.
     */

    public QuizMatch(String id, QuizCategory quizCategory, int round, boolean active, Date updated, QuizUser player1, QuizUser player2, List<QuizRound> rounds) {
        this.id = id;
        this.category = quizCategory;
        this.round = round;
        this.active = active;
        this.updated = updated;
        this.rounds = rounds;
        this.player1 = player1;
        this.player2 = player2;
        this.acknowledge = ImmutableMap.of(
                player1.getId(), false,
                player2.getId(), false
        );
    }

    public QuizMatch(String id, QuizCategory quizCategory, int round, boolean active, Date updated, List<QuizRound> rounds) {
        this.id = id;
        this.category = quizCategory;
        this.round = round;
        this.active = active;
        this.updated = updated;
        this.rounds = rounds;
    }

    public QuizUser getOpponent(QuizUser me) {
        if (getPlayer1().getAuthentication().equals(me.getAuthentication())) return getPlayer2();
        return getPlayer1();
    }

    public boolean isInitiator(QuizUser user) {
        return getPlayer1().getAuthentication().equals(user.getAuthentication());
    }

    public boolean isMyTurn(QuizUser me) {
        if (!active) {
            return false;
        }

        QuizRound r = getCurrentRound();
        int myPlayerIndex = getMyPlayerIndex(me);
        int opponentPlayerIndex = (myPlayerIndex % 2) + 1;
        int myAnswerCount = r.countAnswers(myPlayerIndex);
        int opponentAnswerCount = r.countAnswers(opponentPlayerIndex);
        int numQuestions = r.getQuestions().size();

        if (myAnswerCount == opponentAnswerCount) {
            return !isInitiator(me);
        }

        if (myAnswerCount < numQuestions) {
            if (r.hasPlayed((myPlayerIndex % 2) + 1)) return true;
            return myAnswerCount > opponentAnswerCount;
        }

        return false;
    }

    public int[] getScores() {
        int[] scores = new int[]{0, 0};
        for (QuizRound r : rounds) {
            int[] score = r.getScores();
            scores[0] += score[0];
            scores[1] += score[1];
        }
        return scores;
    }

    public int[] getSortedScores(QuizUser me) {
        int[] scores = getScores();
        int[] sorted = Arrays.copyOf(scores, 2);
        int playerIdx = getMyPlayerIndex(me);
        sorted[0] = scores[playerIdx - 1];
        sorted[1] = scores[(playerIdx - 1) ^ 1];
        return sorted;
    }

    public QuizResult getResult(QuizUser me) {
        if (isActive()) return QuizResult.PENDING;
        int[] scores = getScores();
        int playerIndex = getMyPlayerIndex(me) - 1;
        if (scores[playerIndex] > scores[playerIndex ^ 1]) return QuizResult.WON;
        else if (scores[playerIndex] < scores[playerIndex ^ 1]) return QuizResult.LOST;
        else return QuizResult.DRAW;
    }

    public boolean isOver() {
        for (QuizRound r : rounds) {
            if (!r.hasPlayed(1) || !r.hasPlayed(2)) return false;
        }
        return round >= Constants.NUM_ROUNDS || round >= rounds.size() - 1;
    }

    public int getMyPlayerIndex(QuizUser me) {
        return isInitiator(me) ? 1 : 2;
    }

    public QuizRound getCurrentRound() {
        return rounds.get(round - 1);
    }

    public void nextRound() {
        round = Math.min(round + 1, Constants.NUM_ROUNDS);
    }

    public void acknowledge(QuizUser me) {
        acknowledge.put(me.getId(), true);
    }

    public List<QuizRound> getDisplayRounds(QuizUser me) {
        List<QuizRound> filtered = new ArrayList<>();
        int playerIdx = getMyPlayerIndex(me);
        for (int i = 0; i < rounds.size(); i++) {
            QuizRound r = rounds.get(i);
            if (r.hasPlayed(playerIdx) || r.getId() == round) filtered.add(r);
        }
        return filtered;
    }

    public static final Creator<QuizMatch> CREATOR = new Creator<QuizMatch>() {
        @Override
        public QuizMatch createFromParcel(Parcel in) {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(in.readString(), QuizMatch.class);
        }

        @Override
        public QuizMatch[] newArray(int size) {
            return new QuizMatch[size];
        }
    };

    @Override
    public int describeContents() {
        return id.hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Gson gson = new GsonBuilder().create();
        parcel.writeString(gson.toJson(this));
    }

    @Override
    /* < 0 means argument is greater than object */
    /* Sort by active, then by updated */
    public int compareTo(@NonNull QuizMatch match) {
        return match.getUpdated().compareTo(updated);
    }
}
