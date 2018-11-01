package ncsu.project15.ece484_project15_client;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainMenu extends Fragment {

    private static final String KEY_ARGS = "ARGS";
    PrinterOwnerFragment mPrinterOwnerFragment = new PrinterOwnerFragment();

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
                //startActivity(new Intent(MainMenu.this, DownloadActivity.class));
            }
        });
        return view;
    }
}
