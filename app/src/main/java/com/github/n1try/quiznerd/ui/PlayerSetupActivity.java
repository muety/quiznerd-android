package com.github.n1try.quiznerd.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.n1try.quiznerd.R;
import com.github.n1try.quiznerd.R2;
import com.github.n1try.quiznerd.model.GenderType;
import com.github.n1try.quiznerd.model.QuizMatch;
import com.github.n1try.quiznerd.model.QuizQuestion;
import com.github.n1try.quiznerd.model.QuizUser;
import com.github.n1try.quiznerd.service.QuizApiCallbacks;
import com.github.n1try.quiznerd.service.QuizApiService;
import com.github.n1try.quiznerd.utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.kohsuke.randname.RandomNameGenerator;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class PlayerSetupActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R2.id.setup_avatar_iv)
    CircleImageView avatarIv;
    @BindView(R2.id.setup_nickname_input)
    EditText nicknameInput;
    @BindView(R2.id.setup_female_button)
    Button femaleToggle;
    @BindView(R2.id.setup_male_button)
    Button maleToggle;
    @BindView(R2.id.setup_female_label)
    TextView femaleLabelTv;
    @BindView(R2.id.setup_male_label)
    TextView maleLabelTv;
    @BindView(R2.id.setup_loading_spinner)
    ProgressBar mLoadingSpinner;
    @BindView(R2.id.toolbar)
    Toolbar toolbar;

    private QuizApiService mApiService;
    private GenderType selectedGender;
    private String selectedNickname;
    private FirebaseUser mAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_setup);
        ButterKnife.bind(this);

        mApiService = QuizApiService.getInstance();
        mAuthentication = FirebaseAuth.getInstance().getCurrentUser();
        RandomNameGenerator mRandomNameGen = new RandomNameGenerator(new Random().nextInt());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        setTitle(R.string.create_profile);

        setGender(GenderType.FEMALE);

        maleToggle.setOnClickListener(this);
        femaleToggle.setOnClickListener(this);
        nicknameInput.addTextChangedListener(new TextWatcher() {
            private Timer timer = new Timer();
            private final long DELAY = 1000;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                if (editable.length() > 0) {
                                    new FetchUserTask().execute();
                                }
                            }
                        }, DELAY
                );
            }
        });
        nicknameInput.setText(mRandomNameGen.next());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!checkReady()) return true;
        getMenuInflater().inflate(R.menu.setup_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create) {
            createUser();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.setup_female_button) {
            setGender(GenderType.FEMALE);
        } else if (id == R.id.setup_male_button) {
            setGender(GenderType.MALE);
        }
    }

    private void setGender(GenderType gender) {
        if (selectedGender != null && selectedGender.equals(gender)) return;

        switch (gender) {
            case FEMALE:
                selectedGender = GenderType.FEMALE;
                femaleLabelTv.setTypeface(null, Typeface.BOLD);
                femaleLabelTv.setTextColor(getResources().getColor(R.color.colorAccent));
                maleLabelTv.setTypeface(null, Typeface.NORMAL);
                maleLabelTv.setTextColor(getResources().getColor(R.color.textColorSecondary));
                break;
            case MALE:
                selectedGender = GenderType.MALE;
                maleLabelTv.setTypeface(null, Typeface.BOLD);
                maleLabelTv.setTextColor(getResources().getColor(R.color.colorAccent));
                femaleLabelTv.setTypeface(null, Typeface.NORMAL);
                femaleLabelTv.setTextColor(getResources().getColor(R.color.textColorSecondary));
                break;
        }
        setReadyState();
    }


    private boolean checkReady() {
        return !TextUtils.isEmpty(selectedNickname) && selectedGender != null;
    }

    private void setReadyState() {
        if (checkReady()) {
            UserUtils.loadUserAvatar(this, QuizUser.builder()
                    .id(nicknameInput.getText().toString())
                    .gender(selectedGender)
                    .build(), avatarIv);
        } else {
            avatarIv.setImageDrawable(getDrawable(R.drawable.ic_unknown_user));
        }
        supportInvalidateOptionsMenu();
    }

    private void createUser() {
        QuizUser newUser = QuizUser.builder()
                .id(selectedNickname)
                .authentication(mAuthentication.getUid())
                .gender(selectedGender)
                .build();

        mApiService.createUser(newUser, new QuizApiCallbacks() {
            @Override
            public void onMatchesFetched(List<QuizMatch> matches) {
            }

            @Override
            public void onUsersFetched(List<QuizUser> users) {
            }

            @Override
            public void onRandomQuestionsFetched(List<QuizQuestion> questions) {
            }

            @Override
            public void onMatchCreated(QuizMatch match) {
            }

            @Override
            public void onUserCreated(QuizUser user) {
                mApiService.userCache.put(user.getId(), user);
                UserUtils.serializeToPreferences(getApplicationContext(), user);
                launchMain();
            }

            @Override
            public void onMatchDeleted(QuizMatch match) {

            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getApplicationContext(), R.string.error_create_user, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void launchMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private class FetchUserTask extends AsyncTask<Void, Void, Void> implements QuizApiCallbacks {
        private final CountDownLatch latch;
        private QuizUser userResult;

        public FetchUserTask() {
            latch = new CountDownLatch(1);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mApiService.getUserById(nicknameInput.getText().toString(), this);
            try {
                latch.await();
            } catch (InterruptedException e) {
                onError(e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            runOnUiThread(() -> mLoadingSpinner.setVisibility(View.VISIBLE));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (userResult == null || userResult.getAuthentication().equals(mAuthentication.getUid())) {
                selectedNickname = nicknameInput.getText().toString();
            } else {
                selectedNickname = "";
            }
            runOnUiThread(() -> {
                mLoadingSpinner.setVisibility(View.GONE);
                setReadyState();
            });
        }

        @Override
        public void onMatchesFetched(List<QuizMatch> matches) {
        }

        @Override
        public void onUsersFetched(List<QuizUser> users) {
            if (!users.isEmpty()) {
                userResult = users.get(0);
            }
            latch.countDown();
        }

        @Override
        public void onRandomQuestionsFetched(List<QuizQuestion> questions) {
        }

        @Override
        public void onMatchCreated(QuizMatch match) {
        }

        @Override
        public void onUserCreated(QuizUser user) {
        }

        @Override
        public void onMatchDeleted(QuizMatch match) {

        }

        @Override
        public void onError(Exception e) {
            latch.countDown();
        }
    }

}
