package ncsu.project15.ece484_project15_client;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    PrinterOwnerFragment mPrinterOwnerFragment = new PrinterOwnerFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button test_maps_button = findViewById(R.id.test_map_layout_button);
        Button test_login_button = findViewById(R.id.test_login_button);
        Button test_send_document_button = findViewById(R.id.test_send_document_button);


        test_maps_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, MapsActivity.class));
            }
        });

        test_login_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, MainActivity.class));
            }
        });

        test_send_document_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, DownloadActivity.class));
            }
        });
    }
}
