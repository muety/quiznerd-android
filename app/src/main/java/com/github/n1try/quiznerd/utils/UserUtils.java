package com.github.n1try.quiznerd.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pixplicity.sharp.Sharp;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserUtils {
    public static final String AVATAR_URL_TEMPLATE = "https://avatars.dicebear.com/v2/%s/%s.svg";

    public static void loadUserAvatar(Context context, final QuizUser user, final ImageView target) {
        OkHttpClient httpClient = AndroidUtils.createOrGetHttpClient(context);
        new FetchAvatarTask(httpClient, user, target, context.getDrawable(R.drawable.ic_unknown_user)).execute();
    }

    public static void serializeToPreferences(Context context, QuizUser user) {
        Gson gson = new GsonBuilder().create();
        SharedPreferences prefs = context.getSharedPreferences(Constants.KEY_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.KEY_ME, gson.toJson(user)).commit();
    }

    public static QuizUser deserializeFromPreferences(Context context) {
        Gson gson = new GsonBuilder().create();
        SharedPreferences prefs = context.getSharedPreferences(Constants.KEY_PREFERENCES, Context.MODE_PRIVATE);
        try {
            return gson.fromJson(prefs.getString(Constants.KEY_ME, null), QuizUser.class);
        } catch (Exception e) {
            return null;
        }
    }
}

class FetchAvatarTask extends AsyncTask<Void, Void, Void> {
    private final OkHttpClient httpClient;
    private final QuizUser user;
    private final ImageView target;
    private final Drawable fallback;

    public FetchAvatarTask(OkHttpClient httpClient, QuizUser user, ImageView target, Drawable fallback) {
        this.httpClient = httpClient;
        this.user = user;
        this.target = target;
        this.fallback = fallback;
    }

    @Override
    protected Void doInBackground(Void... objects) {
        try {
            OkHttpClient httpClient = this.httpClient;
            String userNick = user.getId().replace(" ", "").toLowerCase();

            Request request = new Request.Builder()
                    .url(String.format(
                            UserUtils.AVATAR_URL_TEMPLATE, user.getGender().name().toLowerCase(), userNick
                    ))
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful() && response.body().contentLength() > 0) {
                InputStream stream = response.body().byteStream();
                Sharp.loadInputStream(stream).into(target);
                stream.close();
            } else {
                target.setImageDrawable(fallback);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}