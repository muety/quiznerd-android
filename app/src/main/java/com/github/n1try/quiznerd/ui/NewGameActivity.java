package com.github.n1try.quiznerd.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.ui.adapter.QuizCategoryAdapter;
import com.github.n1try.quiznerd.ui.adapter.QuizUserAdapter;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.UserUtils;

import java.util.HashMap;
import java.util.Map;

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

        Map<String, QuizUser> previousOpponents = new HashMap<>();
        for (QuizMatch match : mApiService.matchCache.values()) {
            previousOpponents.put(match.getPlayer1().getId(), match.getPlayer1());
            previousOpponents.put(match.getPlayer2().getId(), match.getPlayer2());
        }
        previousOpponents.remove(mUser.getId());

        if (previousOpponents.isEmpty()) {
            mFriendsLabel.setVisibility(View.GONE);
            mFriendsList.setVisibility(View.GONE);
        } else {
            mFriendsLabel.setVisibility(View.VISIBLE);
            mFriendsList.setVisibility(View.VISIBLE);
            QuizUserAdapter userAdapter = new QuizUserAdapter(this, previousOpponents.values().toArray(new QuizUser[]{}));
            mFriendsList.setAdapter(userAdapter);
            mFriendsList.setOnItemClickListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentSelectedOpponent == null) return false;
        getMenuInflater().inflate(R.menu.new_match, menu);
        return true;
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
        }
    }
}
