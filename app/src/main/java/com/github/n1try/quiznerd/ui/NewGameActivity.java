package com.github.n1try.quiznerd.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizRound;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiCallbacks;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.ui.adapter.QuizCategoryAdapter;
import com.github.n1try.quiznerd.ui.adapter.QuizUserAdapter;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.UserUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class NewGameActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "NewGameActivity";

    @BindView(R.id.new_category_spinner)
    Spinner mCategorySpinner;
    @BindView(R.id.new_mail_input)
    EditText mMailInput;
    @BindView(R.id.new_find_opponent_button)
    ImageButton mSearchButton;
    @BindView(R.id.new_friends_label)
    TextView mFriendsLabel;
    @BindView(R.id.new_friends_lv)
    ListView mFriendsList;
    @BindView(R.id.new_opponent_controls_container)
    View opponentControlsContainer;
    @BindView(R.id.new_selected_opponent_container)
    ViewGroup selectedOpponentContainer;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.appbar)
    AppBarLayout mAppbar;

    private Context mContext;
    private QuizApiService mApiService;
    private QuizUser mUser;
    private QuizUser currentSelectedOpponent;
    private QuizUserAdapter mUserAdapter;
    private List<QuizQuestion> mRandomQuestions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_match);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        setTitle(R.string.new_match);

        mApiService = QuizApiService.getInstance();
        mContext = this;

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mUser = bundle.getParcelable(Constants.KEY_ME);
        if (bundle.containsKey(Constants.KEY_RANDOM_QUESTIONS)) {
            mRandomQuestions = bundle.getParcelableArrayList(Constants.KEY_RANDOM_QUESTIONS);
        }

        QuizCategoryAdapter adapter = new QuizCategoryAdapter(this, QuizCategory.values());
        mCategorySpinner.setAdapter(adapter);

        List<QuizUser> previousOpponents = getCachedUsers();
        if (previousOpponents.isEmpty()) {
            mFriendsLabel.setVisibility(View.GONE);
            mFriendsList.setVisibility(View.GONE);
        } else {
            mFriendsLabel.setVisibility(View.VISIBLE);
            mFriendsList.setVisibility(View.VISIBLE);
            mFriendsList.setOnItemClickListener(this);
        }
        mUserAdapter = new QuizUserAdapter(this, previousOpponents.toArray(new QuizUser[]{}));
        mFriendsList.setAdapter(mUserAdapter);
        mMailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSearchButton.setEnabled(Patterns.EMAIL_ADDRESS.matcher(editable.toString()).matches());
            }
        });
        mSearchButton.setEnabled(false);
        mSearchButton.setOnClickListener(this);
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                QuizCategory cat = (QuizCategory) adapterView.getItemAtPosition(i);
                if (mRandomQuestions == null || mRandomQuestions.isEmpty() || !mRandomQuestions.get(0).getCategory().equals(cat)) {
                    new FetchRandomQuestionsTask().execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.KEY_ME, mUser);
        outState.putParcelableArrayList(Constants.KEY_RANDOM_QUESTIONS, new ArrayList<>(mRandomQuestions));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentSelectedOpponent == null
                || mRandomQuestions == null
                || mRandomQuestions.size() < Constants.NUM_ROUNDS * Constants.NUM_QUESTIONS_PER_ROUND) {
            return false;
        }
        getMenuInflater().inflate(R.menu.new_match, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start:
                createMatch();
                break;
        }
        return false;
    }

    private List<QuizUser> getCachedUsers() {
        Map<String, QuizUser> users = new HashMap<>(mApiService.userCache);
        users.remove(mUser.getId());
        return new ArrayList<>(users.values());
    }

    private void showCurrentSelectedOpponent() {
        selectedOpponentContainer.removeAllViews();
        View selectedOpponentView = getLayoutInflater().inflate(R.layout.item_user, selectedOpponentContainer);
        CircleImageView avatarIv = selectedOpponentView.findViewById(R.id.quiz_avatar_iv);
        TextView usernameTv = selectedOpponentView.findViewById(R.id.quiz_username_tv);
        ImageButton clearButton = selectedOpponentView.findViewById(R.id.clear_button);
        UserUtils.loadUserAvatar(this, currentSelectedOpponent, avatarIv);
        usernameTv.setText(currentSelectedOpponent.getDisplayName());
        selectedOpponentContainer.setVisibility(View.VISIBLE);
        opponentControlsContainer.setVisibility(View.GONE);
        clearButton.setVisibility(View.VISIBLE);
        clearButton.setOnClickListener(this);
        supportInvalidateOptionsMenu();
    }

    private void hideCurrentSelectedOpponent() {
        selectedOpponentContainer.setVisibility(View.GONE);
        opponentControlsContainer.setVisibility(View.VISIBLE);
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getAdapter() instanceof QuizUserAdapter) {
            currentSelectedOpponent = (QuizUser) adapterView.getItemAtPosition(i);
            showCurrentSelectedOpponent();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear_button:
                currentSelectedOpponent = null;
                hideCurrentSelectedOpponent();
                break;
            case R.id.new_find_opponent_button:
                new FetchUserTask().execute();
                break;
        }
    }

    private void createMatch() {
        if (currentSelectedOpponent == null) return;
        QuizUser player1 = mUser;
        QuizUser player2 = currentSelectedOpponent;
        QuizCategory category = (QuizCategory) mCategorySpinner.getSelectedItem();
        List<QuizRound> rounds = new ArrayList<>(Constants.NUM_ROUNDS);
        for (int i = 0; i < Constants.NUM_ROUNDS; i++) {
            rounds.add(QuizRound.builder()
                    .id(i + 1)
                    .answers1(QuizRound.DEFAULT_ANSWERS)
                    .answers2(QuizRound.DEFAULT_ANSWERS)
                    .questions(mRandomQuestions.subList(
                            i * Constants.NUM_QUESTIONS_PER_ROUND,
                            i * Constants.NUM_QUESTIONS_PER_ROUND + Constants.NUM_QUESTIONS_PER_ROUND)
                    )
                    .build());
        }

        QuizMatch match = new QuizMatch("", category, 1, true, new Date(), player1, player2, rounds);
        mApiService.createMatch(match, new QuizApiCallbacks() {
            @Override
            public void onMatchesFetched(List<QuizMatch> matches) {}

            @Override
            public void onUsersFetched(List<QuizUser> users) {}

            @Override
            public void onRandomQuestionsFetched(List<QuizQuestion> questions) {}

            @Override
            public void onMatchCreated(QuizMatch match) {
                mApiService.matchCache.put(match.getId(), match);
                onBackPressed();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(mContext, R.string.error_create_match, Toast.LENGTH_LONG).show();
            }
        });
    }

    class FetchUserTask extends AsyncTask<Void, Void, Void> implements QuizApiCallbacks {
        private final CountDownLatch latch;
        private final Context context;
        private QuizUser userResult;

        public FetchUserTask() {
            latch = new CountDownLatch(1);
            context = getApplicationContext();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mApiService.fetchUserByMail(mMailInput.getText().toString(), this);
            try {
                latch.await();
            } catch (InterruptedException e) {
                onError(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (userResult == null || userResult.equals(mUser)) {
                Toast.makeText(context, R.string.user_not_found, Toast.LENGTH_LONG).show();
            } else {
                currentSelectedOpponent = userResult;
                mUserAdapter.notifyDataSetInvalidated();
                showCurrentSelectedOpponent();
            }
        }

        @Override
        public void onMatchesFetched(List<QuizMatch> matches) {}

        @Override
        public void onUsersFetched(List<QuizUser> users) {
            if (!users.isEmpty()) {
                userResult = users.get(0);
            }
            latch.countDown();
        }

        @Override
        public void onRandomQuestionsFetched(List<QuizQuestion> questions) {}

        @Override
        public void onMatchCreated(QuizMatch match) {}

        @Override
        public void onError(Exception e) {
            latch.countDown();
        }
    }

    class FetchRandomQuestionsTask extends AsyncTask<Void, Void, Void> implements QuizApiCallbacks {
        private CountDownLatch latch;
        private Context context;

        public FetchRandomQuestionsTask() {
            latch = new CountDownLatch(Constants.NUM_ROUNDS);
            context = getApplicationContext();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < Constants.NUM_ROUNDS; i++) {
                mApiService.fetchRandomQuestions(Constants.NUM_QUESTIONS_PER_ROUND, (QuizCategory) mCategorySpinner.getSelectedItem(), this);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                onError(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            supportInvalidateOptionsMenu();
        }

        @Override
        public void onMatchesFetched(List<QuizMatch> matches) {}

        @Override
        public void onUsersFetched(List<QuizUser> users) {}

        @Override
        public void onRandomQuestionsFetched(List<QuizQuestion> questions) {
            if (mRandomQuestions == null) {
                mRandomQuestions = questions;
            } else {
                mRandomQuestions.addAll(questions);
            }
            latch.countDown();
        }

        @Override
        public void onMatchCreated(QuizMatch match) {}

        @Override
        public void onError(Exception e) {
            for (int i = 0; i < Constants.NUM_ROUNDS; i++) {
                latch.countDown();
            }
        }
    }
}
