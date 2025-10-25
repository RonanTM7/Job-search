package com.example.job;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendResetLinkButton;
    private TextView errorMessageTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        emailEditText = findViewById(R.id.et_email);
        sendResetLinkButton = findViewById(R.id.btn_send_reset_link);
        errorMessageTextView = findViewById(R.id.tv_error_message);
        ImageButton backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> onBackPressed());
        sendResetLinkButton.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void sendPasswordResetEmail() {
        sendResetLinkButton.setEnabled(false);
        sendResetLinkButton.setText("Отправка...");
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Введите почту");
            sendResetLinkButton.setEnabled(true);
            sendResetLinkButton.setText("Сменить пароль");
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        errorMessageTextView.setVisibility(TextView.GONE);
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(resetTask -> {
                                    if (resetTask.isSuccessful()) {
                                        Toast.makeText(ForgotPasswordActivity.this, "Ссылка для сброса пароля отправлена на вашу почту", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(ForgotPasswordActivity.this, "Ошибка: " + resetTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        sendResetLinkButton.setEnabled(true);
                                        sendResetLinkButton.setText("Сменить пароль");
                                    }
                                });
                    } else {
                        errorMessageTextView.setText("такого пользователя не существует");
                        errorMessageTextView.setVisibility(TextView.VISIBLE);
                        sendResetLinkButton.setEnabled(true);
                        sendResetLinkButton.setText("Сменить пароль");
                    }
                });
    }
}
