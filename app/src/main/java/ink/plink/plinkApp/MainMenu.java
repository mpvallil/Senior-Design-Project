package ink.plink.plinkApp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainMenu extends Fragment {


    private static final int READ_REQUEST_CODE = 42;

    private static final String KEY_ARGS = "ARGS";
    PrinterOwnerFragment mPrinterOwnerFragment = new PrinterOwnerFragment();
    OnMainMenuInteractionListener mCallback;

    public static MainMenu newInstance(String name){
        MainMenu fragment = new MainMenu();
        Bundle args = new Bundle();
        args.putString(KEY_ARGS, name);
        fragment.setArguments(args);
        return fragment;
    }

    public MainMenu() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String name = getArguments().getString(KEY_ARGS);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_main_menu, container, false);
        setToolbar(view);

        Button test_maps_button = view.findViewById(R.id.test_map_layout_button);
        Button test_login_button = view.findViewById(R.id.test_login_button);
        Button test_send_document_button = view.findViewById(R.id.test_send_document_button);
        String permission[] = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        hasPermissions(getContext(), permission);


        test_maps_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainMenu.this, MapsActivity.class));
            }
        });

        test_login_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });

        test_send_document_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onMainMenuInteraction(R.id.test_send_document_button);
            }
        });
        return view;
    }

    private void setToolbar(View v) {
        Toolbar fragmentToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        Toolbar activityToolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        activityToolbar.setVisibility(View.GONE);
        ((AppCompatActivity)getActivity()).setSupportActionBar(fragmentToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fragmentToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnMainMenuInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 98;
    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
                    return false;
                }
            }
        }
        return true;
    }

    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("application/pdf");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("FileExplorer", "Uri: " + uri.toString());
            }
        }
    }



    public interface OnMainMenuInteractionListener {
        // TODO: Update argument type and name
        void onMainMenuInteraction(Integer btn);
    }
}
