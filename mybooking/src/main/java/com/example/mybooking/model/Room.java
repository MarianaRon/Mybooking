package com.example.mybooking.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity

public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Тип номера, например, "Одноместный", "Двухместный", "Люкс". Обязательное поле.
    @Column(nullable = false)
    private String type;

    //Цена за номер за ночь. Обязательное поле.
    @Column(nullable = false)
    private Double price;

    //Вместимость номера (количество человек). Обязательное поле.
    @Column(nullable = false)
    private Integer capacity;


    //Отель, к которому принадлежит номер. Ссылается на сущность Hotel. Обязательное поле.
    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;


    //Список бронирований для этого номера. Ссылается на сущность Reservation. Указывает, что номер может иметь множество бронирований.
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reservation> reservations;


    //Список изображений номера. Ссылается на сущность Image. Указывает, что номер может иметь множество изображений.
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Image> images;


    // Конструктори, геттери та сеттери

    public Room() {
    }

    public Room(String type, Double price, Integer capacity, Hotel hotel) {
        this.type = type;
        this.price = price;
        this.capacity = capacity;
        this.hotel = hotel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }
}
