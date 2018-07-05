package com.github.n1try.quiznerd.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizAnswer;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.FirestoreService;
import com.github.n1try.quiznerd.service.QuizCacheService;
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

    private FirestoreService mFirestoreService;
    private QuizCacheService mQuizCache;
    private FragmentManager mFragmentManager;
    private QuizUser mUser;
    private QuizMatch mMatch;
    private int mCurrentQuestionIdx = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_ingame);
        ButterKnife.bind(this);

        mFragmentManager = getSupportFragmentManager();
        mFirestoreService = FirestoreService.getInstance();
        mQuizCache = QuizCacheService.getInstance();
        setSupportActionBar(mToolbar);

        String matchId = getIntent().getStringExtra(Constants.KEY_MATCH_ID);
        mMatch = mQuizCache.matchCache.get(matchId);
        mUser = getIntent().getParcelableExtra(Constants.KEY_ME);

        setTitle(getString(R.string.round_template, mMatch.getRound()));

        mNextButton.setVisibility(View.GONE);
        mNextButton.setOnClickListener(this);

        displayQuestion();
    }

    private void displayQuestion() {
        if (mCurrentQuestionIdx < Constants.NUM_ROUNDS && mCurrentQuestionIdx < mMatch.getCurrentRound().getQuestions().size()) {
            Fragment fragment = IngameQuestionFragment.newInstance(mUser, mMatch.getCurrentRound().getQuestion(mCurrentQuestionIdx), mCurrentQuestionIdx);
            mFragmentManager.beginTransaction().replace(R.id.ingame_question_container, fragment, TAG_QUESTION_FRAGMENT).commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onAnswered(QuizAnswer answer) {
        mNextButton.setVisibility(View.VISIBLE);
        mMatch.getCurrentRound().answerQuestionAt(mCurrentQuestionIdx, answer, mMatch.getMyPlayerIndex(mUser));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ingame_next_fab:
                mQuizCache.matchCache.put(mMatch.getId(), mMatch);
                mFirestoreService.updateQuizRounds(mMatch);
                mCurrentQuestionIdx++;
                displayQuestion();
                break;
        }
    }
}
