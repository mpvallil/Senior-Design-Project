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
import android.support.v4.app.FragmentTransaction;
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
                        GoogleMapsFragment.OnMapsInteractionListener, MainMenu.OnMainMenuInteractionListener {
    /** Fragment references */
    // Fields for naming Fragments from the Nav Menu
    private static final String TAG_MAIN_MENU_FRAG = "MAIN_MENU_FRAG";
    public static final String TAG_GOOGLE_MAPS_FRAG = "GOOGLE_MAPS_FRAG";
    public static final String TAG_SETTINGS_FRAG = "SETTINGS_FRAG";
    // References to Nav Menu Fragments
    GoogleMapsFragment mGoogleMapsFragment;
    SettingsFragment mSettingsFragment;
    MainMenu mMainMenuFragment;
    Fragment currentFragment;
    private FragmentManager fm;
    private final FragmentManager.OnBackStackChangedListener backStackListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            if (!mGoogleMapsFragment.isVisible()) {
                toggle.setDrawerIndicatorEnabled(false);
                toggle.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24px);
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            } else {
                toggle.setDrawerIndicatorEnabled(true);
                toggle.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
                toggle.setToolbarNavigationClickListener(null);
            }
        }
    };

    /** Network Activity fields */
    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;
    private NetworkFragment mNetworkFragmentGET;
    private NetworkFragment mNetworkFragmentPOST;
    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;

    /** Layout fields */
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Toolbar
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // Find the DrawerLayout
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        nvDrawer.setNavigationItemSelectedListener(this);
        fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(backStackListener);

        mGoogleMapsFragment = GoogleMapsFragment.newInstance();
        fm
                .beginTransaction()
                .add(R.id.flContent, mGoogleMapsFragment, TAG_GOOGLE_MAPS_FRAG)
                .commit();
        currentFragment = mGoogleMapsFragment;
        toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_filter_list_24px));

        //Create Network Fragments
        mNetworkFragmentGET = NetworkFragment.getInstance(getSupportFragmentManager(), NetworkFragment.URL_GET);
        mNetworkFragmentPOST = NetworkFragment.getInstance(getSupportFragmentManager(), NetworkFragment.URL_POST);
        mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), NetworkFragment.URL_POST);
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
        toolbar.setBackground(null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return toggle.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        item.setChecked(false);
        int id = item.getItemId();
        Fragment newFragment;
        switch (id) {
            case R.id.nav_drawer_Settings: {
                if (!item.isChecked()) {
                    newFragment = new SettingsFragment();
                    if (currentFragment == mGoogleMapsFragment) {
                        fm.beginTransaction().detach(mGoogleMapsFragment).commit();
                    }
                    fm.beginTransaction().detach(mGoogleMapsFragment).replace(R.id.flContent, newFragment, TAG_SETTINGS_FRAG).addToBackStack(null).commit();

                    toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_more_vert_24px));
                    item.setChecked(true);
                    currentFragment = newFragment;
                }
                break;
            }
            case R.id.nav_drawer_MainMenu: {
                if (!item.isChecked()) {
                    newFragment = new MainMenu();
                    if (currentFragment == mGoogleMapsFragment) {
                        fm.beginTransaction().detach(mGoogleMapsFragment).commit();
                    }
                    fm.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
                            .detach(mGoogleMapsFragment)
                            .replace(R.id.flContent, newFragment, TAG_MAIN_MENU_FRAG)
                            .addToBackStack(null)
                            .commit();

                    toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_more_vert_24px));
                    item.setChecked(true);
                    currentFragment = newFragment;
                }
                break;
            }
            case R.id.nav_drawer_Logout: {


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
            case R.id.test_send_document_button: {
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
    public void onMapsInteraction(Printer printer) {
        startDownload(R.id.test_request_button_btn);
    }

    @Override
    public void onMainMenuInteraction(Integer btn) {
        switch(btn) {
            case R.id.test_send_document_button: {
                startDownload(btn);
            }
        }
    }
}
