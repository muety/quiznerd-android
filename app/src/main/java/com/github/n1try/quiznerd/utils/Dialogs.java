package com.github.n1try.quiznerd.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;

public class Dialogs {
    public static void showDeleteDialog(Context context, QuizMatch match, DialogInterface.OnClickListener onYes) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.delete_match);
        dialog.setMessage(R.string.delete_match_confirm);

        dialog.setPositiveButton(R.string.yes, onYes);

        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.create().show();
    }
}
