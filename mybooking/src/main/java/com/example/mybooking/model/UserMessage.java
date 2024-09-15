package com.example.mybooking.model;

import jakarta.persistence.*;

@Entity
public class UserMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private String email; // Can be an email from both registered and unregistered users.

    public UserMessage() {
    }

    public UserMessage(String topic, String message, String email) {
        this.topic = topic;
        this.message = message;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
