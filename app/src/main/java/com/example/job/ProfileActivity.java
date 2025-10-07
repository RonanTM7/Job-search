package com.example.job;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewEmail;
    private Button buttonLogout, buttonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        textViewEmail = findViewById(R.id.textViewEmail);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonSettings = findViewById(R.id.buttonSettings);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String email = prefs.getString("user_email", "user@example.com");
        textViewEmail.setText(email);

        buttonLogout.setOnClickListener(v -> logout());
        buttonSettings.setOnClickListener(v -> openSettings());
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("is_logged_in", false).apply();

        Toast.makeText(this, "Выход выполнен", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}