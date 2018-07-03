package com.github.n1try.quiznerd.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;
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
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;

    private QuizUser mUser;
    private QuizMatch mMatch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mMatch = getIntent().getParcelableExtra(Constants.KEY_MATCH);
        mUser = getIntent().getParcelableExtra(Constants.KEY_ME);

        appbar.setBackgroundColor(QuizUtils.getCategoryColorId(this, mMatch.getQuizCategory()));

        UserUtils.loadUserAvatar(this, mUser, mAvatar1Iv);
        UserUtils.loadUserAvatar(this, mMatch.getOpponent(mUser), mAvatar2Iv);

        /* Own user should always be the left avatar. But player1 is the creator, who is not necessarily the current user himself. */
        if (mMatch.isInitiator(mUser)) {
            mScoreTv.setText(getString(R.string.round_score_template, mMatch.getScores()[0], mMatch.getScores()[1]));
        } else {
            mScoreTv.setText(getString(R.string.round_score_template, mMatch.getScores()[1], mMatch.getScores()[0]));
        }

        QuizRoundAdapter roundAdapter = new QuizRoundAdapter(this, mMatch.getRounds(), mUser);
        mRoundCardList.setAdapter(roundAdapter);
    }
}
