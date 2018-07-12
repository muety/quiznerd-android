package com.github.n1try.quiznerd.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiCallbacks;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class StartActivity extends AppCompatActivity implements QuizApiCallbacks {
    private static final String TAG = "StartActivity";
    private static final int RC_SIGN_IN = 100;
    private SharedPreferences mPrefs;
    private QuizApiService mApiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = getSharedPreferences(Constants.KEY_PREFERENCES, MODE_PRIVATE);
        mApiService = QuizApiService.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build()
            );

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);

        } else if (!mPrefs.contains(Constants.KEY_USER_NICKNAME)) {
            mApiService.getUserByAuthentication(user.getUid(), this);
        } else {
            launchMain();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN:
                IdpResponse response = IdpResponse.fromResultIntent(data);

                if (resultCode == RESULT_OK) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    mApiService.getUserByAuthentication(user.getUid(), this);
                } else {
                    Log.e(TAG, response.getError().getMessage());
                    Toast.makeText(this, getString(R.string.sign_in_failed), Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void launchMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void launchSetup() {
        Intent intent = new Intent(this, PlayerSetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onMatchesFetched(List<QuizMatch> matches) {}

    @Override
    public void onUsersFetched(List<QuizUser> users) {
        if (!users.isEmpty()) {
            mPrefs.edit().putString(Constants.KEY_USER_NICKNAME, users.get(0).getId()).commit();
            launchMain();
        } else {
            launchSetup();
        }
    }

    @Override
    public void onRandomQuestionsFetched(List<QuizQuestion> questions) {}

    @Override
    public void onMatchCreated(QuizMatch match) {}

    @Override
    public void onUserCreated(QuizUser user) {}

    @Override
    public void onError(Exception e) {
        launchSetup();
    }
}
