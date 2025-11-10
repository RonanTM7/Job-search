package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NewChangeEmailActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button btnChangeEmail;
    private TextView textError;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_change_email);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.edit_text_email);
        btnChangeEmail = findViewById(R.id.btn_change_email);
        textError = findViewById(R.id.text_error);
        ImageButton backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> onBackPressed());

        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textError.setVisibility(TextView.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnChangeEmail.setOnClickListener(v -> changeEmail());
    }

    private void changeEmail() {
        String newEmail = editTextEmail.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser();

        if (newEmail.isEmpty()) {
            textError.setText("Поле не может быть пустым");
            textError.setVisibility(TextView.VISIBLE);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            textError.setText("Введите корректный адрес электронной почты");
            textError.setVisibility(TextView.VISIBLE);
            return;
        }

        if (user != null && newEmail.equals(user.getEmail())) {
            textError.setText("Вы ввели тот же адрес электронной почты.");
            textError.setVisibility(TextView.VISIBLE);
            return;
        }

        if (user == null) {
            textError.setText("Не удалось обновить данные. Попробуйте снова.");
            textError.setVisibility(TextView.VISIBLE);
            return;
        }

        user.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(NewChangeEmailActivity.this, "Письмо с подтверждением отправлено на новую почту.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        Intent intent = new Intent(NewChangeEmailActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = "Неизвестная ошибка";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getClass().getSimpleName() + ": " + task.getException().getMessage();
                        }
                        textError.setText("Ошибка: " + errorMessage);
                        textError.setVisibility(TextView.VISIBLE);
                    }
                });
    }
}
