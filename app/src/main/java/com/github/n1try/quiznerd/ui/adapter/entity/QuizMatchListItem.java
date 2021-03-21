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
import lombok.Getter;

@Getter
public class QuizMatchListItem implements ListItem {
    private final Context context;
    private final QuizUser user;
    private final QuizMatch match;

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
        TextView roundTv = convertView.findViewById(R.id.quiz_score_round_tv);
        TextView scoreTv = convertView.findViewById(R.id.quiz_score_tv);
        TextView turnTv = convertView.findViewById(R.id.quiz_turn_tv);
        ImageView resultIv = convertView.findViewById(R.id.quiz_result_iv);

        int[] scores = match.getSortedScores(user);
        usernameTv.setText(match.getOpponent(user).getId());
        UserUtils.loadUserAvatar(context, match.getOpponent(user), avatarIv);
        roundTv.setText(context.getString(R.string.round_with_score_template, String.valueOf(match.getRound()), String.valueOf(scores[0]), String.valueOf(scores[1])));
        scoreTv.setText(context.getString(R.string.score_no_round_template, String.valueOf(scores[0]), String.valueOf(scores[1])));
        if (match.isActive()) {
            roundTv.setVisibility(View.VISIBLE);
            scoreTv.setVisibility(View.GONE);
            resultIv.setVisibility(View.GONE);
        } else {
            roundTv.setVisibility(View.GONE);
            scoreTv.setVisibility(View.VISIBLE);
            resultIv.setVisibility(View.VISIBLE);
            switch (match.getResult(user)) {
                case WON:
                    resultIv.setImageDrawable(context.getDrawable(R.drawable.ic_exposure_plus_1));
                    break;
                case LOST:
                    resultIv.setImageDrawable(context.getDrawable(R.drawable.ic_exposure_neg_1));
                    break;
                default:
                    resultIv.setImageDrawable(context.getDrawable(R.drawable.ic_exposure_zero));
            }
        }
        categoryIv.setImageDrawable(QuizUtils.getCategoryIcon(context, match.getCategory()));
        if (!match.isMyTurn(user)) turnTv.setVisibility(View.GONE);
        else turnTv.setVisibility(View.VISIBLE);

        return convertView;
    }
}