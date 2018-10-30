package ncsu.project15.ece484_project15_client;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;


public class DownloadActivity extends FragmentActivity implements DownloadCallback<String> {
    
    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;
    private NetworkFragment mNetworkFragmentGET;
    private NetworkFragment mNetworkFragmentPOST;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;
    // Test Button to send HTTP requests
    Button send_request_button;
    // Test Button to send HTTP POST
    Button send_post_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        send_request_button = findViewById(R.id.test_request_button_btn);
        send_post_button = findViewById(R.id.test_post_button_btn);
        mNetworkFragmentGET = NetworkFragment.getInstance(getSupportFragmentManager(), NetworkFragment.URL_GET);
        mNetworkFragmentPOST = NetworkFragment.getInstance(getSupportFragmentManager(), NetworkFragment.URL_POST);
        mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), NetworkFragment.URL_POST);


        send_request_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(send_request_button.getId());
            }
        });

        send_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(send_post_button.getId());
            }
        });
    }

    private void startDownload(int btn) {
        switch (btn) {
            case R.id.test_request_button_btn: {
                if (!mDownloading && mNetworkFragmentGET != null) {
                    // Execute the async download.
                    mNetworkFragmentGET.startDownload();
                    mDownloading = true;
                }
                break;
            }
            case R.id.test_post_button_btn: {
                if (!mDownloading && mNetworkFragmentPOST != null) {
                    // Execute the async download.
                    mNetworkFragmentPOST.startDownload();
                    mDownloading = true;
                }
                break;
            }
        }
    }

    @Override
    public void updateFromDownload(String result) {
        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        Log.i("onProgressUpdate", "arrived");
        switch(progressCode) {

            case Progress.ERROR:
             
                break;
            case Progress.CONNECT_SUCCESS:

                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:

                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:

                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:

                break;
        }
    }

    @Override
    public void finishDownloading() {
        mDownloading = false;
        if (mNetworkFragment != null) {
            mNetworkFragment.cancelDownload();
        }
    }
}
