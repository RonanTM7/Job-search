package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EnterNewPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText;
    private TextView errorMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_new_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        newPasswordEditText = findViewById(R.id.et_new_password);
        errorMessageTextView = findViewById(R.id.tv_error_message);
        ImageButton backButton = findViewById(R.id.btn_back);
        Button nextButton = findViewById(R.id.btn_next);

        String oldPassword = getIntent().getStringExtra("old_password");

        backButton.setOnClickListener(v -> onBackPressed());
        nextButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            if (validatePassword(newPassword, oldPassword)) {
                Intent intent = new Intent(EnterNewPasswordActivity.this, ConfirmNewPasswordActivity.class);
                intent.putExtra("new_password", newPassword);
                startActivity(intent);
            }
        });
    }

    private boolean validatePassword(String password, String oldPassword) {
        if (password.isEmpty()) {
            errorMessageTextView.setText("Пароль не может быть пустым");
            errorMessageTextView.setVisibility(TextView.VISIBLE);
            return false;
        }
        if (password.length() < 6) {
            errorMessageTextView.setText("Пароль слишком короткий");
            errorMessageTextView.setVisibility(TextView.VISIBLE);
            return false;
        }
        if (password.length() > 16) {
            errorMessageTextView.setText("Пароль слишком длинный");
            errorMessageTextView.setVisibility(TextView.VISIBLE);
            return false;
        }
        if (password.equals(oldPassword)) {
            errorMessageTextView.setText("Пароль должен отличаться от установленного");
            errorMessageTextView.setVisibility(TextView.VISIBLE);
            return false;
        }
        errorMessageTextView.setVisibility(TextView.GONE);
        return true;
    }
}