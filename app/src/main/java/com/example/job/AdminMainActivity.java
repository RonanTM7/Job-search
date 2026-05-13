package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_admin_main);

        findViewById(R.id.btn_users).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminUsersActivity.class));
        });

        findViewById(R.id.btn_chats).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminChatsActivity.class));
        });

        findViewById(R.id.btn_exit_admin).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
