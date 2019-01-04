package ncsu.project15.ece484_project15_client;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
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
                        GoogleMapsFragment.OnMapsInteractionListener, ManageDocument.OnManageDocumentInteractionListener,
                        PrinterOwnerFragment.OnPrinterOwnerFragmentInteractionListener, PrinterDisplayFragment.OnPrinterDisplayInteractionListener,
                        PrinterFilterFragment.PrinterFilterFragmentListener {
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
    private static final String TAG_PRINTER_DISPLAY_FRAG = "PRINTER_DISPLAY_FRAG";
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
                Log.i("backStackChanged", "no Map");
            } else {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                findViewById(R.id.my_toolbar).setVisibility(View.VISIBLE);
                mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                toggle.setDrawerIndicatorEnabled(true);
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

        setToolbar();
        setDrawerLayout();

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

    private void setDrawerLayout() {
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
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .add(R.id.flContent, new SettingsFragment(), TAG_SETTINGS_FRAG)
                                    .addToBackStack(null)
                                    .commit();
                            currentFragment = newFragment;
                            break;
                        }
                        case R.id.nav_drawer_PrinterLanding: {
                            newFragment = new PrinterOwnerFragment();
                            mGoogleMapsFragment.setUserVisibleHint(false);
                            fm.beginTransaction()
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
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
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
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
    }

    private void setToolbar() {
        // Set Toolbar
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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
        if (item.getItemId() == R.id.action_filter) {
            DialogFragment dialogFragment = new PrinterFilterFragment();
            dialogFragment.show(fm, "filter");
            return false;
        } else {
            return toggle.onOptionsItemSelected(item);
        }
    }

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
        //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

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
        PrinterDisplayFragment mPrinterDisplayFragment = new PrinterDisplayFragment();
        mPrinterDisplayFragment.setPrinter(printer);
        fm.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.flContent, mPrinterDisplayFragment, TAG_PRINTER_DISPLAY_FRAG)
                .addToBackStack(null)
                .commit();
        startDownload();
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
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(R.id.flContent, new PrinterSettingsFragment(), TAG_PRINTER_SETTINGS_FRAGMENT)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onPrinterDisplayInteraction(NetworkFragment printerNetworkFragment) {
        mNetworkFragment = printerNetworkFragment;
        startDownload();
    }

    @Override
    public void onPrinterFilterPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onPrinterFilterClearClick(DialogFragment dialog) {

    }
}
