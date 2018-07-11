package com.github.n1try.quiznerd.utils;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.widget.ImageView;

import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.svg.GlideApp;
import com.github.n1try.quiznerd.utils.svg.SvgSoftwareLayerSetter;

public class UserUtils {
    public static final String AVATAR_URL_TEMPLATE = "https://avatars.dicebear.com/v2/%s/%s.svg";

    public static void loadUserAvatar(Context context, QuizUser user, ImageView target) {
        String userNick = user.getDisplayName().replace(" ", "").toLowerCase();
        userNick = userNick.substring(0, Math.min(userNick.length() - 1, 10));

        GlideApp.with(context)
                .as(PictureDrawable.class)
                .dontAnimate()
                .listener(new SvgSoftwareLayerSetter())
                .load(String.format(
                        AVATAR_URL_TEMPLATE, user.getGender().name().toLowerCase(), userNick
                ))
                .into(target);
    }
}
