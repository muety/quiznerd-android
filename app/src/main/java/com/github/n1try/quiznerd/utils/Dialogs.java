package com.github.n1try.quiznerd.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizResult;

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

    public static void showQuitAppDialog(Context context, DialogInterface.OnClickListener onYes) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.quit_app);
        dialog.setMessage(R.string.quit_app_confirm);

        dialog.setPositiveButton(R.string.quit, onYes);

        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.create().show();
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

    public static void showQuestionDialog(Context context, QuizQuestion question, int userAnswerId, int opponentAnswerId) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_question);

        TextView titleTv = dialog.findViewById(R.id.dialog_title_tv);
        TextView questionTv = dialog.findViewById(R.id.dialog_question_tv);
        TextView codeTv = dialog.findViewById(R.id.dialog_code_tv);
        TextView answersTv = dialog.findViewById(R.id.dialog_answers_tv);
        TextView yourAnswerTv = dialog.findViewById(R.id.dialog_your_answer_tv);
        TextView opponentAnswerTv = dialog.findViewById(R.id.dialog_opponent_answser_tv);
        TextView correctAnswerTv = dialog.findViewById(R.id.dialog_correct_answser_tv);

        if (TextUtils.isEmpty(question.getCode())) {
            codeTv.setVisibility(View.GONE);
        }

        char[] alphas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        StringBuilder answers = new StringBuilder();
        for (int i = 0; i < question.getAnswers().size(); i++) {
            answers.append(alphas[i])
                    .append(": ")
                    .append(question.getAnswer(i).getText())
                    .append("\n");
        }

        titleTv.setText(context.getString(R.string.question_template, question.getId()));
        questionTv.setText(question.getText());
        codeTv.setText(Html.fromHtml(question.getCode()));
        answersTv.setText(answers.toString());
        correctAnswerTv.setText(question.getCorrectAnswer().getText());
        yourAnswerTv.setText(question.getAnswer(userAnswerId).getText());
        opponentAnswerTv.setText(question.getAnswer(opponentAnswerId).getText());

        dialog.show();
    }
}
