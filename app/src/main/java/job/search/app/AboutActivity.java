package job.search.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        TextView appVersionTextView = findViewById(R.id.tv_app_version);
        ImageButton backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> onBackPressed());

        String versionName = getString(R.string.app_version_number);
        appVersionTextView.setText("Версия " + versionName);

        findViewById(R.id.tv_what_new).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(AboutActivity.this, UpdatesActivity.class);
            startActivity(intent);
        });
    }
}
