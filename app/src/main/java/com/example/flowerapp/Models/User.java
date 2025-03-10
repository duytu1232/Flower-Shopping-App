package com.example.flowerapp.Models;

public class User {
    private int userId;
    private String username;
    private String password; // Thêm trường password
    private String email;
    private String role;
    private String status;
    private String fullName;
    private String phone;
    private String avatarUri;

    public User() {
    }

    // Constructor với 8 tham số (giữ nguyên)
    public User(int userId, String username, String email, String role, String status, String fullName, String phone, String avatarUri) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.status = status;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarUri = avatarUri;
    }

    // Constructor mới với 9 tham số, bao gồm password
    public User(int userId, String username, String password, String email, String role, String status, String fullName, String phone, String avatarUri) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = status;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarUri = avatarUri;
    }

    // Getters và Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; } // Thêm getter cho password
    public void setPassword(String password) { this.password = password; } // Thêm setter cho password

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarUri() { return avatarUri; }
    public void setAvatarUri(String avatarUri) { this.avatarUri = avatarUri; }
}