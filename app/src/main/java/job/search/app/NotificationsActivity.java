package job.search.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import job.search.app.adapter.NotificationAdapter;
import job.search.app.model.Notification;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView tvNoNotifications;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        ImageButton btnBack = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.notificationsRecyclerView);
        tvNoNotifications = findViewById(R.id.tv_no_notifications);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadNotifications();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this::onNotificationClick, this::onDeleteClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        if (userId == null) return;

        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    List<Notification> notifications = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Notification notification = doc.toObject(Notification.class);
                            notification.setId(doc.getId());
                            notifications.add(notification);
                        }
                    }

                    if (notifications.isEmpty()) {
                        tvNoNotifications.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        // Sort by timestamp descending
                        notifications.sort((n1, n2) -> {
                            if (n1.getTimestamp() == null || n2.getTimestamp() == null) return 0;
                            return n2.getTimestamp().compareTo(n1.getTimestamp());
                        });

                        tvNoNotifications.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setNotifications(notifications);
                    }
                });
    }

    private void onNotificationClick(Notification notification) {
        if ("chat".equals(notification.getType())) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("CHAT_ID", notification.getRelatedId());
            intent.putExtra("USER_NAME", notification.getSenderName());
            intent.putExtra("IS_EMPLOYER_CHAT", notification.isEmployerChat());
            startActivity(intent);
        } else if ("application".equals(notification.getType())) {
            // Depending on role, redirect to vacancy details or applications
            String role = getSharedPreferences("AppSettings", MODE_PRIVATE).getString("userRole", "seeker");
            if ("employer".equals(role)) {
                Intent intent = new Intent(this, EmployerVacancyDetailActivity.class);
                intent.putExtra("vacancyId", notification.getRelatedId());
                startActivity(intent);
            } else {
                // Seekers can just go to applications fragment via MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("open_applications", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    private void onDeleteClick(Notification notification) {
        db.collection("notifications").document(notification.getId()).delete();
    }
}
