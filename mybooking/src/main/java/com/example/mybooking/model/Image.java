package com.example.mybooking.model;

import jakarta.persistence.*;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Lob
    @Column(nullable = false)
    private byte[] photoBytes; // Додавання поля для зберігання байтових даних

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;// Связь с отелем

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    // Конструктори, геттери та сеттери

    public Image() {
    }

    public Image(String url, byte[] photoBytes, Hotel hotel, Room room) {
        this.url = url;
        this.photoBytes = photoBytes;
        this.hotel = hotel;
        this.room = room;
    }

    // Геттери і сеттери
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getPhotoBytes() {
        return photoBytes;
    }

    public void setPhotoBytes(byte[] photoBytes) {
        this.photoBytes = photoBytes;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
