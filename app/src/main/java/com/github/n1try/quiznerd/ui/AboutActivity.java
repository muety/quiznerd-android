package com.github.n1try.quiznerd.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.utils.AndroidUtils;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AboutActivity";
    @BindView(R.id.about_text_tv)
    TextView mTextTv;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.about);

        try {
            InputStream fileStream = getAssets().open("about.html");
            String html = AndroidUtils.streamToString(fileStream);
            mTextTv.setText(Html.fromHtml(html));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
