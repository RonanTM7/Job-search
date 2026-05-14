package job.search.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import job.search.app.adapter.MessageAdapter;
import job.search.app.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerMessages;
    private MessageAdapter adapter;
    private EditText editMessage;
    private FirebaseFirestore db;
    private String chatId;
    private String currentUserId;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        // Default guest ID to avoid crash
        currentUserId = "guest_" + androidId;
        chatId = "guest_" + androidId;

        if (user != null) {
            currentUserId = user.getUid();
            chatId = user.getUid();
            isAdmin = "ronanauf@gmail.com".equals(user.getEmail());

            // If admin is opening a specific chat
            String targetChatId = getIntent().getStringExtra("CHAT_ID");
            if (isAdmin && targetChatId != null) {
                chatId = targetChatId;
                String userName = getIntent().getStringExtra("USER_NAME");
                if (userName != null) {
                    ((TextView)findViewById(R.id.chat_title)).setText(userName);
                }
            }

            if (user.isAnonymous()) {
                registerGuestInFirestore(androidId);
            }
        } else {
            // This case handles user == null but normally LoginActivity signs in anonymously
            registerGuestInFirestore(androidId);
        }

        recyclerMessages = findViewById(R.id.recycler_messages);
        editMessage = findViewById(R.id.edit_message);
        ImageButton btnSend = findViewById(R.id.btn_send);

        adapter = new MessageAdapter(currentUserId);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        return;
                    }
                    if (snapshot == null) return;
                    List<Message> messages = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        Message msg = doc.toObject(Message.class);
                        if (msg != null) {
                            messages.add(msg);
                        }
                    }

                    // Extra sort to be sure
                    Collections.sort(messages, (m1, m2) -> Long.compare(m1.getTimestampLong(), m2.getTimestampLong()));

                    adapter.setMessages(messages);
                    if (!messages.isEmpty()) {
                        recyclerMessages.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void registerGuestInFirestore(String androidId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.isAnonymous()) {
            boolean chatIdChanged = !currentUserId.equals(user.getUid());
            currentUserId = user.getUid();
            chatId = user.getUid();

            if (chatIdChanged) {
                adapter = new MessageAdapter(currentUserId);
                recyclerMessages.setAdapter(adapter);
                loadMessages();
            }

            java.util.Map<String, Object> guest = new java.util.HashMap<>();
            guest.put("username", "Гость (" + currentUserId.substring(0, Math.min(4, currentUserId.length())) + ")");
            guest.put("status", "active");
            guest.put("email", "guest_" + currentUserId + "@anonymous.auth");
            guest.put("phone", "N/A");

            db.collection("users").document(currentUserId).set(guest, com.google.firebase.firestore.SetOptions.merge())
                    .addOnFailureListener(e -> android.util.Log.e("ChatActivity", "Guest reg failed", e));
        } else if (user == null) {
            // If still no user, try to sign in anonymously
            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(authResult -> {
                FirebaseUser newUser = authResult.getUser();
                if (newUser != null) {
                    currentUserId = newUser.getUid();
                    chatId = newUser.getUid();
                    registerGuestInFirestore(androidId);
                }
            });
        }
    }

    private void sendMessage() {
        String text = editMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String messageId = db.collection("chats").document(chatId).collection("messages").document().getId();
        Message message = new Message(messageId, currentUserId, text, FieldValue.serverTimestamp(), isAdmin);

        db.collection("chats").document(chatId).collection("messages").document(messageId).set(message);

        // Update chat metadata for admin list
        java.util.Map<String, Object> chatMeta = new java.util.HashMap<>();
        chatMeta.put("lastMessage", text);
        chatMeta.put("timestamp", FieldValue.serverTimestamp());
        chatMeta.put("chatId", chatId);

        if (!isAdmin) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                    chatMeta.put("userName", doc.getString("username") != null ? doc.getString("username") : "Пользователь");
                    db.collection("chats").document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge());
                }).addOnFailureListener(e -> {
                    chatMeta.put("userName", "Пользователь (ошибка)");
                    db.collection("chats").document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge());
                });
            } else {
                chatMeta.put("userName", "Гость");
                db.collection("chats").document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge());
            }
        } else {
            db.collection("chats").document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge());
        }

        editMessage.setText("");
    }
}
