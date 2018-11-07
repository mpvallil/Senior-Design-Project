package ncsu.project15.ece484_project15_client;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainMenu extends Fragment {

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
                //startActivity(new Intent(MainMenu.this, MainActivity.class));
            }
        });

        test_send_document_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onMainMenuInteraction(R.id.test_send_document_button);
            }
        });
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = getActivity().findViewById(R.id.my_toolbar);
        toolbar.setBackgroundResource(R.color.colorPrimary);
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



    public interface OnMainMenuInteractionListener {
        // TODO: Update argument type and name
        void onMainMenuInteraction(Integer btn);
    }
}
