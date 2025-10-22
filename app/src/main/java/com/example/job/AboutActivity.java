package com.example.job;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private TextView appVersionTextView;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        appVersionTextView = findViewById(R.id.tv_app_version);
        backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> onBackPressed());

        String versionName = "1.0";
        appVersionTextView.setText("Версия " + versionName);
    }
}
