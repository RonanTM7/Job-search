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
import android.text.TextWatcher;
import android.text.Editable;
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
        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(PrivacyActivity.this, CheckCurrentPasswordActivity.class);
            startActivity(intent);
        });
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

                        db.collection("users").document(userId).update("email", newEmail)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(PrivacyActivity.this, "Почта успешно обновлена.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                });

                        loadUserData();
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
            emailTextView.setText(user.getEmail());

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
        TextView textError = dialog.findViewById(R.id.text_error);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        dialogTitle.setText("Введите текущий пароль");
        editTextData.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editTextData.setHint("Текущий пароль");
        btnSave.setText("Подтвердить");

        editTextData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textError.setVisibility(TextView.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String password = editTextData.getText().toString();
            if (password.isEmpty()) {
                textError.setText("Пароль не может быть пустым");
                textError.setVisibility(TextView.VISIBLE);
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), password);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            FirebaseUser freshUser = FirebaseAuth.getInstance().getCurrentUser();
                            showNewValueDialog(field, freshUser, password);
                        } else {
                            textError.setText("Неверный пароль");
                            textError.setVisibility(TextView.VISIBLE);
                        }
                    });
        });
        dialog.show();
    }

    private void showNewValueDialog(String field, FirebaseUser user, String oldPassword) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_data);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        EditText editTextData = dialog.findViewById(R.id.edit_text_data);
        TextView textError = dialog.findViewById(R.id.text_error);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSave = dialog.findViewById(R.id.btn_save);

        dialogTitle.setText("Введите новое значение");
        if (field.equals("phone")) {
            editTextData.setInputType(InputType.TYPE_CLASS_PHONE);
            editTextData.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    textError.setVisibility(TextView.GONE);
                    if (!s.toString().startsWith("+7")) {
                        editTextData.setText("+7");
                        editTextData.setSelection(editTextData.getText().length());
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

                        editTextData.removeTextChangedListener(this);
                        editTextData.setText(formatted.toString());
                        editTextData.setSelection(formatted.length());
                        editTextData.addTextChangedListener(this);
                    }
                }
            });
        }
        editTextData.setHint("Новое значение");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newValue = editTextData.getText().toString();
            if (newValue.isEmpty()) {
                textError.setText("Поле не может быть пустым");
                textError.setVisibility(TextView.VISIBLE);
                return;
            }

            if (field.equals("email") && newValue.equals(user.getEmail())) {
                textError.setText("Вы ввели тот же адрес электронной почты.");
                textError.setVisibility(TextView.VISIBLE);
                return;
            }

            if (user == null) {
                textError.setText("Не удалось обновить данные. Попробуйте снова.");
                textError.setVisibility(TextView.VISIBLE);
                return;
            }

            if (field.equals("email")) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newValue).matches()) {
                    textError.setText("Введите корректный адрес электронной почты");
                    textError.setVisibility(TextView.VISIBLE);
                    return;
                }
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
                                textError.setText("Ошибка: " + errorMessage);
                                textError.setVisibility(TextView.VISIBLE);
                            }
                        });
            } else if (field.equals("phone")) {
                if (newValue.equals(phoneNumberTextView.getText().toString())) {
                    textError.setText("Номер должен отличаться от используемого сейчас");
                    textError.setVisibility(TextView.VISIBLE);
                    return;
                }
                if (newValue.length() != 16) {
                    textError.setText("Введите корректный номер телефона");
                    textError.setVisibility(TextView.VISIBLE);
                    return;
                }
                db.collection("users").document(user.getUid())
                        .update(field, newValue)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(PrivacyActivity.this, "Данные успешно обновлены", Toast.LENGTH_SHORT).show();
                            loadUserData();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            textError.setText("Не удалось обновить номер телефона.");
                            textError.setVisibility(TextView.VISIBLE);
                        });
            }
        });
        dialog.show();
    }
}