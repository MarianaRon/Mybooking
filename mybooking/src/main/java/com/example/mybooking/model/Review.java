package com.example.mybooking.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String content;
    @Column(nullable = false)
    private Integer rating;
    @Column(nullable = false)
    private LocalDateTime reviewDate;
    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public Review() {
    }

    public Review(String content, Integer rating, LocalDateTime reviewDate, Hotel hotel, User user) {
        this.content = content;
        this.rating = rating;
        this.reviewDate = reviewDate;
        this.hotel = hotel;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public String getComment() {
//        return content;
//    }
//
//    public void setComment(String comment) {
//        this.content = comment;
//    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
