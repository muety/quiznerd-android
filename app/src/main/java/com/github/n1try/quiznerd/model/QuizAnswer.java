package com.github.n1try.quiznerd.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizAnswer implements Parcelable {
    private int id;
    private String text;
    private boolean correct;

    public static int EMPTY_ANSWER_ID = -1;
    public static int TIMEOUT_ANSWER_ID = -2;
    public static QuizAnswer EMPTY_ANSWER = new QuizAnswer(EMPTY_ANSWER_ID, "(not answered)", false);
    public static QuizAnswer TIMEOUT_ANSWER = new QuizAnswer(TIMEOUT_ANSWER_ID, "(timeout)", false);

    public static final Creator<QuizAnswer> CREATOR = new Creator<QuizAnswer>() {
        @Override
        public QuizAnswer createFromParcel(Parcel in) {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(in.readString(), QuizAnswer.class);
        }

        @Override
        public QuizAnswer[] newArray(int size) {
            return new QuizAnswer[size];
        }
    };

    @Override
    public int describeContents() {
        return id;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Gson gson = new GsonBuilder().create();
        parcel.writeString(gson.toJson(this));
    }
}
