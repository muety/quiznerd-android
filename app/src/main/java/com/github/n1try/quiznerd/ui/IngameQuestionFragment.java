package com.github.n1try.quiznerd.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.R2;
import com.github.n1try.quiznerd.model.QuizAnswer;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.utils.Constants;
import com.github.n1try.quiznerd.utils.QuizUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/* CAUTION: Can only be attached by activities that also implement OnAnsweredListener */
public class IngameQuestionFragment extends Fragment implements QuizCategoryAware {
    @BindView(R2.id.ingame_question_title_tv)
    TextView mTitleTv;
    @BindView(R2.id.ingame_category_iv)
    ImageView mCategoryIv;
    @BindView(R2.id.ingame_question_text_tv)
    TextView mTextTv;
    @BindView(R2.id.ingame_question_code_tv)
    TextView mCodeTv;
    @BindView(R2.id.ingame_question_button_container)
    GridLayout mAnswerButtonGrid;
    @BindView(R2.id.ingame_question_result_iv)
    ImageView mResultIndicator;

    private Context mContext;
    private OnAnsweredListener mAnsweredListener;
    private LayoutInflater mInflater;
    private QuizQuestion mQuestion;
    private int mPosition;
    private List<Button> mAnswerButtons;

    private int colorSuccess;
    private int colorFailed;

    @Override
    public void setColors() {
        int color = QuizUtils.getCategoryColorId(mContext, mQuestion.getCategory(), false);
        mTitleTv.setTextColor(color);
    }

    protected interface OnAnsweredListener {
        void onAnswered(QuizAnswer answer);
    }

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
        mResultIndicator.setVisibility(View.GONE);
        if (TextUtils.isEmpty(mQuestion.getCode())) {
            mCodeTv.setVisibility(View.GONE);
        } else {
            mCodeTv.setText(Html.fromHtml(mQuestion.getCode()));
            mCodeTv.setVisibility(View.VISIBLE);
        }

        mAnswerButtons = new ArrayList<>();
        for (QuizAnswer answer : mQuestion.getAnswers()) {
            Button b = inflateAnswerButtons(answer);
            b.setTag(answer.getId());
            mAnswerButtons.add(b);
            mAnswerButtonGrid.addView(b);
        }

        setColors();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mAnsweredListener = (OnAnsweredListener) context;

        colorSuccess = ContextCompat.getColor(context, R.color.success);
        colorFailed = ContextCompat.getColor(context, R.color.danger);
    }

    private Button inflateAnswerButtons(final QuizAnswer answer) {
        Button button = (Button) mInflater.inflate(R.layout.include_button_answer, null);
        button.setText(answer.getText());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.width = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        button.setLayoutParams(params);

        button.setOnClickListener(view -> mAnsweredListener.onAnswered(answer));

        return button;
    }

    public void revealSolution(QuizAnswer userAnswer) {
        if (userAnswer == null || userAnswer.equals(QuizAnswer.TIMEOUT_ANSWER)) {
            Toast.makeText(mContext, R.string.result_time_up, Toast.LENGTH_SHORT).show();
            mResultIndicator.setImageDrawable(mContext.getDrawable(R.drawable.ic_wrong));
            mResultIndicator.setVisibility(View.VISIBLE);
        } else if (userAnswer.isCorrect()) {
            Toast.makeText(mContext, R.string.result_correct, Toast.LENGTH_SHORT).show();
            mResultIndicator.setImageDrawable(mContext.getDrawable(R.drawable.ic_check));
            mResultIndicator.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(mContext, R.string.result_wrong, Toast.LENGTH_SHORT).show();
            mResultIndicator.setImageDrawable(mContext.getDrawable(R.drawable.ic_wrong));
            mResultIndicator.setVisibility(View.VISIBLE);
        }

        for (Button b : mAnswerButtons) {
            int answerId = (int) b.getTag();
            if (answerId == mQuestion.getCorrectAnswer().getId()) {
                b.setBackgroundTintList(ColorStateList.valueOf(colorSuccess));
            }
            if (userAnswer != null && answerId == userAnswer.getId() && !userAnswer.isCorrect()) {
                b.setBackgroundTintList(ColorStateList.valueOf(colorFailed));
            }
            b.setEnabled(false);
        }
    }
}
