package com.github.n1try.quiznerd.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.R2;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizRound;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.Dialogs;
import com.github.n1try.quiznerd.utils.QuizUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.AllArgsConstructor;
import lombok.Data;

public class QuizRoundAdapter extends ArrayAdapter<QuizRound> {
    @BindView(R2.id.quiz_round_tv)
    TextView mRoundTv;
    @BindView(R2.id.quiz_category_iv)
    ImageView mCategoryIv;
    @BindView(R2.id.quiz_questions_lv)
    ListView mQuestionsLv;
    @BindView(R2.id.quiz_progress_tv)
    TextView mProgressTv;

    private final Context context;
    private final QuizUser mUser;
    private final QuizMatch mMatch;
    private final int color;
    private final int colorDark;
    private final int playerIdx;

    public QuizRoundAdapter(@NonNull Context context, @NonNull QuizMatch match, @NonNull QuizUser user) {
        super(context, 0, match.getDisplayRounds(user));
        this.context = context;
        this.mUser = user;
        this.mMatch = match;
        this.color = QuizUtils.getCategoryColorId(context, match.getRounds().get(0).getCategory(), false);
        this.colorDark = QuizUtils.getCategoryColorId(context, match.getRounds().get(0).getCategory(), true);
        this.playerIdx = mMatch.getMyPlayerIndex(mUser);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_quiz_round_card, parent, false);
        }
        ButterKnife.bind(this, convertView);

        final QuizRound round = getItem(position);
        mRoundTv.setText(context.getString(R.string.round_template, String.valueOf(round.getId())));
        mRoundTv.setTextColor(color);
        mCategoryIv.setImageDrawable(QuizUtils.getCategoryIcon(context, round.getCategory()));

        if (mayShowRound(round)) {
            List<QuizRoundQuestion> roundQuestions = new ArrayList<>();
            for (int i = 0; i < round.getQuestions().size(); i++) {
                QuizQuestion q = round.getQuestions().get(i);
                roundQuestions.add(new QuizRoundQuestion(
                        q,
                        round.isQuestionCorrect(i, 1),
                        round.isQuestionCorrect(i, 2),
                        round.hasPlayedRound(1, i),
                        round.hasPlayedRound(2, i)
                ));
            }
            mQuestionsLv.setAdapter(new QuizRoundQuestionAdapter(context, roundQuestions));
            mQuestionsLv.setOnItemClickListener((adapterView, view, i, l) -> Dialogs.showQuestionDialog(context,
                    round.getQuestion(i),
                    round.getAnswersByPlayerIndex(playerIdx).get(i).intValue(),
                    round.getAnswersByPlayerIndex((playerIdx % 2) + 1).get(i).intValue()
            ));
            mQuestionsLv.setVisibility(View.VISIBLE);

            if (round.hasPlayed(playerIdx) && !round.hasPlayed((playerIdx % 2) + 1)) {
                mProgressTv.setVisibility(View.VISIBLE);
                mProgressTv.setText(R.string.progress_waiting_for_opponent);
            } else {
                mProgressTv.setVisibility(View.GONE);
            }
        } else {
            if (mMatch.isMyTurn(mUser)) mProgressTv.setText(R.string.progress_your_turn);
            else mProgressTv.setText(R.string.progress_waiting_for_opponent);
            mQuestionsLv.setVisibility(View.GONE);
            mProgressTv.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    /* User is allowed to see the round's questions if either ...
        - ... the match is over
        - ... the round is over
        - ... it's the opponent's turn and the user has already made her guess
     */
    private boolean mayShowRound(QuizRound round) {
        return !mMatch.isActive() ||
                mMatch.getRound() > round.getId() ||
                (!mMatch.isMyTurn(mUser) && round.countAnswers(mMatch.getMyPlayerIndex(mUser)) >= round.getQuestions().size());
    }

    @Data
    @AllArgsConstructor
    private static class QuizRoundQuestion {
        private QuizQuestion question;
        private boolean correct1;
        private boolean correct2;
        private boolean answered1;
        private boolean answered2;
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

            final ImageView state1Iv = convertView.findViewById(R.id.question_status1_iv);
            final ImageView state2Iv = convertView.findViewById(R.id.question_status2_iv);
            final TextView textTv = convertView.findViewById(R.id.question_text);

            final QuizRoundQuestion question = getItem(position);

            ImageView currentStateIv;
            currentStateIv = mMatch.getMyPlayerIndex(mUser) == 1 ? state1Iv : state2Iv;
            if (question.isCorrect1())
                currentStateIv.setImageDrawable(context.getDrawable(R.drawable.ic_check));
            else if (!question.isAnswered1())
                currentStateIv.setImageDrawable(context.getDrawable(R.drawable.ic_help));
            else currentStateIv.setImageDrawable(context.getDrawable(R.drawable.ic_wrong));

            currentStateIv = mMatch.getMyPlayerIndex(mUser) == 1 ? state2Iv : state1Iv;
            if (question.isCorrect2())
                currentStateIv.setImageDrawable(context.getDrawable(R.drawable.ic_check));
            else if (!question.isAnswered2())
                currentStateIv.setImageDrawable(context.getDrawable(R.drawable.ic_help));
            else currentStateIv.setImageDrawable(context.getDrawable(R.drawable.ic_wrong));

            textTv.setText(question.question.getText());

            return convertView;
        }
    }
}
