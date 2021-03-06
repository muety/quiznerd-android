package com.github.n1try.quiznerd.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Lists;
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
    public static List<Long> DEFAULT_ANSWERS = Lists.newArrayList(-1L, -1L, -1L);

    private int id;
    private List<QuizQuestion> questions;
    @Builder.Default
    private List<Long> answers1 = new ArrayList<>();
    @Builder.Default
    private List<Long> answers2 = new ArrayList<>();

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

    public boolean isQuestionAnswered(int questionIdx, int playerIdx) {
        List<Long> answers = playerIdx == 1 ? answers1 : answers2;
        return answers.get(questionIdx).intValue() != QuizAnswer.EMPTY_ANSWER_ID;
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
            if (playerIdx == 1) answers1.set(questionIdx, (long) answer.getId());
            else if (playerIdx == 2) answers2.set(questionIdx, (long) answer.getId());
        } catch (ArrayIndexOutOfBoundsException e) {
            // TODO
        }
    }

    public boolean hasPlayedRound(int playerIdx, int questionIdx) {
        List<Long> answers = playerIdx == 1 ? answers1 : answers2;
        return answers.get(questionIdx).intValue() != QuizAnswer.EMPTY_ANSWER_ID;
    }

    public boolean hasPlayed(int playerIdx) {
        for (int i = 0; i < questions.size(); i++) {
            if (!hasPlayedRound(playerIdx, i)) return false;
        }
        return true;
    }

    public int getQuestionIndex(QuizQuestion question) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).equals(question)) return i;
        }
        return -1;
    }

    public List<Long> getAnswersByPlayerIndex(int playerIndex) {
        return playerIndex == 1 ? answers1 : answers2;
    }

    public int getMyNextQuestionIndex(int playerIdx) {
        List<Long> answers = playerIdx == 1 ? answers1 : answers2;
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i) == QuizAnswer.EMPTY_ANSWER_ID) {
                return i;
            }
        }
        return -1;
    }

    public int countAnswers(int playerIdx) {
        List<Long> answers = playerIdx == 1 ? answers1 : answers2;
        int c = 0;
        for (Long id : answers) {
            if (id != QuizAnswer.EMPTY_ANSWER_ID) c++;
        }
        return c;
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
