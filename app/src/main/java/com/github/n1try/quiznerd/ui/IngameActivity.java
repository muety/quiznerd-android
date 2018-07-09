package com.github.n1try.quiznerd.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizAnswer;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.service.QuizRoundManager;
import com.github.n1try.quiznerd.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IngameActivity extends AppCompatActivity implements IngameQuestionFragment.OnAnsweredListener, View.OnClickListener {
    private static final String TAG_QUESTION_FRAGMENT = "question_fragment";

    @BindView(R.id.ingame_question_container)
    View mQuestionContainer;
    @BindView(R.id.ingame_next_fab)
    FloatingActionButton mNextButton;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.appbar)
    AppBarLayout mAppbar;
    @BindView(R.id.ingame_countdown)
    ProgressBar mProgressBar;

    private QuizApiService mApiService;
    private QuizRoundManager mQuizRoundManager;
    private FragmentManager mFragmentManager;
    private IngameQuestionFragment mCurrentFragment;
    private QuizUser mUser;
    private QuizMatch mMatch;
    private CountDownTimer mCountdown;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_ingame);
        ButterKnife.bind(this);

        mApiService = QuizApiService.getInstance();
        mFragmentManager = getSupportFragmentManager();
        mMatch = mApiService.matchCache.get(getIntent().getStringExtra(Constants.KEY_MATCH_ID));
        mUser = getIntent().getParcelableExtra(Constants.KEY_ME);
        mQuizRoundManager = new QuizRoundManager(mMatch, mUser);
        setSupportActionBar(mToolbar);
        setTitle(getString(R.string.round_template, mMatch.getRound()));

        mNextButton.setVisibility(View.GONE);
        mNextButton.setOnClickListener(this);

        mCountdown = new CountDownTimer(getResources().getInteger(R.integer.countdown_millis), 1000) {
            @Override
            public void onTick(long remaining) {
                mProgressBar.setProgress((int) remaining);
            }

            @Override
            public void onFinish() {
                mProgressBar.setProgress(0);
                mCurrentFragment.revealSolution(QuizAnswer.EMPTY_ANSWER);
                mQuizRoundManager.timeout();
            }
        }.start();

        displayQuestion(mQuizRoundManager.playCurrentRound().getCurrentQuestion());
    }

    private void displayQuestion(QuizQuestion question) {
        mCurrentFragment = IngameQuestionFragment.newInstance(mUser, question, mMatch.getCurrentRound().getQuestionIndex(question));
        mFragmentManager.beginTransaction().replace(R.id.ingame_question_container, mCurrentFragment, TAG_QUESTION_FRAGMENT).commit();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onAnswered(QuizAnswer answer) {
        mNextButton.setVisibility(View.VISIBLE);
        mCountdown.cancel();
        mQuizRoundManager.answer(answer);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ingame_next_fab:
                QuizQuestion nextQuestion = mQuizRoundManager.next().getCurrentQuestion();
                if (nextQuestion == null) super.onBackPressed();
                else displayQuestion(nextQuestion);
                break;
        }
    }
}