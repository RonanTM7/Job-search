package job.search.app.model;

import com.google.firebase.Timestamp;
import java.util.Date;

public class Message {
    private String id;
    private String senderId;
    private String text;
    private Object timestamp;
    private boolean admin;
    private boolean read;

    public Message() {}

    public Message(String id, String senderId, String text, Object timestamp, boolean admin) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.admin = admin;
        this.read = false;
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

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
