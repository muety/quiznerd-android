package com.github.n1try.quiznerd.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.github.n1try.quiznerd.model.QuizCategory;

public class QuizUtils {
    public static Drawable getCategoryIcon(Context context, QuizCategory category) {
        int id = context.getResources().getIdentifier(String.format("ic_cat_%s", category.name().toLowerCase()), "drawable", context.getPackageName());
        return context.getDrawable(id);
    }
}
