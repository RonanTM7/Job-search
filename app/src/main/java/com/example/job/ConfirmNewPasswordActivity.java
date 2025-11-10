package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ConfirmNewPasswordActivity extends AppCompatActivity {

    private EditText confirmPasswordEditText;
    private TextView errorMessageTextView;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_new_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        confirmPasswordEditText = findViewById(R.id.et_confirm_password);
        errorMessageTextView = findViewById(R.id.tv_error_message);
        ImageButton backButton = findViewById(R.id.btn_back);
        Button nextButton = findViewById(R.id.btn_next);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String newPassword = getIntent().getStringExtra("new_password");

        backButton.setOnClickListener(v -> onBackPressed());
        nextButton.setOnClickListener(v -> {
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            if (confirmPassword.equals(newPassword)) {
                updatePassword(newPassword);
            } else {
                errorMessageTextView.setText("Пароли не совпадают");
                errorMessageTextView.setVisibility(TextView.VISIBLE);
            }
        });
    }

    private void updatePassword(String newPassword) {
        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ConfirmNewPasswordActivity.this, "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ConfirmNewPasswordActivity.this, PrivacyActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            errorMessageTextView.setText("Ошибка при смене пароля");
                            errorMessageTextView.setVisibility(TextView.VISIBLE);
                        }
                    });
        }
    }
}