package com.github.n1try.quiznerd.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizRound;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.QuizUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.AllArgsConstructor;
import lombok.Data;

public class QuizRoundAdapter extends ArrayAdapter<QuizRound> {
    @BindView(R.id.quiz_round_tv)
    TextView mRoundTv;
    @BindView(R.id.quiz_category_iv)
    ImageView mCategoryIv;
    @BindView(R.id.quiz_questions_lv)
    ListView mQuestionsLv;
    @BindView(R.id.quiz_progress_tv)
    TextView mProgressTv;

    private Context context;
    private QuizUser mUser;
    private QuizMatch mMatch;
    private int color;

    public QuizRoundAdapter(@NonNull Context context, @NonNull QuizMatch match, @NonNull QuizUser user) {
        super(context, 0, match.getRounds());
        this.context = context;
        this.mUser = user;
        this.mMatch = match;
        this.color = QuizUtils.getCategoryColorId(context, match.getRounds().get(0).getCategory());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_quiz_round_card, parent, false);
        }
        ButterKnife.bind(this, convertView);

        final QuizRound round = getItem(position);
        mRoundTv.setText(context.getString(R.string.round_template, round.getId()));
        mRoundTv.setTextColor(color);
        mCategoryIv.setImageDrawable(QuizUtils.getCategoryIcon(context, round.getCategory()));

        if (!mMatch.isActive() || mMatch.getRound() > round.getId()) {
            List<QuizRoundQuestion> roundQuestions = new ArrayList<>();
            for (int i = 0; i < round.getQuestions().size(); i++) {
                QuizQuestion q = round.getQuestions().get(i);
                roundQuestions.add(new QuizRoundQuestion(q, round.isQuestionCorrect(i, 1), round.isQuestionCorrect(i, 2)));
            }
            mQuestionsLv.setAdapter(new QuizRoundQuestionAdapter(context, roundQuestions));
            mQuestionsLv.setVisibility(View.VISIBLE);
            mProgressTv.setVisibility(View.GONE);
        } else {
            if (mMatch.isMyTurn(mUser)) {
                mProgressTv.setText(R.string.progress_your_turn);
            } else {
                mProgressTv.setText(R.string.progress_waiting_for_opponent);
            }
            mQuestionsLv.setVisibility(View.GONE);
            mProgressTv.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Data
    @AllArgsConstructor
    private class QuizRoundQuestion {
        private QuizQuestion question;
        private boolean correct1;
        private boolean correct2;
    }

    private class QuizRoundQuestionAdapter extends ArrayAdapter<QuizRoundQuestion> {
        public QuizRoundQuestionAdapter(@NonNull Context context, @NonNull List<QuizRoundQuestion> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_round_question, parent, false);
            }

            final ImageView state1Iv = convertView.findViewById(R.id.ingame_question_result_iv);
            final ImageView state2Iv = convertView.findViewById(R.id.question_status2_iv);
            final TextView textTv = convertView.findViewById(R.id.question_text);

            final QuizRoundQuestion question = getItem(position);
            if (question.isCorrect1()) state1Iv.setImageDrawable(context.getDrawable(R.drawable.ic_check));
            else state1Iv.setImageDrawable(context.getDrawable(R.drawable.ic_wrong));
            if (question.isCorrect2()) state2Iv.setImageDrawable(context.getDrawable(R.drawable.ic_check));
            else state2Iv.setImageDrawable(context.getDrawable(R.drawable.ic_wrong));
            textTv.setText(question.question.getText());

            return convertView;
        }
    }
}
