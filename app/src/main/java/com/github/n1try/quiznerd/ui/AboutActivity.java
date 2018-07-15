package com.github.n1try.quiznerd.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.utils.AndroidUtils;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AboutActivity";

    private Context context;

    @BindView(R.id.about_text_tv)
    TextView mTextTv;
    @BindView(R.id.new_loading_spinner)
    ProgressBar mLoadingSpinner;
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
        context = this;

        OkHttpClient httpClient = AndroidUtils.createOrGetHttpClient(this);
        Request request = new Request.Builder().url("https://storage.ferdinand-muetsch.de/quiznerd_about.html").build();
        mLoadingSpinner.setVisibility(View.VISIBLE);
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mLoadingSpinner.setVisibility(View.GONE);
                Log.e(TAG, e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                mLoadingSpinner.setVisibility(View.GONE);
                InputStream stream = response.body().byteStream();
                final String html = AndroidUtils.streamToString(stream);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextTv.setText(Html.fromHtml(html));
                    }
                });
            }
        });
    }
}
