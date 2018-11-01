package ncsu.project15.ece484_project15_client;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DownloadCallback<String>, SettingsFragment.OnSettingsInteractionListener,
                        GoogleMapsFragment.OnMapsInteractionListener {

    private static final String TAG_MAIN_MENU_FRAG = "MAIN_MENU_FRAG";
    public static final String TAG_GOOGLE_MAPS_FRAG = "GOOGLE_MAPS_FRAG";
    public static final String TAG_SETTINGS_FRAG = "SETTINGS_FRAG";
    GoogleMapsFragment mGoogleMapsFragment;

    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;
    private NetworkFragment mNetworkFragmentGET;
    private NetworkFragment mNetworkFragmentPOST;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_new);
        setSupportActionBar(toolbar);

        // Find the DrawerLayout
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        nvDrawer.setNavigationItemSelectedListener(this);

        mGoogleMapsFragment = GoogleMapsFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.flContent, mGoogleMapsFragment, TAG_GOOGLE_MAPS_FRAG)
                .addToBackStack(TAG_GOOGLE_MAPS_FRAG)
                .commit();
        nvDrawer.setCheckedItem(R.id.test_GoogleMap);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_baseline_filter_list_24px));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case android.R.id.home: {
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            }
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fm = getSupportFragmentManager();
        switch (id) {
            case R.id.test_GoogleMap: {
                if (!item.isChecked()) {
                    if (mGoogleMapsFragment != null) {
                        fm.beginTransaction().remove(fm.findFragmentById(R.id.flContent)).commit();
                        fm.beginTransaction().show(mGoogleMapsFragment).commit();
                        mGoogleMapsFragment.setUserVisibleHint(true);
                    } else {
                        fm.beginTransaction().add(R.id.flContent, new GoogleMapsFragment(), TAG_GOOGLE_MAPS_FRAG).commit();
                        mGoogleMapsFragment = (GoogleMapsFragment)fm.findFragmentByTag(TAG_GOOGLE_MAPS_FRAG);
                    }
                    toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_filter_list_24px));
                    item.setChecked(true);
                }
                break;
            }
            case R.id.test_Settings: {
                if (!item.isChecked()) {
                    if (mGoogleMapsFragment.isVisible()) {
                        fm.beginTransaction().hide(mGoogleMapsFragment).commit();
                        mGoogleMapsFragment.setUserVisibleHint(false);
                        fm.beginTransaction().add(R.id.flContent, SettingsFragment.newInstance("1", "2"), TAG_SETTINGS_FRAG).commit();
                    } else {
                        fm.beginTransaction().replace(R.id.flContent, SettingsFragment.newInstance("1", "2"), TAG_SETTINGS_FRAG).commit();
                    }
                    toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_more_vert_24px));
                    item.setChecked(true);
                }
                break;
            }
            case R.id.test_MainMenu: {
                if (!item.isChecked()) {
                    if (mGoogleMapsFragment.isVisible()) {
                        fm.beginTransaction().hide(mGoogleMapsFragment).commit();
                        mGoogleMapsFragment.setUserVisibleHint(false);
                        fm.beginTransaction().add(R.id.flContent, MainMenu.newInstance("1"), TAG_MAIN_MENU_FRAG).commit();
                    } else {
                        fm.beginTransaction().replace(R.id.flContent, MainMenu.newInstance("1"), TAG_SETTINGS_FRAG).commit();
                    }
                    toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_more_vert_24px));
                    item.setChecked(true);
                }

            }
            default: {

                toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_filter_list_24px));
            }
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void startDownload(int btn) {
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

    @Override
    public void onSettingsInteraction(Uri uri) {

    }

    @Override
    public void onMapsInteraction(Uri uri) {

    }
}
