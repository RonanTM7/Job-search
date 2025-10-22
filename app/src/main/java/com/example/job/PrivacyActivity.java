package com.example.job;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class PrivacyActivity extends AppCompatActivity {

    private TextView phoneNumberTextView, emailTextView;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        phoneNumberTextView = findViewById(R.id.tv_phone_number);
        emailTextView = findViewById(R.id.tv_email);
        Button changePasswordButton = findViewById(R.id.btn_change_password);
        ImageButton backButton = findViewById(R.id.btn_back);

        loadUserData();

        backButton.setOnClickListener(v -> onBackPressed());
        phoneNumberTextView.setOnClickListener(v -> showUpdateDialog("phone"));
        emailTextView.setOnClickListener(v -> showUpdateDialog("email"));
        changePasswordButton.setOnClickListener(v -> showUpdateDialog("password"));
    }

    private void loadUserData() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            phoneNumberTextView.setText(documentSnapshot.getString("phone"));
                            emailTextView.setText(documentSnapshot.getString("email"));
                        }
                    });
        }
    }

    private void showUpdateDialog(String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Сменить " + field);

        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Текущий пароль");
        builder.setView(passwordInput);

        builder.setPositiveButton("Подтвердить", (dialog, which) -> {
            String password = passwordInput.getText().toString();
            if (password.isEmpty()) {
                Toast.makeText(this, "Пароль не может быть пустым", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), password);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showNewValueDialog(field);
                        } else {
                            Toast.makeText(PrivacyActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showNewValueDialog(String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите новое значение");

        final EditText newValueInput = new EditText(this);
        if (field.equals("password")) {
            newValueInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        builder.setView(newValueInput);

        builder.setPositiveButton("Применить", (dialog, which) -> {
            String newValue = newValueInput.getText().toString();
            if (newValue.isEmpty()) {
                Toast.makeText(this, "Поле не может быть пустым", Toast.LENGTH_SHORT).show();
                return;
            }

            if (field.equals("password")) {
                currentUser.updatePassword(newValue)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PrivacyActivity.this, "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PrivacyActivity.this, "Ошибка при смене пароля", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                db.collection("users").document(currentUser.getUid())
                        .update(field, newValue)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(PrivacyActivity.this, "Данные успешно обновлены", Toast.LENGTH_SHORT).show();
                            loadUserData();
                        });
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
