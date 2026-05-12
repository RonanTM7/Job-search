package com.example.job.model;

public class User {
    private String uid;
    private String username;
    private String email;
    private String phone;
    private String status;

    public User() {}

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
}
