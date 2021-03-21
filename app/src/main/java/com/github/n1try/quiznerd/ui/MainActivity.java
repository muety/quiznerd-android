// Launcher icon: https://www.flaticon.com/free-icon/nerd_626580#term=nerd&page=1&position=54

/*
Can do's:
TODO: Tests
TODO: Refactor QuizApiCallbacks
TODO: Fix double questions
TODO: Use live-updates with snapshot listeners to speed up initial loading time and maintain consistency
TODO: Ability to add questions
TODO: Stats
 */

package com.github.n1try.quiznerd.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.R2;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiCallbacks;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.ui.adapter.QuizMatchAdapter;
import com.github.n1try.quiznerd.ui.adapter.entity.ListItem;
import com.github.n1try.quiznerd.ui.adapter.entity.QuizMatchListItem;
import com.github.n1try.quiznerd.utils.AndroidUtils;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.Dialogs;
import com.github.n1try.quiznerd.utils.QuizUtils;
import com.github.n1try.quiznerd.utils.UserUtils;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.base.Stopwatch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private QuizUser mUser;
    private QuizApiService mApiService;
    private QuizMatchAdapter mMatchAdapter;
    private SharedPreferences mPrefs;

    @BindView(R2.id.main_container)
    View mMainContainer;
    @BindView(R2.id.main_loading_container)
    View mLoadingContainer;
    @BindView(R2.id.main_avatar_iv)
    ImageView mAvatarIv;
    @BindView(R2.id.main_username_tv)
    TextView mUsernameTv;
    @BindView(R2.id.main_score_tv)
    TextView mScoreTv;
    @BindView(R2.id.main_no_matches_label)
    TextView mNoMatchesLabelTv;
    @BindView(R2.id.main_new_fab)
    FloatingActionButton mNewQuizFab;
    @BindView(R2.id.main_quiz_lv)
    ListView mQuizList;
    @BindView(R2.id.main_quiz_list_container)
    View mQuizListContainer;
    @BindView(R2.id.main_refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mApiService = QuizApiService.getInstance();
        mPrefs = getSharedPreferences(Constants.KEY_PREFERENCES, MODE_PRIVATE);
        mUser = UserUtils.deserializeFromPreferences(this);

        mQuizList.setOnItemClickListener(this);
        mQuizList.setOnCreateContextMenuListener(this);
        mNewQuizFab.setOnClickListener(this);
        mRefreshLayout.setOnRefreshListener(this::init);
        mRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        setReady(false); // Only show loading overlay initially, not on refresh
        init();
        initMessaging();
    }

    private void init() {
        mNewQuizFab.setVisibility(AndroidUtils.isNetworkConnected(this) ? View.VISIBLE : View.GONE);
        mApiService.matchCache.clear();
        new FetchDataTask().execute();
    }

    private void initMessaging() {
        FirebaseMessaging.getInstance().subscribeToTopic(mUser.getId());
    }

    private void revokeMessaging() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(mUser.getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        postMatchesLoad(mApiService.matchCache.values());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // http://tools.android.com/tips/non-constant-fields
        final int itemId = item.getItemId();
        if (itemId == R.id.menu_refresh) {
            mRefreshLayout.setRefreshing(true);
            mRefreshLayout.post(this::init);
        } else if (itemId == R.id.menu_bug) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setDataAndType(Uri.parse("mailto:"), "text/html");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.mail)});
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_subject));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.bug_body));
            startActivity(Intent.createChooser(intent, getString(R.string.send_mail)));
        } else if (itemId == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (itemId == R.id.menu_licenses) {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.license_title));
            startActivity(new Intent(this, OssLicensesMenuActivity.class));
        } else if (itemId == R.id.menu_logout) {
            mPrefs.edit().remove(Constants.KEY_ME).commit();
            FirebaseAuth.getInstance().signOut();
            mApiService.userCache.clear();
            mApiService.matchCache.clear();
            revokeMessaging();
            startActivity(new Intent(this, StartActivity.class));
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Dialogs.showQuitAppDialog(this, (dialogInterface, i) -> {
            finishAffinity();
            System.exit(0);
        });
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
        if (mRefreshLayout.isRefreshing()) return;
        ListItem item = (ListItem) adapterView.getItemAtPosition(i);
        if (item instanceof QuizMatchListItem) {
            QuizMatch match = ((QuizMatchListItem) item).getMatch();
            Intent intent = new Intent(this, QuizDetailsActivity.class);
            intent.putExtra(Constants.KEY_ME, mUser);
            intent.putExtra(Constants.KEY_MATCH_ID, match.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.main_quiz_lv) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_quiz_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ListItem listItem = mMatchAdapter.getItem(menuInfo.position);
        if (!(listItem instanceof QuizMatchListItem)) return false;
        final QuizMatch selectedMatch = ((QuizMatchListItem) listItem).getMatch();
        final QuizUser opponent = selectedMatch.getOpponent(mUser);

        final int itemId = item.getItemId();
        if (itemId == R.id.menu_delete_match) {
            Dialogs.showDeleteDialog(this, (dialogInterface, i) -> mApiService.deleteMatch(selectedMatch, new QuizApiCallbacks() {
                @Override
                public void onMatchesFetched(List<QuizMatch> matches) {
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
                    mRefreshLayout.setRefreshing(true);
                    mRefreshLayout.post(() -> init());
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getApplicationContext(), R.string.error_delete_match, Toast.LENGTH_LONG).show();
                }
            }));
            return true;
        } else if (itemId == R.id.menu_poke_opponent) {
            Date lastPoked = mApiService.pokeCache.get(opponent);
            long secondsSinceLastPoke = lastPoked == null
                    ? Integer.MAX_VALUE
                    : (new Date().getTime() - lastPoked.getTime()) / 1000;
            if (secondsSinceLastPoke > 60) {
                mApiService.pokeOpponent(selectedMatch, mUser);
                mApiService.pokeCache.put(opponent, new Date());
                Toast.makeText(this, getString(R.string.poked_template, opponent.getId()), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.not_poked_template, opponent.getId()), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void postMatchesLoad(Collection<QuizMatch> matches) {
        if (matches.isEmpty()) {
            mMatchAdapter = null;
            mNoMatchesLabelTv.setVisibility(View.VISIBLE);
        } else {
            mMatchAdapter = new QuizMatchAdapter(this, QuizMatchAdapter.generateItemList(this, new ArrayList<>(matches), mUser));
            mQuizList.setAdapter(mMatchAdapter);
            mNoMatchesLabelTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.main_new_fab) {
            Intent intent = new Intent(this, NewGameActivity.class);
            intent.putExtra(Constants.KEY_ME, mUser);
            startActivity(intent);
        }
    }

    class FetchDataTask extends AsyncTask<Void, Void, Void> implements QuizApiCallbacks {
        private final CountDownLatch latch;
        private final List<QuizMatch> matches;
        private final Context context;
        private final Stopwatch stopwatch;

        public FetchDataTask() {
            context = getApplicationContext();
            latch = new CountDownLatch(2);
            stopwatch = Stopwatch.createUnstarted();
            matches = Collections.synchronizedList(new ArrayList<>());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            stopwatch.start();
            mApiService.fetchActiveMatches(mUser.getId(), this);
            mApiService.fetchPastMatches(mUser.getId(), this);
            try {
                latch.await();
            } catch (InterruptedException e) {
                onError(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String winRatio = getString(R.string.score_template, (QuizUtils.getWinRatio(matches, mUser) * 100));
            mUsernameTv.setText(mUser.getId());
            mScoreTv.setText(winRatio);
            UserUtils.loadUserAvatar(context, mUser, mAvatarIv);
            postMatchesLoad(matches);
            stopwatch.stop();
            setReady(true);
        }

        @Override
        public void onMatchesFetched(List<QuizMatch> matches) {
            this.matches.addAll(matches);
            latch.countDown();
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
            Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }
}
