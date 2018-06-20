package com.github.n1try.quiznerd.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.service.FirestoreService;
import com.github.n1try.quiznerd.utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
    private static final String TAG = "MainActivity";
    FirebaseUser mUser;

    @BindView(R.id.main_avatar_iv) ImageView mAvatarIv;
    @BindView(R.id.main_username_tv) TextView mUsernameTv;
    @BindView(R.id.main_score_tv) TextView mScoreTv;
    @BindView(R.id.main_new_fab) FloatingActionButton mNewQuizFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        String winRatio = getString(R.string.score_template, UserUtils.getUserScore(this, mUser));
        mUsernameTv.setText(mUser.getDisplayName());
        mScoreTv.setText(winRatio);

        UserUtils.loadUserAvatar(this, mUser, mAvatarIv);
        FirestoreService.getInstance().fetchMatches();
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
}
