package com.github.n1try.quiznerd.utils;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.widget.ImageView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.svg.GlideApp;
import com.github.n1try.quiznerd.utils.svg.SvgSoftwareLayerSetter;

public class UserUtils {
    public static final String AVATAR_URL_TEMPLATE = "https://avatars.dicebear.com/v2/%s/%s.svg";

    public static void loadUserAvatar(Context context, QuizUser user, final ImageView target) {
        String userNick = user.getId().replace(" ", "").toLowerCase();

        GlideApp.with(context)
                .as(PictureDrawable.class)
                .dontAnimate()
                .listener(new SvgSoftwareLayerSetter())
                .load(String.format(
                        AVATAR_URL_TEMPLATE, user.getGender().name().toLowerCase(), userNick
                ))
                .error(R.drawable.ic_unknown_user)
                .fallback(R.drawable.ic_unknown_user)
                .into(target);
    }
}
