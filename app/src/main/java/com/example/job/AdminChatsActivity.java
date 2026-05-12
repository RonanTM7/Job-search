package com.example.job;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.job.adapter.AdminChatAdapter;
import com.example.job.model.ChatMeta;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class AdminChatsActivity extends AppCompatActivity {

    private RecyclerView recyclerChats;
    private AdminChatAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_admin_chats);

        db = FirebaseFirestore.getInstance();
        recyclerChats = findViewById(R.id.recycler_chats);
        recyclerChats.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminChatAdapter(chat -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("CHAT_ID", chat.getChatId());
            intent.putExtra("USER_NAME", chat.getUserName());
            startActivity(intent);
        });

        recyclerChats.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadChats();
    }

    private void loadChats() {
        db.collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        android.widget.Toast.makeText(this, "Ошибка чатов: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshot == null) return;

                    List<ChatMeta> chats = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        chats.add(doc.toObject(ChatMeta.class));
                    }
                    adapter.setChats(chats);
                });
    }
}
