package com.example.job.model;

public class ChatMeta {
    private String chatId;
    private String userName;
    private String lastMessage;
    private long timestamp;

    public ChatMeta() {}

    public String getChatId() { return chatId; }
    public String getUserName() { return userName; }
    public String getLastMessage() { return lastMessage; }
    public long getTimestamp() { return timestamp; }
}
