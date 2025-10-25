package com.example.job;

import android.app.Dialog;
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
                            showNewValueDialog(field);
                        } else {
                            Toast.makeText(PrivacyActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        dialog.show();
    }

    private void showNewValueDialog(String field) {
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

            if (field.equals("password")) {
                currentUser.updatePassword(newValue)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PrivacyActivity.this, "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
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
                            dialog.dismiss();
                        });
            }
        });
        dialog.show();
    }
}
