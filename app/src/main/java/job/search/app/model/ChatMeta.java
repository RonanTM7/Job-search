package job.search.app.model;

import com.google.firebase.Timestamp;
import java.util.Date;

public class ChatMeta {
    private String chatId;
    private String userName;
    private String lastMessage;
    private Object timestamp;

    public ChatMeta() {}

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Object getTimestamp() { return timestamp; }

    public long getTimestampLong() {
        if (timestamp instanceof Long) return (Long) timestamp;
        if (timestamp instanceof Timestamp) return ((Timestamp) timestamp).toDate().getTime();
        if (timestamp instanceof Date) return ((Date) timestamp).getTime();
        return 0;
    }

    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }
}
