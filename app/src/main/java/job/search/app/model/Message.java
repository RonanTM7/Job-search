package job.search.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {
    private String id;
    private String senderId;
    private String text;
    @ServerTimestamp
    private Object timestamp;
    private boolean admin;

    public Message() {}

    public Message(String id, String senderId, String text, Object timestamp, boolean admin) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.admin = admin;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Object getTimestamp() { return timestamp; }

    public long getTimestampLong() {
        if (timestamp instanceof Long) return (Long) timestamp;
        if (timestamp instanceof Timestamp) return ((Timestamp) timestamp).toDate().getTime();
        if (timestamp instanceof Date) return ((Date) timestamp).getTime();
        return System.currentTimeMillis(); // Fallback for local messages without server timestamp yet
    }

    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }

    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }
}
