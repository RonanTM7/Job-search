package com.example.job;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;
import com.example.job.utils.CustomToast;

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

        Button myResumeButton = findViewById(R.id.btn_my_resume);
        Button logoutButton = findViewById(R.id.btn_logout);

        myResumeButton.setOnClickListener(v -> {
            if (currentUser != null && currentUser.isAnonymous()) {
                CustomToast.showToast(MyProfileActivity.this, "Для начала зарегистрируйтесь", 4000);
                Intent intent = new Intent(MyProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                startActivity(new Intent(MyProfileActivity.this, ResumeActivity.class));
            }
        });

        if (currentUser != null && currentUser.isAnonymous()) {
            usernameTextView.setText("Гость");
            phoneTextView.setText("Не авторизован");
            logoutButton.setText("Войти");
            logoutButton.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
        } else {
            loadUserProfile();
        }

        logoutButton.setOnClickListener(v -> {
            if (currentUser != null && currentUser.isAnonymous()) {
                Intent intent = new Intent(MyProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_logout);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

                Button btnNo = dialog.findViewById(R.id.btn_no);
                Button btnYes = dialog.findViewById(R.id.btn_yes);

                btnNo.setOnClickListener(view -> dialog.dismiss());
                btnYes.setOnClickListener(view -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MyProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    dialog.dismiss();
                    finish();
                });

                dialog.show();
            }
        });

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
                    .addOnFailureListener(e -> CustomToast.showToast(MyProfileActivity.this, "Ошибка загрузки данных", 4000));
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