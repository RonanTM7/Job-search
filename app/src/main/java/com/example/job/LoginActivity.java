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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.job.utils.CustomToast;

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
                CustomToast.showToast(LoginActivity.this, message, 4000);
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
                CustomToast.showToast(LoginActivity.this, message, 4000);
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
            CustomToast.showToast(this, "Заполните все поля", 4000);
            buttonLogin.setEnabled(true);
            buttonLogin.setText("Войти");
            return;
        }

        // вход
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            db.collection("users").document(user.getUid()).get().addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful() && dbTask.getResult() != null) {
                                    String status = dbTask.getResult().getString("status");
                                    if ("blocked".equals(status)) {
                                        mAuth.signOut();
                                        errorTextView.setText("этот аккаунт заблокирован");
                                        errorTextView.setVisibility(TextView.VISIBLE);
                                        buttonLogin.setEnabled(true);
                                        buttonLogin.setText("Войти");
                                    } else if ("deleted".equals(status)) {
                                        mAuth.signOut();
                                        errorTextView.setText("Аккаунт удален, зарегистрируйтесь под новыми данными");
                                        errorTextView.setVisibility(TextView.VISIBLE);
                                        buttonLogin.setEnabled(true);
                                        buttonLogin.setText("Войти");
                                    } else {
                                        if (user.isEmailVerified() || email.equals("ronanauf@gmail.com")) {
                                            if (email.equals("ronanauf@gmail.com")) {
                                                startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                                            } else {
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            }
                                            finish();
                                        } else {
                                            errorTextView.setText("Пожалуйста, подтвердите вашу почту");
                                            errorTextView.setVisibility(TextView.VISIBLE);
                                            buttonLogin.setEnabled(true);
                                            buttonLogin.setText("Войти");
                                        }
                                    }
                                } else {
                                    // If no doc in Firestore, but Auth succeeds (shouldn't happen with normal flow)
                                    if (user.isEmailVerified() || email.equals("ronanauf@gmail.com")) {
                                        if (email.equals("ronanauf@gmail.com")) {
                                            startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                                        } else {
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        }
                                        finish();
                                    } else {
                                        errorTextView.setText("Пожалуйста, подтвердите вашу почту");
                                        errorTextView.setVisibility(TextView.VISIBLE);
                                        buttonLogin.setEnabled(true);
                                        buttonLogin.setText("Войти");
                                    }
                                }
                            });
                        }
                    } else {
                        String errorMessage = "Неверный логин или пароль";
                        Exception exception = authTask.getException();
                        if (exception != null) {
                            String message = exception.getMessage();
                            if (message != null && (message.contains("network") || message.contains("connection"))) {
                                errorMessage = "Ошибка входа, проверьте соединение с интернетом";
                                CustomToast.showToast(LoginActivity.this, errorMessage, 4000);
                            }
                        }
                        errorTextView.setText(errorMessage);
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
