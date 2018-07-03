package com.github.n1try.quiznerd.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.github.n1try.quiznerd.model.QuizCategory;

public class QuizUtils {
    public static Drawable getCategoryIcon(Context context, QuizCategory category) {
        int id = context.getResources().getIdentifier(String.format("ic_cat_%s", category.name().toLowerCase()), "drawable", context.getPackageName());
        return context.getDrawable(id);
    }

    public static int getCategoryColorId(Context context, QuizCategory category) {
        int id = context.getResources().getIdentifier("category" + category.getDisplayName(), "color", context.getPackageName());
        return ContextCompat.getColor(context, id);
    }
}
