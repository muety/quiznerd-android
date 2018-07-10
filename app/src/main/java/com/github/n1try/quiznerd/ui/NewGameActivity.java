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
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiCallbacks;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.ui.adapter.QuizCategoryAdapter;
import com.github.n1try.quiznerd.ui.adapter.QuizUserAdapter;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.UserUtils;

import java.util.ArrayList;
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

    private QuizApiService mApiService;
    private QuizUser mUser;
    private QuizUser currentSelectedOpponent;
    private QuizUserAdapter mUserAdapter;

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

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mUser = bundle.getParcelable(Constants.KEY_ME);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentSelectedOpponent == null) return false;
        getMenuInflater().inflate(R.menu.new_match, menu);
        return true;
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
        public void onMatchesFetched(List<QuizMatch> matches) {
        }

        @Override
        public void onUsersFetched(List<QuizUser> users) {
            if (!users.isEmpty()) {
                userResult = users.get(0);
            }
            latch.countDown();
        }

        @Override
        public void onRandomQuestionsFetched(List<QuizQuestion> questions) {
        }

        @Override
        public void onError(Exception e) {
            latch.countDown();
        }
    }
}
