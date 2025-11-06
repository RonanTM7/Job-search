package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView errorTextView;
    private ImageButton themeChangeButton;
    private static final int FORGOT_PASSWORD_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewRegister = findViewById(R.id.textViewRegister);
        themeChangeButton = findViewById(R.id.themeChangeButton);
        errorTextView = findViewById(R.id.errorTextView);

        if (getIntent().hasExtra("TOAST_MESSAGE")) {
            String message = getIntent().getStringExtra("TOAST_MESSAGE");
            if (message != null && !message.isEmpty()) {
                com.example.job.CustomToast.showToast(LoginActivity.this, message, 4000);
                getIntent().removeExtra("TOAST_MESSAGE");
            }
        }

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
        findViewById(R.id.textViewForgotPassword).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivityForResult(intent, FORGOT_PASSWORD_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FORGOT_PASSWORD_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String message = data.getStringExtra("TOAST_MESSAGE");
            if (message != null) {
                com.example.job.CustomToast.showToast(LoginActivity.this, message, 4000);
            }
        }
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
            com.example.job.CustomToast.showToast(this, "Заполните все поля", 4000);
            buttonLogin.setEnabled(true);
            buttonLogin.setText("Войти");
            return;
        }

        // вход
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                errorTextView.setText("Пользователя с такой почтой не существует");
                errorTextView.setVisibility(TextView.VISIBLE);
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Войти");
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, authTask -> {
                            if (authTask.isSuccessful()) {
                                if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    errorTextView.setText("Пожалуйста, подтвердите вашу почту");
                                    errorTextView.setVisibility(TextView.VISIBLE);
                                    buttonLogin.setEnabled(true);
                                    buttonLogin.setText("Войти");
                                }
                            } else {
                                errorTextView.setText("Неверный логин или пароль");
                                errorTextView.setVisibility(TextView.VISIBLE);
                                buttonLogin.setEnabled(true);
                                buttonLogin.setText("Войти");
                            }
                        });
            }
        });
    }

    private void register() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
}