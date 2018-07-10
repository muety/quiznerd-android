package com.github.n1try.quiznerd.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.model.QuizCategory;
import com.github.n1try.quiznerd.utils.QuizUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuizCategoryAdapter extends ArrayAdapter<QuizCategory> {
    @BindView(R.id.quiz_category_tv)
    TextView mCategoryTv;
    @BindView(R.id.quiz_category_iv)
    ImageView mCategoryIv;

    private final Context context;

    public QuizCategoryAdapter(@NonNull Context context, @NonNull QuizCategory[] objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_category, parent, false);
        }
        ButterKnife.bind(this, convertView);

        final QuizCategory category = getItem(position);
        mCategoryIv.setImageDrawable(QuizUtils.getCategoryIcon(context, category));
        mCategoryTv.setText(category.getDisplayName());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
