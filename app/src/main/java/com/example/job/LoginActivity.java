package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private TextView errorTextView;
    private ImageButton themeChangeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewRegister = findViewById(R.id.textViewRegister);
        themeChangeButton = findViewById(R.id.themeChangeButton);
        errorTextView = findViewById(R.id.errorTextView);

        updateThemeIcon();

        themeChangeButton.setOnClickListener(v -> {
            int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            recreate();
        });

        buttonLogin.setOnClickListener(v -> login());
        textViewRegister.setOnClickListener(v -> register());
    }
    private void updateThemeIcon() {
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            themeChangeButton.setImageResource(R.drawable.ic_sun);
        } else {
            themeChangeButton.setImageResource(R.drawable.ic_moon);
        }
    }
    private void login() {
        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setEnabled(false);
        buttonLogin.setText("Вход...");

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            buttonLogin.setEnabled(true);
            buttonLogin.setText("Войти");
            return;
        }

        // вход
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        errorTextView.setText("Неверный логин или пароль");
                        errorTextView.setVisibility(TextView.VISIBLE);
                        buttonLogin.setEnabled(true);
                        buttonLogin.setText("Войти");
                    }
                });
    }

    private void register() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
}