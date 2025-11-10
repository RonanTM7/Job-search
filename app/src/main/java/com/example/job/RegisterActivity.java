package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.example.job.utils.CustomToast;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, phoneEditText, emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button registerButton = findViewById(R.id.registerButton);
        TextView loginTextView = findViewById(R.id.loginTextView);

        registerButton.setOnClickListener(v -> registerUser());
        loginTextView.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().startsWith("+7")) {
                    phoneEditText.setText("+7");
                    phoneEditText.setSelection(phoneEditText.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String phoneNumber = s.toString().replaceAll("\\D", "");
                if (phoneNumber.length() > 1) {
                    phoneNumber = phoneNumber.substring(1);
                    StringBuilder formatted = new StringBuilder("+7 ");
                    formatted.append(phoneNumber.substring(0, Math.min(3, phoneNumber.length())));
                    if (phoneNumber.length() >= 4) {
                        formatted.append(" ").append(phoneNumber.substring(3, Math.min(6, phoneNumber.length())));
                    }
                    if (phoneNumber.length() >= 7) {
                        formatted.append(" ").append(phoneNumber.substring(6, Math.min(8, phoneNumber.length())));
                    }
                    if (phoneNumber.length() >= 9) {
                        formatted.append(" ").append(phoneNumber.substring(8, Math.min(10, phoneNumber.length())));
                    }

                    phoneEditText.removeTextChangedListener(this);
                    phoneEditText.setText(formatted.toString());
                    phoneEditText.setSelection(formatted.length());
                    phoneEditText.addTextChangedListener(this);
                }
            }
        });
    }

    private void registerUser() {
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setText("Регистрация...");
        String username = usernameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Введите имя пользователя");
            registerButton.setEnabled(true);
            registerButton.setText("Зарегистрироваться");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Введите номер телефона");
            registerButton.setEnabled(true);
            registerButton.setText("Зарегистрироваться");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Введите почту");
            registerButton.setEnabled(true);
            registerButton.setText("Зарегистрироваться");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Введите корректный адрес электронной почты");
            registerButton.setEnabled(true);
            registerButton.setText("Зарегистрироваться");
            return;
        }

        if (phone.length() != 16) {
            phoneEditText.setError("Введите корректный номер телефона");
            registerButton.setEnabled(true);
            registerButton.setText("Зарегистрироваться");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Введите пароль");
            registerButton.setEnabled(true);
            registerButton.setText("Зарегистрироваться");
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Пароль должен содержать не менее 6 символов");
            registerButton.setEnabled(true);
            registerButton.setText("Зарегистрироваться");
            return;
        }

        db.collection("users").whereEqualTo("username", username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                usernameEditText.setError("Имя пользователя уже занято");
                registerButton.setEnabled(true);
                registerButton.setText("Зарегистрироваться");
            } else {
                db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                        emailEditText.setError("Почта уже зарегистрирована");
                        registerButton.setEnabled(true);
                        registerButton.setText("Зарегистрироваться");
                    } else {
                        db.collection("users").whereEqualTo("phone", phone).get().addOnCompleteListener(task3 -> {
                            if (task3.isSuccessful() && !task3.getResult().isEmpty()) {
                                phoneEditText.setError("Номер телефона уже зарегистрирован");
                                registerButton.setEnabled(true);
                                registerButton.setText("Зарегистрироваться");
                            } else {
                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this, authTask -> {
                                            if (authTask.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if (user != null) {
                                                    user.sendEmailVerification();
                                                    saveUserToFirestore(user.getUid(), username, phone, email);

                                                    runOnUiThread(() -> {
                                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                        intent.putExtra("TOAST_MESSAGE", "Регистрация прошла успешно. Пожалуйста, подтвердите вашу почту.");
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    });
                                                }
                                            } else {
                                                CustomToast.showToast(RegisterActivity.this, "Ошибка регистрации: " + Objects.requireNonNull(authTask.getException()).getMessage(), 4000);
                                                registerButton.setEnabled(true);
                                                registerButton.setText("Зарегистрироваться");
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        });
    }

    private void saveUserToFirestore(String userId, String username, String phone, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("phone", phone);
        user.put("email", email);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    CustomToast.showToast(RegisterActivity.this, "Ошибка сохранения данных: " + e.getMessage(), 4000);
                }));
    }
}