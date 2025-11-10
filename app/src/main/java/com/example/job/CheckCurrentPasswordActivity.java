package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class CheckCurrentPasswordActivity extends AppCompatActivity {

    private EditText currentPasswordEditText;
    private TextView errorMessageTextView;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_current_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        currentPasswordEditText = findViewById(R.id.et_current_password);
        errorMessageTextView = findViewById(R.id.tv_error_message);
        Button nextButton = findViewById(R.id.btn_next);
        Button forgotPasswordButton = findViewById(R.id.btn_forgot_password);
        ImageButton backButton = findViewById(R.id.btn_back);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        backButton.setOnClickListener(v -> onBackPressed());
        nextButton.setOnClickListener(v -> checkPassword());
        forgotPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(CheckCurrentPasswordActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void checkPassword() {
        String password = currentPasswordEditText.getText().toString().trim();
        if (password.isEmpty()) {
            errorMessageTextView.setText("Пароль не может быть пустым");
            errorMessageTextView.setVisibility(TextView.VISIBLE);
            return;
        }

        if (currentUser != null && currentUser.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), password);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(CheckCurrentPasswordActivity.this, EnterNewPasswordActivity.class);
                            intent.putExtra("old_password", password);
                            startActivity(intent);
                        } else {
                            errorMessageTextView.setText("Неверный пароль");
                            errorMessageTextView.setVisibility(TextView.VISIBLE);
                        }
                    });
        }
    }
}