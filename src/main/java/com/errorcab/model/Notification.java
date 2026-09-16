package com.errorcab.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * In-app notification entity as specified in Section 24.
 */
public class Notification {
    private int id;
    private int userId;
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private boolean read;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    public Notification(int id, int userId, String title, String message, LocalDateTime timestamp, boolean read) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.read = read;
    }

    public Notification(int userId, String title, String message) {
        this(0, userId, title, message, LocalDateTime.now(), false);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getFormattedTime() {
        return timestamp != null ? timestamp.format(TIME_FORMATTER) : "";
    }
}
