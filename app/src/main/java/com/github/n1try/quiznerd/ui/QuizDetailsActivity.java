package com.github.n1try.quiznerd.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.ui.adapter.QuizRoundAdapter;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.QuizUtils;
import com.github.n1try.quiznerd.utils.UserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuizDetailsActivity extends AppCompatActivity {
    @BindView(R.id.details_avatar1_iv)
    ImageView mAvatar1Iv;
    @BindView(R.id.details_avatar2_iv)
    ImageView mAvatar2Iv;
    @BindView(R.id.details_score_tv)
    TextView mScoreTv;
    @BindView(R.id.details_round_card)
    ListView mRoundCardList;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.appbar)
    AppBarLayout mAppbar;
    @BindView(R.id.details_play_button)
    Button mPlayButton;

    private QuizApiService mApiService;
    private QuizUser mUser;
    private QuizMatch mMatch;
    private int color;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_details);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mApiService = QuizApiService.getInstance();

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mMatch = mApiService.matchCache.get(bundle.getString(Constants.KEY_MATCH_ID));
        mUser = bundle.getParcelable(Constants.KEY_ME);

        color = QuizUtils.getCategoryColorId(this, mMatch.getCategory());
        mAppbar.setBackgroundColor(QuizUtils.getCategoryColorId(this, mMatch.getCategory()));
        UserUtils.loadUserAvatar(this, mUser, mAvatar1Iv);
        UserUtils.loadUserAvatar(this, mMatch.getOpponent(mUser), mAvatar2Iv);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMatch = mApiService.matchCache.get(mMatch.getId());

        if (!mMatch.isMyTurn(mUser)) mPlayButton.setVisibility(View.GONE);
        mPlayButton.setBackgroundTintList(ColorStateList.valueOf(color));
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuizDetailsActivity.this, IngameActivity.class);
                intent.putExtra(Constants.KEY_MATCH_ID, mMatch.getId());
                intent.putExtra(Constants.KEY_ME, mUser);
                startActivity(intent);
            }
        });

        /* Own user should always be the left avatar. But player1 is the creator, who is not necessarily the current user himself. */
        if (mMatch.isInitiator(mUser)) {
            mScoreTv.setText(getString(R.string.round_score_template, mMatch.getScores()[0], mMatch.getScores()[1]));
        } else {
            mScoreTv.setText(getString(R.string.round_score_template, mMatch.getScores()[1], mMatch.getScores()[0]));
        }

        QuizRoundAdapter roundAdapter = new QuizRoundAdapter(this, mMatch, mUser);
        mRoundCardList.setAdapter(roundAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.KEY_MATCH_ID, mMatch.getId());
        outState.putParcelable(Constants.KEY_ME, mUser);
    }
}
