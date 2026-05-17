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
import job.search.app.utils.FcmSender;
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
    private boolean isEmployerChat = false;
    private String userRole;
    private com.google.firebase.firestore.ListenerRegistration messagesListener;

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
            isAdmin = "ronanauf@gmail.com".equals(user.getEmail()); // Legacy check

            userRole = getSharedPreferences("AppSettings", MODE_PRIVATE).getString("userRole", "seeker");
            if ("admin".equals(userRole)) isAdmin = true;

            isEmployerChat = getIntent().getBooleanExtra("IS_EMPLOYER_CHAT", false);

            // If admin or employer is opening a specific chat
            String targetChatId = getIntent().getStringExtra("CHAT_ID");
            if ((isAdmin || isEmployerChat) && targetChatId != null) {
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
        String collection = isEmployerChat ? "employer_chats" : "chats";
        messagesListener = db.collection(collection).document(chatId).collection("messages")
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
                        markMessagesAsRead(messages);
                    }
                });
        resetUnreadCount();
    }

    private void markMessagesAsRead(List<Message> messages) {
        String collection = isEmployerChat ? "employer_chats" : "chats";
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        boolean hasUpdates = false;

        for (Message msg : messages) {
            if (!msg.getSenderId().equals(currentUserId) && !msg.isRead()) {
                batch.update(db.collection(collection).document(chatId).collection("messages")
                        .document(msg.getId()), "read", true);
                hasUpdates = true;
            }
        }

        if (hasUpdates) {
            batch.commit();
        }
    }

    private void resetUnreadCount() {
        String collection = isEmployerChat ? "employer_chats" : "chats";
        String unreadField;

        if (isAdmin) {
            unreadField = "unreadCountAdmin";
        } else if ("employer".equals(userRole)) {
            unreadField = "unreadCountEmployer";
        } else {
            unreadField = "unreadCountSeeker";
        }

        db.collection(collection).document(chatId).update(unreadField, 0);
    }

    private void sendPushNotification(String text) {
        String recipientId;
        String collection;

        if (isEmployerChat) {
            String role = getSharedPreferences("AppSettings", MODE_PRIVATE).getString("userRole", "seeker");
            if ("employer".equals(role)) {
                // Employer is sending to Seeker
                recipientId = chatId.split("_")[0];
                collection = "seekers";
            } else {
                // Seeker is sending to Employer (we need to find employerId for this vacancy)
                db.collection("employer_chats").document(chatId).get().addOnSuccessListener(doc -> {
                    String employerId = doc.getString("employerId");
                    if (employerId != null) {
                        fetchTokenAndSend(employerId, "employers", text);
                    }
                });
                return;
            }
        } else if (isAdmin) {
            // Admin is sending to Seeker
            recipientId = chatId;
            collection = "seekers";
        } else {
            // Seeker is sending to Admin
            // Admin token handling is more complex (broadcast or specific admin?)
            // For now, let's assume we don't send push to admin or send to a fixed UID if known
            return;
        }

        fetchTokenAndSend(recipientId, collection, text);
    }

    private void fetchTokenAndSend(String uid, String collection, String text) {
        db.collection(collection).document(uid).get().addOnSuccessListener(doc -> {
            String token = doc.getString("fcmToken");
            if (token != null) {
                String senderName = isAdmin ? "Поддержка" : "Новое сообщение";
                FcmSender.sendNotification(token, senderName, text);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
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

            db.collection("seekers").document(currentUserId).set(guest, com.google.firebase.firestore.SetOptions.merge())
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

        String collection = isEmployerChat ? "employer_chats" : "chats";

        String messageId = db.collection(collection).document(chatId).collection("messages").document().getId();
        Message message = new Message(messageId, currentUserId, text, FieldValue.serverTimestamp(), isAdmin);

        db.collection(collection).document(chatId).collection("messages").document(messageId).set(message);

        sendPushNotification(text);

        // Update chat metadata for list (admin or employer)
        java.util.Map<String, Object> chatMeta = new java.util.HashMap<>();
        chatMeta.put("lastMessage", text);
        chatMeta.put("timestamp", FieldValue.serverTimestamp());
        chatMeta.put("chatId", chatId);

        if (isEmployerChat) {
            String role = getSharedPreferences("AppSettings", MODE_PRIVATE).getString("userRole", "seeker");
            if ("employer".equals(role)) {
                chatMeta.put("employerReplied", true);
                chatMeta.put("unreadCountSeeker", FieldValue.increment(1));
            } else {
                chatMeta.put("unreadCountEmployer", FieldValue.increment(1));
                // Ensure employerId is in ChatMeta for push notifications when seeker replies
                db.collection("applications").whereEqualTo("userId", currentUserId).whereEqualTo("vacancyId", chatId.substring(currentUserId.length() + 1)).get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                String employerId = querySnapshot.getDocuments().get(0).getString("employerId");
                                if (employerId != null) {
                                    db.collection(collection).document(chatId).update("employerId", employerId);
                                }
                            }
                        });
            }
            db.collection(collection).document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge());
        } else if (!isAdmin) {
            chatMeta.put("unreadCountAdmin", FieldValue.increment(1));
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                db.collection("seekers").document(user.getUid()).get().addOnSuccessListener(doc -> {
                    chatMeta.put("userName", doc.getString("username") != null ? doc.getString("username") : "Пользователь");
                    db.collection(collection).document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge());
                }).addOnFailureListener(e -> {
                    chatMeta.put("userName", "Пользователь (ошибка)");
                    db.collection(collection).document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge());
                });
            } else {
                chatMeta.put("userName", "Гость");
                db.collection(collection).document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge());
            }
        } else {
            chatMeta.put("unreadCountSeeker", FieldValue.increment(1));
            db.collection(collection).document(chatId).set(chatMeta, com.google.firebase.firestore.SetOptions.merge());
        }

        editMessage.setText("");
    }
}
