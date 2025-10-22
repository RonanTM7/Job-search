package com.example.job;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class AppearanceActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppSettings";
    private static final String THEME_KEY = "isDarkTheme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button lightThemeButton = findViewById(R.id.btn_light_theme);
        Button darkThemeButton = findViewById(R.id.btn_dark_theme);
        ImageButton backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> onBackPressed());

        lightThemeButton.setOnClickListener(v -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            saveTheme(false);
        });

        darkThemeButton.setOnClickListener(v -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            saveTheme(true);
        });
    }

    private void saveTheme(boolean isDarkTheme) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(THEME_KEY, isDarkTheme);
        editor.apply();
    }
}
