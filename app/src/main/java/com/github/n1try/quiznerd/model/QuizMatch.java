package com.github.n1try.quiznerd.model;

import android.os.Parcel;
import android.os.Parcelable;

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
    private QuizCategory quizCategory;
    private QuizUser player1;
    private QuizUser player2;
    private int round;
    private boolean active;
    private Date updated;
    private List<QuizRound> rounds;

    public QuizMatch(String id, QuizCategory quizCategory, int round, boolean active, Date updated, List<QuizRound> rounds) {
        this.id = id;
        this.quizCategory = quizCategory;
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
        if (isInitiator(me) && getRound() % 2 == 0) return true;
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

    public QuizRound getCurrentRound() {
        return rounds.get(round - 1);
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
