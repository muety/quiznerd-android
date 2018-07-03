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
import com.github.n1try.quiznerd.model.QuizRound;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.QuizUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuizRoundAdapter extends ArrayAdapter<QuizRound> {
    @BindView(R.id.quiz_round_tv)
    TextView mRoundTv;
    @BindView(R.id.quiz_category_iv)
    ImageView mCategoryIv;
    @BindView(R.id.quiz_questions_lv)
    ListView mQuestionsLv;

    private Context context;
    private QuizUser mUser;
    private int color;

    public QuizRoundAdapter(@NonNull Context context, @NonNull List<QuizRound> objects, @NonNull QuizUser user) {
        super(context, 0, objects);
        this.context = context;
        this.mUser = user;
        this.color = QuizUtils.getCategoryColorId(context, objects.get(0).getCategory());
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

        return convertView;
    }
}
