package com.github.n1try.quiznerd.ui.adapter.entity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.QuizUtils;
import com.github.n1try.quiznerd.utils.UserUtils;

import de.hdodenhof.circleimageview.CircleImageView;

public class QuizMatchListItem implements ListItem {
    private Context context;
    private QuizUser user;
    private QuizMatch match;

    public QuizMatchListItem(Context context, QuizMatch match, QuizUser user) {
        this.context = context;
        this.match = match;
        this.user = user;
    }

    public QuizMatch getMatch() {
        return match;
    }

    @Override
    public int getViewType() {
        return RowType.LIST_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_quiz, null, false);
        }

        ImageView categoryIv = convertView.findViewById(R.id.quiz_category_iv);
        CircleImageView avatarIv = convertView.findViewById(R.id.quiz_avatar_iv);
        TextView usernameTv = convertView.findViewById(R.id.quiz_username_tv);
        TextView roundTv = convertView.findViewById(R.id.quiz_round_tv);
        TextView turnTv = convertView.findViewById(R.id.quiz_turn_tv);

        int[] scores = match.getScores();
        usernameTv.setText(match.getOpponent(user).getDisplayName());
        UserUtils.loadUserAvatar(context, match.getOpponent(user), avatarIv);
        roundTv.setText(context.getString(R.string.round_with_score_template, match.getRound(), scores[0], scores[1]));
        categoryIv.setImageDrawable(QuizUtils.getCategoryIcon(context, match.getCategory()));
        if (!match.isMyTurn(user)) turnTv.setVisibility(View.GONE);

        return convertView;
    }
}