package com.github.n1try.quiznerd.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class AndroidUtils {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
