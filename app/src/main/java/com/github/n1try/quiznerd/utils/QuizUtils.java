package com.github.n1try.quiznerd.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizResult;
import com.github.n1try.quiznerd.model.QuizUser;

import java.util.List;

public class QuizUtils {
    public static int getCategoryIconId(Context context, QuizCategory category) {
        return context.getResources().getIdentifier(String.format("ic_cat_%s", category.name().toLowerCase()), "drawable", context.getPackageName());
    }

    public static Drawable getCategoryIcon(Context context, QuizCategory category) {
        return context.getDrawable(getCategoryIconId(context, category));
    }

    public static int getCategoryColorId(Context context, QuizCategory category, boolean dark) {
        String languageId = category.name().substring(0, 1).toUpperCase() + category.name().substring(1).toLowerCase();
        int id = dark
                ? context.getResources().getIdentifier("category" + languageId + "Dark", "color", context.getPackageName())
                : context.getResources().getIdentifier("category" + languageId, "color", context.getPackageName());
        return ContextCompat.getColor(context, id);
    }

    public static float getWinRatio(List<QuizMatch> matches, QuizUser me) {
        float won = 0f;
        float total = 0f;
        for (QuizMatch m : matches) {
            if (m.isActive()) continue;
            QuizResult result = m.getResult(me);
            if (result.equals(QuizResult.WON)) won++;
            if (!result.equals(QuizResult.PENDING)) total++;
        }
        return total > 0 ? won / total : 0;
    }
}
