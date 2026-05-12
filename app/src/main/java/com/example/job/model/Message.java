package com.example.job.model;

public class Message {
    private String id;
    private String senderId;
    private String text;
    private long timestamp;
    private boolean isAdmin;

    public Message() {}

    public Message(String id, String senderId, String text, long timestamp, boolean isAdmin) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.isAdmin = isAdmin;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public boolean isAdmin() { return isAdmin; }
}
