package com.github.n1try.quiznerd.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.UserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class QuizUserAdapter extends ArrayAdapter<QuizUser> {
    @BindView(R.id.quiz_avatar_iv)
    CircleImageView avatarIv;
    @BindView(R.id.quiz_username_tv)
    TextView usernameTv;

    private Context context;

    public QuizUserAdapter(@NonNull Context context, @NonNull QuizUser[] objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }
        ButterKnife.bind(this, convertView);

        final QuizUser user = getItem(position);
        usernameTv.setText(user.getDisplayName());
        UserUtils.loadUserAvatar(context, user, avatarIv);

        return convertView;
    }
}
