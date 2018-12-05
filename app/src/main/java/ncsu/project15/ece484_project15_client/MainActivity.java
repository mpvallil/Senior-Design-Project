package ncsu.project15.ece484_project15_client;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import ncsu.project15.ece484_project15_client.NetworkFragmentBuilder;
import ncsu.project15.ece484_project15_client.dummy.DummyContent;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DownloadCallback<String>, SettingsFragment.OnSettingsInteractionListener,
                        GoogleMapsFragment.OnMapsInteractionListener, MainMenu.OnMainMenuInteractionListener, ManageDocument.OnManageDocumentInteractionListener,
                        PrinterOwnerFragment.OnPrinterOwnerFragmentInteractionListener {
    //Bundle arguments
    public static final String KEY_USER_ACCOUNT = "User Account Key";
    GoogleSignInAccount mGoogleSignInAccount;

    /** Fragment references */
    // Fields for naming Fragments from the Nav Menu
    private static final String TAG_MAIN_MENU_FRAG = "MAIN_MENU_FRAG";
    private static final String TAG_GOOGLE_MAPS_FRAG = "GOOGLE_MAPS_FRAG";
    private static final String TAG_PRINTER_OWNER_FRAGMENT = "PRINTER_OWNER_FRAG";
    private static final String TAG_SETTINGS_FRAG = "SETTINGS_FRAG";
    private static final String TAG_MANAGE_DOCUMENT_FRAG = "MANAGE_FRAG";
    private static final String TAG_PRINTER_SETTINGS_FRAGMENT = "PRINTER_SETTINGS_FRAGMENT";
    // References to Nav Menu Fragments
    GoogleMapsFragment mGoogleMapsFragment;
    SettingsFragment mSettingsFragment;
    MainMenu mMainMenuFragment;
    Fragment currentFragment;
    private FragmentManager fm;
    private final FragmentManager.OnBackStackChangedListener backStackListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            if (fm.getBackStackEntryCount() > 0) {
                toggle.setDrawerIndicatorEnabled(false);
//                toggle.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24px);
//                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        onBackPressed();
//                    }
//                });
                Log.i("backStackChanged", "no Map");
            } else {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                findViewById(R.id.my_toolbar).setVisibility(View.VISIBLE);
                mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                toggle.setDrawerIndicatorEnabled(true);
//                toggle.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
//                toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_filter_list_24px));
                toggle.setToolbarNavigationClickListener(null);
                mGoogleMapsFragment.setUserVisibleHint(true);
                Log.i("backStackChanged", "Map");
            }
        }
    };

    /** Network Activity fields */
    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;
    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;

    /** Layout fields */
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ImageView nvDrawerHeaderImage;
    private TextView nvDrawerHeaderName;
    private TextView nvDrawerHeaderEmail;
    ActionBarDrawerToggle toggle;
    MenuItem drawerItem;

    JsonObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Get Google User Account
        Bundle args = getIntent().getExtras();
        mGoogleSignInAccount = args.getParcelable(KEY_USER_ACCOUNT);

        // Set Toolbar
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_filter_list_24px));

        // Find the DrawerLayout
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                return super.onOptionsItemSelected(item);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (drawerItem != null) {
                    int id = drawerItem.getItemId();
                    Fragment newFragment;
                    mGoogleMapsFragment.setUserVisibleHint(false);
                    switch (id) {
                        case R.id.nav_drawer_Settings: {
                            newFragment = new SettingsFragment();
                            fm.beginTransaction()
                                    .setCustomAnimations(R.animator.slide_up, 0, 0, R.animator.slide_down)
                                    .add(R.id.flContent, newFragment, TAG_SETTINGS_FRAG)
                                    .addToBackStack(null)
                                    .commit();
                            currentFragment = newFragment;
                            break;
                        }
                        case R.id.nav_drawer_MainMenu: {
                            newFragment = new MainMenu();
                            fm.beginTransaction()
                                    .setCustomAnimations(R.animator.slide_up, 0, 0, R.animator.slide_down)
                                    .add(R.id.flContent, newFragment, TAG_MAIN_MENU_FRAG)
                                    .addToBackStack(null)
                                    .commit();
                            currentFragment = newFragment;
                            break;
                        }
                        case R.id.nav_drawer_PrinterLanding: {
                            newFragment = new PrinterOwnerFragment();
                            mGoogleMapsFragment.setUserVisibleHint(false);
                            fm.beginTransaction()
                                    .setCustomAnimations(R.animator.slide_up, 0, 0, R.animator.slide_down)
                                    .add(R.id.flContent, newFragment, TAG_PRINTER_OWNER_FRAGMENT)
                                    .addToBackStack(null)
                                    .commit();
                            currentFragment = newFragment;
                            break;
                        }

                        case R.id.nav_drawer_SendDocument: {
                            newFragment = new ManageDocument();
                            mGoogleMapsFragment.setUserVisibleHint(false);
                            fm.beginTransaction()
                                    .setCustomAnimations(R.animator.slide_up, 0, 0, R.animator.slide_down)
                                    .add(R.id.flContent, newFragment, TAG_MANAGE_DOCUMENT_FRAG)
                                    .addToBackStack(null)
                                    .commit();
                            currentFragment = newFragment;
                            break;
                        }
                        case R.id.nav_drawer_Logout: {
                            startActivity(new Intent(MainActivity.this, SplashActivity.class).putExtra(SplashActivity.KEY_SIGN_OUT, mGoogleSignInAccount));
                            finish();
                            break;
                        }
                    }
                    drawerItem = null;
                    mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
        };
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        // Find Nav Drawer and associated elements
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        nvDrawer.setNavigationItemSelectedListener(this);
        View nvDrawerHeader = nvDrawer.getHeaderView(0);
        nvDrawerHeaderImage = nvDrawerHeader.findViewById(R.id.imageView_user_picture);
        nvDrawerHeaderName = nvDrawerHeader.findViewById(R.id.texView_user_name);
        nvDrawerHeaderEmail = nvDrawerHeader.findViewById(R.id.textView_user_email);

        //Set header fields
        if (mGoogleSignInAccount.getPhotoUrl() != null) {
            Picasso.get().load(mGoogleSignInAccount.getPhotoUrl()).into(nvDrawerHeaderImage);
        }
        nvDrawerHeaderName.setText(mGoogleSignInAccount.getDisplayName());
        nvDrawerHeaderEmail.setText(mGoogleSignInAccount.getEmail());

        fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(backStackListener);

        mGoogleMapsFragment = GoogleMapsFragment.newInstance();
        fm
                .beginTransaction()
                .add(R.id.flContent, mGoogleMapsFragment, TAG_GOOGLE_MAPS_FRAG)
                .commit();
        currentFragment = mGoogleMapsFragment;

        json = new JsonObject();
        json.addProperty("name", "printer1");
        Log.i("JSON", json.toString());
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
        getMenuInflater().inflate(R.menu.filter_view, menu);
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
        this.drawerItem = item;
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startDownload() {
        if (mNetworkFragment != null && !mDownloading) {
            mNetworkFragment.startDownload();
            mDownloading = true;
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
    public void onMapsInteraction(NetworkFragment mapsNetworkFragment) {
        mNetworkFragment = mapsNetworkFragment;
        startDownload();
    }

    @Override
    public void onMainMenuInteraction(Integer btn) {
        switch(btn) {
            case R.id.test_send_document_button: {
                //mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), NetworkFragment.URL_UPLOAD);
                startDownload();
            }
        }
    }

    @Override
    public void onManageDocumentInteraction(NetworkFragment documentNetworkFragment) {
        mNetworkFragment = documentNetworkFragment;
        startDownload();
    }

    @Override
    public void onPrinterOwnerFragmentInteraction(DummyContent.DummyItem item) {
        if(fm.findFragmentByTag(TAG_PRINTER_OWNER_FRAGMENT).isVisible()) {
            fm.beginTransaction()
                    .setCustomAnimations(R.animator.slide_up, R.animator.slide_down, R.animator.slide_up, R.animator.slide_down)
                    .replace(R.id.flContent, new PrinterSettingsFragment(), TAG_PRINTER_SETTINGS_FRAGMENT)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
