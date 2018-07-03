package com.github.n1try.quiznerd.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Data;

@Data
public class QuizAnswer implements Parcelable {
    private int id;
    private String text;
    private boolean correct;

    protected QuizAnswer(Parcel in) {
        id = in.readInt();
        text = in.readString();
        correct = in.readByte() != 0;
    }

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
