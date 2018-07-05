package com.github.n1try.quiznerd.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizAnswer;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.QuizUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IngameQuestionFragment extends Fragment {
    @BindView(R.id.ingame_question_title_tv)
    TextView mTitleTv;
    @BindView(R.id.ingame_category_iv)
    ImageView mCategoryIv;
    @BindView(R.id.ingame_question_text_tv)
    TextView mTextTv;
    @BindView(R.id.ingame_question_code_tv)
    TextView mCodeTv;
    @BindView(R.id.ingame_question_button_container)
    GridLayout mAnswerButtonGrid;

    private Context mContext;
    private LayoutInflater mInflater;
    private QuizUser mUser;
    private QuizQuestion mQuestion;
    private int mPosition;

    public static IngameQuestionFragment newInstance(QuizUser user, QuizQuestion question, int position) {
        IngameQuestionFragment fragment = new IngameQuestionFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_ME, user);
        bundle.putParcelable(Constants.KEY_QUESTION, question);
        bundle.putInt(Constants.KEY_POSITION, position);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = getArguments().getParcelable(Constants.KEY_ME);
        mQuestion = getArguments().getParcelable(Constants.KEY_QUESTION);
        mPosition = getArguments().getInt(Constants.KEY_POSITION);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingame_question, container, false);
        mInflater = inflater;
        ButterKnife.bind(this, view);

        mTitleTv.setText(mContext.getString(R.string.question_template, String.valueOf(mPosition + 1)));
        mTextTv.setText(mQuestion.getText());
        mCategoryIv.setImageDrawable(QuizUtils.getCategoryIcon(mContext, mQuestion.getCategory()));
        if (TextUtils.isEmpty(mQuestion.getCode())) {
            mCodeTv.setVisibility(View.GONE);
        } else {
            mCodeTv.setText(mQuestion.getCode());
            mCodeTv.setVisibility(View.VISIBLE);
        }

        for (QuizAnswer answer : mQuestion.getAnswers()) {
            mAnswerButtonGrid.addView(inflateAnswerButtons(answer));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private Button inflateAnswerButtons(QuizAnswer answer) {
        Button button = (Button) mInflater.inflate(R.layout.button_answer, null);
        button.setText(answer.getText());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.width = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        button.setLayoutParams(params);
        return button;
    }
}
