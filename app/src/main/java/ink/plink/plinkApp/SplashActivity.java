package ink.plink.plinkApp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SplashActivity extends AppCompatActivity implements DownloadCallback<String> {

    public static final String KEY_SIGN_OUT = "Sign Out Key";
    private static final String TAG = "SplashActivity";

    // Views to crossfade
    private View mainLogo;
    private View notifyUser;
    private View continueButton;
    private View cancelButton;
    private TextView userInfoText;
    private ProgressBar progressBarHorizontal;
    private int systemShortAnimTime;

    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount userAccount;

    private final int RC_SIGN_IN = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mainLogo = findViewById(R.id.imageView2);
        notifyUser = findViewById(R.id.notifyUser);
        userInfoText = findViewById(R.id.userInfoText);
        continueButton = findViewById(R.id.continueButton);
        cancelButton = findViewById(R.id.cancelButton);
        progressBarHorizontal = findViewById(R.id.determinateBar);
        progressBarHorizontal.setVisibility(View.GONE);
        signInButton = findViewById(R.id.sign_in_button_splash);

        systemShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.backend_server_id))
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        if (getIntent().getExtras() != null) {
            Bundle args = getIntent().getExtras();
            if (args.getParcelable(KEY_SIGN_OUT) != null) {
                GoogleSignInAccount gsa = args.getParcelable(KEY_SIGN_OUT);
                final String email = gsa.getEmail();
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getBaseContext(), "Logged out of "+ email, Toast.LENGTH_LONG).show();
                            }
                        });

            }
        } else {
            // Check for existing Google Sign In account, if the user is already signed in
            // the GoogleSignInAccount will be non-null.
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            handleTokenSignIn(account, true);
        }

    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //signInButton.animate().setStartDelay(500).alpha(1.0f).setDuration(1000).start();
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTokenSignIn(userAccount, false);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crossfadeView(false);
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            handleTokenSignIn(account, true);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
            updateUI(null,false);
        }
    }

    private void updateUI(GoogleSignInAccount account, boolean isRegistered) {
        if (account != null) {
            if (isRegistered) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(MainActivity.KEY_USER_ACCOUNT, account);
                startActivity(intent);
                finish();
            } else {
                userInfoText.setText(getString(R.string.userInfoTextString, account.getDisplayName(), account.getEmail()));
                crossfadeView(true);
            }
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleTokenSignIn(GoogleSignInAccount account, boolean isInDatabase) {
        if (account != null) {
            if (isInDatabase) {
                userAccount = account;
                NetworkFragment signInFragment = NetworkFragment.getTokenSigninInstance(getSupportFragmentManager(), account.getIdToken());
                signInFragment.startDownload();
            } else {
                NetworkFragment.getTokenDatabaseSigninInstance(getSupportFragmentManager(), account.getIdToken()).startDownload();
            }
        }
    }

    @Override
    public void updateFromDownload(String result) {
        if (result != null) {
            if (result.contains(NetworkFragment.HTTP_UNAUTHORIZED)) {
                updateUI(null, false);
            } else if (result.contains(NetworkFragment.HTTP_NOT_FOUND)) {
                updateUI(userAccount, false);
            } else if (result.contains(NetworkFragment.HTTP_OK)) {
                updateUI(userAccount, true);
            } else if (result.contains(NetworkFragment.HTTP_SERVER_ERROR)) {
                progressBarHorizontal.setProgress(0);
                new AlertDialog.Builder(this).setMessage("The Plink Service is experiencing difficulty")
                        .setTitle("Error")
                        .setPositiveButton("OK", null)
                        .create()
                        .show();
            }
        }
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(final int progressCode, final int percentComplete) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(progressCode) {
                    case Progress.ERROR:
                        progressBarHorizontal.setProgress(percentComplete);
                        break;
                    case Progress.CONNECT_SUCCESS:
                        progressBarHorizontal.setVisibility(View.VISIBLE);
                        progressBarHorizontal.setProgress(percentComplete);
                        break;
                    case Progress.GET_INPUT_STREAM_SUCCESS:
                        progressBarHorizontal.setProgress(percentComplete);
                        break;
                    case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                        progressBarHorizontal.setProgress(percentComplete);
                        break;
                    case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                        progressBarHorizontal.setProgress(percentComplete);
                        break;
                }
            }
        });
    }

    @Override
    public void finishDownloading() {

    }

    private void crossfadeView(boolean notifyUserView) {
        if (notifyUserView) {
            notifyUser.setAlpha(0);
            notifyUser.setVisibility(View.VISIBLE);

            notifyUser.animate()
                    .alpha(1f)
                    .setDuration(systemShortAnimTime)
                    .setListener(null);

            signInButton.animate()
                    .alpha(0f)
                    .setDuration(systemShortAnimTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            signInButton.setVisibility(View.GONE);
                        }
                    });

            mainLogo.animate()
                    .alpha(0f)
                    .setDuration(systemShortAnimTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mainLogo.setVisibility(View.GONE);
                        }
                    });
        } else {
            signInButton.setAlpha(0);
            signInButton.setVisibility(View.VISIBLE);

            signInButton.animate()
                    .alpha(1f)
                    .setDuration(systemShortAnimTime)
                    .setListener(null);

            mainLogo.setAlpha(0);
            mainLogo.setVisibility(View.VISIBLE);

            mainLogo.animate()
                    .alpha(1f)
                    .setDuration(systemShortAnimTime)
                    .setListener(null);

            notifyUser.animate()
                    .alpha(0f)
                    .setDuration(systemShortAnimTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            notifyUser.setVisibility(View.GONE);
                        }
                    });
        }
    }
}
