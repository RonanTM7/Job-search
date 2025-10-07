package com.example.job;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchDarkTheme;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppSettings";
    private static final String THEME_KEY = "isDarkTheme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Инициализация элементов
        switchDarkTheme = findViewById(R.id.switchDarkTheme);
        TextView textViewVersion = findViewById(R.id.textViewVersion);

        // Установка версии приложения
        textViewVersion.setText("1.0.0");

        // Загрузка сохраненной темы
        boolean isDarkTheme = sharedPreferences.getBoolean(THEME_KEY, false);
        switchDarkTheme.setChecked(isDarkTheme);

        // Обработчик переключателя темы
        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveThemePreference(isChecked);
            applyTheme(isChecked);
        });
    }

    private void saveThemePreference(boolean isDarkTheme) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(THEME_KEY, isDarkTheme);
        editor.apply();
    }

    private void applyTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Перезапускаем активность для применения темы
        recreate();
    }
}