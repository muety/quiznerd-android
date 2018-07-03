/*
TODO: Swipe to refresh
TODO: Start new matches
TODO: Cloud function to generate new matches
TODO: Play activity
TODO: Start next round
TODO: App icon
TODO: Category-specific dark color
TODO: Ability to change username
TODO: Clean questions
TODO: Import questions
 */

package com.github.n1try.quiznerd.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.FirestoreService;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.UserUtils;
import com.google.common.base.Stopwatch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private FirebaseUser mAuthentication;
    private QuizUser mUser;
    private FirestoreService mFirestore;
    private List<QuizMatch> mMatches;
    private QuizMatchAdapter mMatchAdapter;

    @BindView(R.id.main_container)
    View mMainContainer;
    @BindView(R.id.main_loading_container)
    View mLoadingContainer;
    @BindView(R.id.main_avatar_iv)
    ImageView mAvatarIv;
    @BindView(R.id.main_username_tv)
    TextView mUsernameTv;
    @BindView(R.id.main_score_tv)
    TextView mScoreTv;
    @BindView(R.id.main_new_fab)
    FloatingActionButton mNewQuizFab;
    @BindView(R.id.main_quiz_lv)
    ListView mQuizList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuthentication = FirebaseAuth.getInstance().getCurrentUser();
        mFirestore = FirestoreService.getInstance();

        mQuizList.setOnItemClickListener(this);

        setReady(false);
        new FetchDataTask().execute();
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
    public void onBackPressed() {
    }

    private void setReady(boolean ready) {
        if (ready) {
            mMainContainer.setVisibility(View.VISIBLE);
            mLoadingContainer.setVisibility(View.GONE);
        } else {
            mMainContainer.setVisibility(View.GONE);
            mLoadingContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        QuizMatch match = mMatches.get(i);
        Intent intent = new Intent(this, QuizDetailsActivity.class);
        intent.putExtra(Constants.KEY_ME, mUser);
        intent.putExtra(Constants.KEY_MATCH, match);
        startActivity(intent);
    }

    class FetchDataTask extends AsyncTask<Void, Void, Void> implements FirestoreService.FirestoreCallbacks {
        private final CountDownLatch latch;
        private List<QuizUser> users;
        private List<QuizMatch> matches;
        private Context context;
        private Stopwatch stopwatch;

        public FetchDataTask() {
            context = getApplicationContext();
            latch = new CountDownLatch(2);
            stopwatch = Stopwatch.createUnstarted();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            stopwatch.start();
            mFirestore.fetchUserByAuthentication(mAuthentication.getUid(), this);
            mFirestore.fetchActiveMatches(this);
            try {
                latch.await();
            } catch (InterruptedException e) {
                onError(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (this.users != null &&
                    this.matches != null &&
                    this.users.size() == 1 &&
                    this.users.get(0).getAuthentication().equals(mAuthentication.getUid())) {
                mUser = this.users.get(0);
                String winRatio = getString(R.string.score_template, UserUtils.getUserScore(context, mUser));
                mUsernameTv.setText(mAuthentication.getDisplayName());
                mScoreTv.setText(winRatio);
                UserUtils.loadUserAvatar(context, mUser, mAvatarIv);

                mMatches = this.matches;
                mMatchAdapter = new QuizMatchAdapter(context, mMatches, mUser);
                mQuizList.setAdapter(mMatchAdapter);
            } else {
                onError(new Resources.NotFoundException());
            }

            stopwatch.stop();
            setReady(true);
        }

        @Override
        public void onMatchesFetched(List<QuizMatch> matches) {
            Log.i(TAG, String.format("%s matches fetched after %s ms.", matches.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            this.matches = matches;
            latch.countDown();
        }

        @Override
        public void onUsersFetched(List<QuizUser> users) {
            Log.i(TAG, String.format("Own user fetched. Found %s after %s ms.", users.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            this.users = users;
            latch.countDown();
        }

        @Override
        public void onError(Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, getString(R.string.error_fetch_matches), Toast.LENGTH_SHORT).show();
        }
    }
}
