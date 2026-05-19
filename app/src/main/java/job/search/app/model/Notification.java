package job.search.app.model;

import com.google.firebase.Timestamp;

public class Notification {
    private String id;
    private String userId; // Target user
    private String title;
    private String message;
    private String type; // "chat", "application"
    private String relatedId; // chatId or vacancyId
    private boolean employerChat;
    private String senderName;
    private Timestamp timestamp;

    public Notification() {}

    public Notification(String id, String userId, String title, String message, String type, String relatedId, boolean employerChat, String senderName, Timestamp timestamp) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedId = relatedId;
        this.employerChat = employerChat;
        this.senderName = senderName;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }

    public boolean isEmployerChat() { return employerChat; }
    public void setEmployerChat(boolean employerChat) { this.employerChat = employerChat; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
