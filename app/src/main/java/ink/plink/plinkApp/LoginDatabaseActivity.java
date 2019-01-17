package ink.plink.plinkApp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LoginDatabaseActivity extends AppCompatActivity {
    public static final String KEY_USER_ACCOUNT = "User Account Key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_database);
    }
}
