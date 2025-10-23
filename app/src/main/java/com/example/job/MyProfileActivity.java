package com.example.job;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;


public class MyProfileActivity extends AppCompatActivity {

    private TextView usernameTextView, phoneTextView;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        usernameTextView = findViewById(R.id.tv_username);
        phoneTextView = findViewById(R.id.tv_phone_number);
        ImageButton backButton = findViewById(R.id.btn_back);

        loadUserProfile();

        findViewById(R.id.btn_logout).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Да", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MyProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Нет", null)
                .show());

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            usernameTextView.setText(documentSnapshot.getString("username"));
                            String phone = documentSnapshot.getString("phone");
                            if (phone != null) {
                                String formattedPhone = formatPhoneNumber(phone);
                                phoneTextView.setText(formattedPhone);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(MyProfileActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show());
        }
    }

    private String formatPhoneNumber(String phone) {
        String digitsOnly = phone.replaceAll("\\D", "");
        if (digitsOnly.length() == 11) {
            return "+7 " + digitsOnly.substring(1, 4) + " " + digitsOnly.substring(4, 7) + " " + digitsOnly.substring(7, 9) + " " + digitsOnly.substring(9);
        }
        return phone;
    }
}
