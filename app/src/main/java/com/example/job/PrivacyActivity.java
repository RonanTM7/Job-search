package com.example.job;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class PrivacyActivity extends AppCompatActivity {

    private TextView phoneNumberTextView, emailTextView;
    private ImageButton refreshEmailButton;
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
        refreshEmailButton = findViewById(R.id.btn_refresh_email);
        Button changePasswordButton = findViewById(R.id.btn_change_password);
        ImageButton backButton = findViewById(R.id.btn_back);

        loadUserData();

        backButton.setOnClickListener(v -> onBackPressed());
        phoneNumberTextView.setOnClickListener(v -> showUpdateDialog("phone"));
        emailTextView.setOnClickListener(v -> showUpdateDialog("email"));
        changePasswordButton.setOnClickListener(v -> showUpdateDialog("password"));
        refreshEmailButton.setOnClickListener(v -> refreshEmailData());
    }

    private void refreshEmailData() {
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser freshUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (freshUser != null) {
                        String newEmail = freshUser.getEmail();
                        String userId = freshUser.getUid();

                        // Attempt to update Firestore, but don't rely on it for UI
                        db.collection("users").document(userId).update("email", newEmail)
                                .addOnSuccessListener(aVoid -> {
                                    // Success is optional, the UI is already updated by loadUserData
                                    Toast.makeText(PrivacyActivity.this, "Почта успешно обновлена.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Log or handle the failure silently if needed
                                });

                        loadUserData(); // Reload UI from Auth
                        refreshEmailButton.setVisibility(ImageButton.GONE);

                    }
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        Toast.makeText(PrivacyActivity.this, "Сессия истекла. Пожалуйста, войдите снова.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(PrivacyActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = "Неизвестная ошибка";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getClass().getSimpleName() + ": " + task.getException().getMessage();
                        }
                        Toast.makeText(PrivacyActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Set email directly from Auth - this is the source of truth
            emailTextView.setText(user.getEmail());

            // Load other data like phone number from Firestore
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            phoneNumberTextView.setText(documentSnapshot.getString("phone"));
                        }
                    });
        }
    }

    private void showUpdateDialog(String field) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_data);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        EditText editTextData = dialog.findViewById(R.id.edit_text_data);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        dialogTitle.setText("Введите текущий пароль");
        editTextData.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editTextData.setHint("Текущий пароль");
        btnSave.setText("Подтвердить");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String password = editTextData.getText().toString();
            if (password.isEmpty()) {
                Toast.makeText(this, "Пароль не может быть пустым", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), password);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            FirebaseUser freshUser = FirebaseAuth.getInstance().getCurrentUser();
                            showNewValueDialog(field, freshUser);
                        } else {
                            Toast.makeText(PrivacyActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        dialog.show();
    }

    private void showNewValueDialog(String field, FirebaseUser user) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_data);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        EditText editTextData = dialog.findViewById(R.id.edit_text_data);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        dialogTitle.setText("Введите новое значение");
        if (field.equals("password")) {
            editTextData.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editTextData.setHint("Новое значение");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newValue = editTextData.getText().toString();
            if (newValue.isEmpty()) {
                Toast.makeText(this, "Поле не может быть пустым", Toast.LENGTH_SHORT).show();
                return;
            }

            if (field.equals("email") && newValue.equals(user.getEmail())) {
                Toast.makeText(this, "Вы ввели тот же адрес электронной почты.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user == null) {
                Toast.makeText(this, "Не удалось обновить данные. Попробуйте снова.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (field.equals("password")) {
                user.updatePassword(newValue)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PrivacyActivity.this, "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(PrivacyActivity.this, "Ошибка при смене пароля", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else if (field.equals("email")) {
                user.verifyBeforeUpdateEmail(newValue)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PrivacyActivity.this, "Письмо с подтверждением отправлено на новую почту.", Toast.LENGTH_LONG).show();
                                refreshEmailButton.setVisibility(ImageButton.VISIBLE);
                                dialog.dismiss();
                            } else {
                                String errorMessage = "Неизвестная ошибка";
                                if (task.getException() != null) {
                                    errorMessage = task.getException().getClass().getSimpleName() + ": " + task.getException().getMessage();
                                }
                                Toast.makeText(PrivacyActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                db.collection("users").document(user.getUid())
                        .update(field, newValue)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(PrivacyActivity.this, "Данные успешно обновлены", Toast.LENGTH_SHORT).show();
                            loadUserData();
                            dialog.dismiss();
                        });
            }
        });
        dialog.show();
    }
}
