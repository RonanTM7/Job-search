package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.job.adapter.AdminUserAdapter;
import com.example.job.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity implements AdminUserAdapter.OnUserActionListener {

    private AdminUserAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_admin_users);

        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerUsers = findViewById(R.id.recycler_admin_users);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(this);
        recyclerUsers.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadUsers();
    }

    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(snapshot -> {
            List<User> active = new ArrayList<>();
            List<User> blocked = new ArrayList<>();
            List<User> deleted = new ArrayList<>();

            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                String uid = doc.getId();
                User user = doc.toObject(User.class);
                if (user == null) continue;

                if (uid.startsWith("guest_") || (user.getEmail() != null && user.getEmail().endsWith("@anonymous.auth"))) continue;

                user.setUid(uid);
                if ("ronanauf@gmail.com".equals(user.getEmail())) continue;

                String status = user.getStatus();
                if ("blocked".equals(status)) blocked.add(user);
                else if ("deleted".equals(status)) deleted.add(user);
                else active.add(user);
            }

            List<User> all = new ArrayList<>();
            all.addAll(active);
            all.addAll(blocked);
            all.addAll(deleted);
            adapter.setUsers(all);
        }).addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки юзеров: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onResetPassword(User user) {
        String email = user.getEmail();
        if (email == null || email.isEmpty() || email.startsWith("deleted_")) {
            Toast.makeText(this, "Некорректный email или пользователь удален", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Ссылка отправлена на " + email, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBlockToggle(User user) {
        String newStatus = "blocked".equals(user.getStatus()) ? "active" : "blocked";
        db.collection("users").document(user.getUid()).update("status", newStatus)
                .addOnSuccessListener(aVoid -> loadUsers());
    }

    @Override
    public void onDelete(User user) {
        String originalEmail = user.getEmail();
        String deletedMarker = "deleted_" + System.currentTimeMillis() + "_" + originalEmail;

        db.collection("users").document(user.getUid())
                .update("status", "deleted", "email", deletedMarker)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Пользователь помечен как удаленный. Почта освобождена.", Toast.LENGTH_LONG).show();
                    loadUsers();
                });
    }
}
