package com.github.n1try.quiznerd.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.n1try.quiznerd.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizMatch implements Parcelable {
    private String id;
    private QuizCategory category;
    private QuizUser player1;
    private QuizUser player2;
    private int round; // starts at 1
    private boolean active;
    private Date updated;
    private List<QuizRound> rounds;

    public QuizMatch(String id, QuizCategory quizCategory, int round, boolean active, Date updated, QuizUser player1, QuizUser player2, List<QuizRound> rounds) {
        this.id = id;
        this.category = quizCategory;
        this.round = round;
        this.active = active;
        this.updated = updated;
        this.rounds = rounds;
        this.player1 = player1;
        this.player2 = player2;
    }

    public QuizMatch(String id, QuizCategory quizCategory, int round, boolean active, Date updated, List<QuizRound> rounds) {
        this.id = id;
        this.category = quizCategory;
        this.round = round;
        this.active = active;
        this.updated = updated;
        this.rounds = rounds;
    }

    protected QuizMatch(Parcel in) {
        id = in.readString();
        round = in.readInt();
        active = in.readByte() != 0;
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

        int myPlayerIndex = getMyPlayerIndex(me);
        int opponentPlayerIndex = (myPlayerIndex % 2) + 1;
        int myAnswerCount = getCurrentRound().countAnswers(myPlayerIndex);
        int opponentAnswerCount = getCurrentRound().countAnswers(opponentPlayerIndex);

        if (myAnswerCount == opponentAnswerCount) {
            return !isInitiator(me);
        }

        if (myAnswerCount < getCurrentRound().getQuestions().size()) {
            return true;
        }

        return false;
    }

    public int[] getScores() {
        int[] scores = new int[] {0, 0};
        for (QuizRound r : rounds) {
            int[] score = r.getScores();
            scores[0] += score[0];
            scores[1] += score[1];
        }
        return scores;
    }

    public QuizResult getResult(QuizUser me) {
        if (isActive()) return QuizResult.PENDING;
        int[] scores = getScores();
        int playerIndex = getMyPlayerIndex(me) - 1;
        if (scores[playerIndex] > scores[playerIndex ^ 1]) return QuizResult.WON;
        else if (scores[playerIndex] < scores[playerIndex ^ 1]) return QuizResult.LOST;
        else return QuizResult.DRAW;
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
}
