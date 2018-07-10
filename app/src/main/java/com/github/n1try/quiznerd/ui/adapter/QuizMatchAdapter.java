package com.github.n1try.quiznerd.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.QuizUtils;
import com.github.n1try.quiznerd.utils.UserUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class QuizMatchAdapter extends ArrayAdapter<QuizMatch> {
    @BindView(R.id.quiz_category_iv) ImageView categoryIv;
    @BindView(R.id.quiz_avatar_iv) CircleImageView avatarIv;
    @BindView(R.id.quiz_username_tv) TextView usernameTv;
    @BindView(R.id.quiz_round_tv) TextView roundTv;
    @BindView(R.id.quiz_turn_tv) TextView turnTv;

    private Context context;
    private QuizUser mUser;

    public QuizMatchAdapter(@NonNull Context context, List<QuizMatch> objects, QuizUser user) {
        super(context, 0, objects);
        this.context = context;
        this.mUser = user;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_quiz, parent, false);
        }
        ButterKnife.bind(this, convertView);

        final QuizMatch match = getItem(position);
        int[] scores = match.getScores();
        usernameTv.setText(match.getOpponent(mUser).getDisplayName());
        UserUtils.loadUserAvatar(context, match.getOpponent(mUser), avatarIv);
        roundTv.setText(context.getString(R.string.round_with_score_template, match.getRound(), scores[0], scores[1]));
        categoryIv.setImageDrawable(QuizUtils.getCategoryIcon(context, match.getCategory()));
        if (!match.isMyTurn(mUser)) turnTv.setVisibility(View.GONE);

        return convertView;
    }
}
