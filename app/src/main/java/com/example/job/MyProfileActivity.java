package com.example.job;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            usernameTextView.setText(documentSnapshot.getString("username"));
                            phoneTextView.setText(documentSnapshot.getString("phone"));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MyProfileActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
