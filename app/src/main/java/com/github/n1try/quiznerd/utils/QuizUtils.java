package com.github.n1try.quiznerd.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizResult;

public class QuizUtils {
    public static Drawable getCategoryIcon(Context context, QuizCategory category) {
        int id = context.getResources().getIdentifier(String.format("ic_cat_%s", category.name().toLowerCase()), "drawable", context.getPackageName());
        return context.getDrawable(id);
    }

    public static int getCategoryColorId(Context context, QuizCategory category) {
        int id = context.getResources().getIdentifier("category" + category.getDisplayName(), "color", context.getPackageName());
        return ContextCompat.getColor(context, id);
    }

    public static void showPostMatchDialog(Context context, QuizResult result) {
        if (result.equals(QuizResult.PENDING)) return;

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        int buttonText = 0;

        switch (result) {
            case WON:
                dialog.setTitle(R.string.dialog_title_won);
                dialog.setMessage(R.string.dialog_message_won);
                buttonText = R.string.dialog_button_won;
                break;
            case LOST:
                dialog.setTitle(R.string.dialog_title_lost);
                dialog.setMessage(R.string.dialog_message_lost);
                buttonText = R.string.dialog_button_lost;
                break;
            case DRAW:
                dialog.setTitle(R.string.dialog_title_draw);
                dialog.setMessage(R.string.dialog_message_draw);
                buttonText = R.string.dialog_button_draw;
                break;
        }

        dialog.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.create().show();
    }
}
