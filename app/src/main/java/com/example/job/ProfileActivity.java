package com.example.job;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameTextView, emailTextView, phoneTextView;
    private Button editEmailButton, editPhoneButton, changePasswordButton, buttonLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        editEmailButton = findViewById(R.id.editEmailButton);
        editPhoneButton = findViewById(R.id.editPhoneButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        buttonLogout = findViewById(R.id.buttonLogout);
        loadUserProfile();

        editEmailButton.setOnClickListener(v -> showEditDialog("email"));
        editPhoneButton.setOnClickListener(v -> showEditDialog("phone"));
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        buttonLogout.setOnClickListener(v -> logout());
    }
    private void loadUserProfile() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            usernameTextView.setText(documentSnapshot.getString("username"));
                            emailTextView.setText(documentSnapshot.getString("email"));
                            phoneTextView.setText(documentSnapshot.getString("phone"));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void showEditDialog(String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить " + field);

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            updateField(field, newValue);
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateField(String field, String newValue) {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .update(field, newValue)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Данные обновлены", Toast.LENGTH_SHORT).show();
                        loadUserProfile();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Сменить пароль");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();
            if (newPassword.length() >= 6) {
                currentUser.updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Пароль изменен", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Ошибка: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Пароль должен содержать не менее 6 символов", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
