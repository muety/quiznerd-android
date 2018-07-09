package com.github.n1try.quiznerd.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRound implements Parcelable {
    private int id;
    private List<QuizQuestion> questions;
    private List<Long> answers1 = new ArrayList<>();
    private List<Long> answers2 = new ArrayList<>();

    protected QuizRound(Parcel in) {
        id = in.readInt();
        questions = in.createTypedArrayList(QuizQuestion.CREATOR);
    }

    public static final Creator<QuizRound> CREATOR = new Creator<QuizRound>() {
        @Override
        public QuizRound createFromParcel(Parcel in) {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(in.readString(), QuizRound.class);
        }

        @Override
        public QuizRound[] newArray(int size) {
            return new QuizRound[size];
        }
    };

    public QuizCategory getCategory() {
        return questions.get(0).getCategory();
    }

    public int[] getScores() {
        int[] scores = new int[]{0, 0};
        for (int i = 0; i < questions.size(); i++) {
            int correctAnswer = questions.get(i).getCorrectAnswer().getId();
            int user1Answer = answers1.size() > i ? answers1.get(i).intValue() : -1;
            int user2Answer = answers2.size() > i ? answers2.get(i).intValue() : -1;
            if (correctAnswer == user1Answer) scores[0]++;
            if (correctAnswer == user2Answer) scores[1]++;
        }
        return scores;
    }

    public QuizQuestion getQuestion(int index) {
        return questions.get(index);
    }

    /* playerIdx starts at 1 */
    public boolean isQuestionCorrect(int questionIdx, int playerIdx) {
        if (playerIdx == 1) {
            return answers1.size() > questionIdx && questions.size() > questionIdx && answers1.get(questionIdx) == questions.get(questionIdx).getCorrectAnswer().getId();
        } else if (playerIdx == 2) {
            return answers2.size() > questionIdx && questions.size() > questionIdx && answers2.get(questionIdx) == questions.get(questionIdx).getCorrectAnswer().getId();
        }
        return false;
    }

    public void answerQuestionAt(int questionIdx, QuizAnswer answer, int playerIdx) {
        try {
            if (playerIdx == 1) answers1.set(questionIdx, Long.valueOf(answer.getId()));
            else if (playerIdx == 2) answers2.set(questionIdx, Long.valueOf(answer.getId()));
        } catch (ArrayIndexOutOfBoundsException e) {
            // TODO
        }
    }

    public int getQuestionIndex(QuizQuestion question) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).equals(question)) return i;
        }
        return -1;
    }

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
