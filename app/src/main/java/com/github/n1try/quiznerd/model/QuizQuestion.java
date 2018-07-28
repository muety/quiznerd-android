package com.github.n1try.quiznerd.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
public class QuizQuestion implements Parcelable {
    private String id;
    private long inc;
    private long catInc;
    private String text;
    private String creatorId;
    private String code;
    private QuizCategory category;
    private String random;
    private List<QuizAnswer> answers;

    public static final Creator<QuizQuestion> CREATOR = new Creator<QuizQuestion>() {
        @Override
        public QuizQuestion createFromParcel(Parcel in) {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(in.readString(), QuizQuestion.class);
        }

        @Override
        public QuizQuestion[] newArray(int size) {
            return new QuizQuestion[size];
        }
    };

    public QuizAnswer getCorrectAnswer() {
        for (QuizAnswer answer : answers) {
            if (answer.isCorrect()) return answer;
        }
        return null;
    }

    public QuizAnswer getAnswer(int index) {
        if (index == QuizAnswer.EMPTY_ANSWER_ID) return QuizAnswer.EMPTY_ANSWER;
        if (index == QuizAnswer.TIMEOUT_ANSWER_ID) return QuizAnswer.TIMEOUT_ANSWER;
        return answers.get(index);
    }

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
