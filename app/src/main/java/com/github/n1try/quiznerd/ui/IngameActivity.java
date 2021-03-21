package com.github.n1try.quiznerd.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.R2;
import com.github.n1try.quiznerd.model.QuizAnswer;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.service.QuizRoundManager;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.QuizUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IngameActivity extends AppCompatActivity implements IngameQuestionFragment.OnAnsweredListener, View.OnClickListener, QuizCategoryAware {
    private static final String TAG_QUESTION_FRAGMENT = "question_fragment";

    @BindView(R2.id.ingame_question_container)
    View mQuestionContainer;
    @BindView(R2.id.ingame_next_fab)
    FloatingActionButton mNextButton;
    @BindView(R2.id.toolbar)
    Toolbar mToolbar;
    @BindView(R2.id.appbar)
    AppBarLayout mAppbar;
    @BindView(R2.id.ingame_countdown)
    ProgressBar mProgressBar;

    private QuizRoundManager mQuizRoundManager;
    private FragmentManager mFragmentManager;
    private IngameQuestionFragment mCurrentFragment;
    private QuizUser mUser;
    private QuizMatch mMatch;
    private CountDownTimer mCountdown;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingame);
        ButterKnife.bind(this);

        QuizApiService mApiService = QuizApiService.getInstance();
        mFragmentManager = getSupportFragmentManager();

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mMatch = mApiService.matchCache.get(bundle.getString(Constants.KEY_MATCH_ID));
        mUser = bundle.getParcelable(Constants.KEY_ME);

        mQuizRoundManager = new QuizRoundManager(mMatch, mUser);
        setSupportActionBar(mToolbar);
        setTitle(getString(R.string.round_template, String.valueOf(mMatch.getRound())));
        mNextButton.setOnClickListener(this);

        long countdownState = bundle.containsKey(Constants.KEY_COUNTDOWN)
                ? bundle.getLong(Constants.KEY_COUNTDOWN)
                : getResources().getInteger(R.integer.countdown_millis);
        restartCountdown(countdownState);

        displayQuestion(mQuizRoundManager.playCurrentRound().getCurrentQuestion());

        setColors();
    }

    private void restartCountdown(long state) {
        mCountdown = new CountDownTimer(state, 1000) {
            @Override
            public void onTick(long remaining) {
                mProgressBar.setProgress((int) remaining);
            }

            @Override
            public void onFinish() {
                mProgressBar.setProgress(0);
                onAnswered(QuizAnswer.TIMEOUT_ANSWER);
            }
        }.start();
    }

    private void displayQuestion(QuizQuestion question) {
        mNextButton.setVisibility(View.GONE);
        mCurrentFragment = IngameQuestionFragment.newInstance(mUser, question, mMatch.getCurrentRound().getQuestionIndex(question));
        mFragmentManager.beginTransaction().replace(R.id.ingame_question_container, mCurrentFragment, TAG_QUESTION_FRAGMENT).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.KEY_MATCH_ID, mMatch.getId());
        outState.putParcelable(Constants.KEY_ME, mUser);
        outState.putLong(Constants.KEY_COUNTDOWN, mProgressBar.getProgress());
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onAnswered(QuizAnswer answer) {
        mNextButton.setVisibility(View.VISIBLE);
        mCountdown.cancel();
        mQuizRoundManager.answer(answer);
        mQuizRoundManager.next();
        mCurrentFragment.revealSolution(answer);
    }

    @Override
    public void onClick(View view) {
        // http://tools.android.com/tips/non-constant-fields
        if (view.getId() == R.id.ingame_next_fab) {
            QuizQuestion nextQuestion = mQuizRoundManager.getCurrentQuestion();
            if (nextQuestion == null) super.onBackPressed();
            else {
                displayQuestion(nextQuestion);
                restartCountdown(getResources().getInteger(R.integer.countdown_millis));
            }
        }
    }

    @Override
    public void setColors() {
        int color = QuizUtils.getCategoryColorId(this, mMatch.getCategory(), false);
        int colorDark = QuizUtils.getCategoryColorId(this, mMatch.getCategory(), true);
        mAppbar.setBackgroundColor(color);
        getWindow().setStatusBarColor(colorDark);
        mNextButton.setBackgroundTintList(ColorStateList.valueOf(color));
        mProgressBar.setProgressTintList(ColorStateList.valueOf(color));
    }
}
