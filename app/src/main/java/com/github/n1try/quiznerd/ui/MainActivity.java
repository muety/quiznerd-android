/*
Must-do's:
TODO: App icon
TODO: Category-specific dark color
TODO: Notifications
TODO: Widget
TODO: Tests
TODO: Set database permissions
TODO: Include license section

Can do's:
TODO: Use live-updates with snapshot listeners to speed up initial loading time and maintain consistency
TODO: Settings section (change username, change gender)
TODO: Find opponent by QR code
TODO: Ability to add questions
TODO: Clean questions
 */

package com.github.n1try.quiznerd.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiCallbacks;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.ui.adapter.QuizMatchAdapter;
import com.github.n1try.quiznerd.ui.adapter.entity.ListItem;
import com.github.n1try.quiznerd.ui.adapter.entity.QuizMatchListItem;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.QuizUtils;
import com.github.n1try.quiznerd.utils.UserUtils;
import com.google.common.base.Stopwatch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private FirebaseUser mAuthentication;
    private QuizUser mUser;
    private QuizApiService mApiService;
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
    @BindView(R.id.main_refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuthentication = FirebaseAuth.getInstance().getCurrentUser();
        mApiService = QuizApiService.getInstance();

        mQuizList.setOnItemClickListener(this);
        mNewQuizFab.setOnClickListener(this);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                init();
            }
        });

        setReady(false); // Only show loading overlay initially, not on refresh
        init();
    }

    private void init() {
        new FetchDataTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        postMatchesLoad();
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
            mRefreshLayout.setRefreshing(false);
        } else {
            mMainContainer.setVisibility(View.GONE);
            mLoadingContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ListItem item = (ListItem) adapterView.getItemAtPosition(i);
        if (item instanceof QuizMatchListItem) {
            QuizMatch match = ((QuizMatchListItem) item).getMatch();
            Intent intent = new Intent(this, QuizDetailsActivity.class);
            intent.putExtra(Constants.KEY_ME, mUser);
            intent.putExtra(Constants.KEY_MATCH_ID, match.getId());
            startActivity(intent);
        }
    }

    private void postMatchesLoad() {
        mMatches = new ArrayList<>(mApiService.matchCache.values());
        mMatchAdapter = new QuizMatchAdapter(this, QuizMatchAdapter.generateItemList(this, mMatches, mUser));
        mQuizList.setAdapter(mMatchAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_new_fab:
                Intent intent = new Intent(this, NewGameActivity.class);
                intent.putExtra(Constants.KEY_ME, mUser);
                startActivity(intent);
                break;
        }
    }

    class FetchDataTask extends AsyncTask<Void, Void, Void> implements QuizApiCallbacks {
        private final CountDownLatch latch;
        private List<QuizUser> users;
        private List<QuizMatch> matches;
        private Context context;
        private Stopwatch stopwatch;

        public FetchDataTask() {
            context = getApplicationContext();
            latch = new CountDownLatch(3);
            stopwatch = Stopwatch.createUnstarted();
            matches = Collections.synchronizedList(new ArrayList<QuizMatch>());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            stopwatch.start();
            mApiService.fetchUserByAuthentication(mAuthentication.getUid(), this);
            mApiService.fetchActiveMatches(this);
            mApiService.fetchPastMatches(this);
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
                String winRatio = getString(R.string.score_template, (QuizUtils.getWinRatio(matches, mUser) * 100));
                mUsernameTv.setText(mAuthentication.getDisplayName());
                mScoreTv.setText(winRatio);
                UserUtils.loadUserAvatar(context, mUser, mAvatarIv);

                postMatchesLoad();
            } else {
                onError(new Resources.NotFoundException());
            }

            stopwatch.stop();
            setReady(true);
        }

        @Override
        public void onMatchesFetched(List<QuizMatch> matches) {
            Log.d(TAG, String.format("%s matches fetched after %s ms.", matches.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            this.matches.addAll(matches);
            latch.countDown();
        }

        @Override
        public void onUsersFetched(List<QuizUser> users) {
            Log.d(TAG, String.format("Own user fetched. Found %s after %s ms.", users.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            this.users = users;
            latch.countDown();
        }

        @Override
        public void onRandomQuestionsFetched(List<QuizQuestion> questions) {
        }

        @Override
        public void onMatchCreated(QuizMatch match) {
        }

        @Override
        public void onError(Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, getString(R.string.error_fetch_matches), Toast.LENGTH_SHORT).show();
        }
    }
}
