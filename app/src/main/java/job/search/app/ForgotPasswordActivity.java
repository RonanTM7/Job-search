package job.search.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;
import job.search.app.utils.CustomToast;

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

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(resetTask -> {
                    if (resetTask.isSuccessful()) {
                        errorMessageTextView.setVisibility(TextView.GONE);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("TOAST_MESSAGE", "Ссылка для сброса пароля отправлена на вашу почту");
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        String errorMessage = "Ошибка отправки письма";
                        Exception exception = resetTask.getException();
                        if (exception != null) {
                            String message = exception.getMessage();
                            if (message != null && (message.contains("user-not-found") || message.contains("no user"))) {
                                errorMessage = "Пользователя с такой почтой не существует";
                            } else if (message != null && (message.contains("network") || message.contains("connection"))) {
                                errorMessage = "Ошибка сети, проверьте соединение";
                                CustomToast.showToast(ForgotPasswordActivity.this, errorMessage, 4000);
                            }
                        }
                        errorMessageTextView.setText(errorMessage.toLowerCase());
                        errorMessageTextView.setVisibility(TextView.VISIBLE);
                        sendResetLinkButton.setEnabled(true);
                        sendResetLinkButton.setText("Сменить пароль");
                    }
                });
    }
}
