package com.github.n1try.quiznerd.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.service.FirestoreService;
import com.github.n1try.quiznerd.utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, FirestoreService.FirestoreCallbacks {
    private static final String TAG = "MainActivity";
    private FirebaseUser mUser;
    private FirestoreService mFirestore;
    private List<QuizMatch> activeMatches;
    private QuizMatchAdapter mMatchAdapter;

    @BindView(R.id.main_avatar_iv) ImageView mAvatarIv;
    @BindView(R.id.main_username_tv) TextView mUsernameTv;
    @BindView(R.id.main_score_tv) TextView mScoreTv;
    @BindView(R.id.main_new_fab) FloatingActionButton mNewQuizFab;
    @BindView(R.id.main_quiz_lv) ListView mQuizList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestore = FirestoreService.getInstance();

        String winRatio = getString(R.string.score_template, UserUtils.getUserScore(this, mUser));
        mUsernameTv.setText(mUser.getDisplayName());
        mScoreTv.setText(winRatio);

        UserUtils.loadUserAvatar(this, mUser.getDisplayName(), mAvatarIv);
        mFirestore.fetchMatches(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onMatchesFetched(List<QuizMatch> matches) {
        activeMatches = matches;
        mMatchAdapter = new QuizMatchAdapter(this, activeMatches);
        mQuizList.setAdapter(mMatchAdapter);
    }

    @Override
    public void onError() {
        Toast.makeText(this, getString(R.string.error_fetch_matches), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
    }
}
