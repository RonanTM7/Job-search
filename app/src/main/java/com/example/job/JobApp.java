package com.example.job;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class JobApp extends Application {

    private static final String PREFS_NAME = "AppSettings";
    private static final String THEME_KEY = "isDarkTheme";
    private static final String FIRST_RUN_KEY = "isFirstRun";
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean(FIRST_RUN_KEY, true);

        if (isFirstRun) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            sharedPreferences.edit().putBoolean(FIRST_RUN_KEY, false).apply();
        } else {
            boolean isDarkTheme = sharedPreferences.getBoolean(THEME_KEY, false);
            if (isDarkTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }
}