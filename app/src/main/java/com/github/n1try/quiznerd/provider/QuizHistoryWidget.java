package com.github.n1try.quiznerd.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiCallbacks;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.ui.MainActivity;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.QuizUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class QuizHistoryWidget extends AppWidgetProvider implements QuizApiCallbacks {
    private static final String TAG = "QuizHistoryWidget";
    private RemoteViews views;
    private Context context;
    private QuizUser me;
    private AppWidgetManager appWidgetManager;
    private int[] appWidgetIds;
    private PendingIntent startActivityIntent;

    private void update(Context context, AppWidgetManager appWidgetManager) {
        this.views = new RemoteViews(context.getPackageName(), R.layout.widget_quiz_history);
        this.context = context;
        this.appWidgetManager = appWidgetManager;

        Intent intent = new Intent(context, MainActivity.class);
        startActivityIntent = PendingIntent.getActivity(context, 0, intent, 0);

        QuizApiService apiService = QuizApiService.getInstance();
        SharedPreferences prefs = context.getSharedPreferences(Constants.KEY_PREFERENCES, Context.MODE_PRIVATE);
        Gson gson = new GsonBuilder().create();
        if (!prefs.contains(Constants.KEY_ME)) {
            displayEmptyView();
            applyAll();
        } else {
            this.me = gson.fromJson(prefs.getString(Constants.KEY_ME, null), QuizUser.class);
            apiService.fetchActiveMatches(me.getId(), this);
        }
    }

    private void applyAll() {
        if (appWidgetIds == null) return;
        for (int id : appWidgetIds) {
            appWidgetManager.updateAppWidget(id, views);
        }
    }

    private void displayEmptyView() {
        views.setViewVisibility(R.id.match_no_matches_tv, View.VISIBLE);
        views.setViewVisibility(R.id.match_logo_iv, View.VISIBLE);
        views.setOnClickPendingIntent(R.id.match_no_matches_tv, startActivityIntent);
        views.setOnClickPendingIntent(R.id.match_logo_iv, startActivityIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.appWidgetIds = appWidgetIds;
        update(context, appWidgetManager);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onMatchesFetched(List<QuizMatch> matches) {
        if (!matches.isEmpty()) {
            matches = new ArrayList<>(matches); // Clone
            views.setViewVisibility(R.id.match_no_matches_tv, View.GONE);
            views.setViewVisibility(R.id.match_logo_iv, View.GONE);
            Collections.sort(matches);

            for (int i = 0; i < 3; i++) {
                int containerId = context.getResources().getIdentifier(String.format("match_container_%s", String.valueOf(i + 1)), "id", context.getPackageName());
                int categoryIvId = context.getResources().getIdentifier(String.format("match_category_iv_%s", String.valueOf(i + 1)), "id", context.getPackageName());
                int usernameTvId = context.getResources().getIdentifier(String.format("match_username_tv_%s", String.valueOf(i + 1)), "id", context.getPackageName());
                int scoreTvId = context.getResources().getIdentifier(String.format("match_score_tv_%s", String.valueOf(i + 1)), "id", context.getPackageName());

                if (matches.size() > i) {
                    QuizMatch m = matches.get(i);
                    int[] scores = m.getSortedScores(me);
                    views.setOnClickPendingIntent(containerId, startActivityIntent);
                    views.setViewVisibility(containerId, View.VISIBLE);
                    views.setImageViewResource(categoryIvId, QuizUtils.getCategoryIconId(context, m.getCategory()));
                    views.setTextViewText(usernameTvId, m.getOpponent(me).getId());
                    views.setTextViewText(scoreTvId, context.getString(R.string.score_short_template, scores[0], scores[1]));
                } else {
                    views.setViewVisibility(containerId, View.GONE);
                }
            }
        } else {
            displayEmptyView();
        }
        applyAll();
    }

    @Override
    public void onUsersFetched(List<QuizUser> users) {
    }

    @Override
    public void onRandomQuestionsFetched(List<QuizQuestion> questions) {
    }

    @Override
    public void onMatchCreated(QuizMatch match) {
    }

    @Override
    public void onUserCreated(QuizUser user) {
    }

    @Override
    public void onMatchDeleted(QuizMatch match) {

    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, e.getMessage());
    }
}

