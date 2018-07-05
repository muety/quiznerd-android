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

    private FragmentManager fragmentManager;
    private QuizUser mUser;
    private QuizMatch mMatch;
    private int mCurrentQuestionIdx = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_ingame);
        ButterKnife.bind(this);

        fragmentManager = getSupportFragmentManager();
        setSupportActionBar(mToolbar);

        mMatch = getIntent().getParcelableExtra(Constants.KEY_MATCH);
        mUser = getIntent().getParcelableExtra(Constants.KEY_ME);

        setTitle(getString(R.string.round_template, mMatch.getRound()));

        mNextButton.setVisibility(View.GONE);
        mNextButton.setOnClickListener(this);

        displayQuestion();
    }

    private void displayQuestion() {
        Fragment fragment = IngameQuestionFragment.newInstance(mUser, mMatch.getCurrentRound().getQuestion(mCurrentQuestionIdx), mCurrentQuestionIdx);
        fragmentManager.beginTransaction().replace(R.id.ingame_question_container, fragment, TAG_QUESTION_FRAGMENT).commit();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onAnswered(QuizAnswer answer) {
        mNextButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ingame_next_fab:
                mCurrentQuestionIdx++;
                displayQuestion();
                break;
        }
    }
}
