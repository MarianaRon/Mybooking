package com.example.mybooking.model;

import jakarta.persistence.*;

import java.util.HashSet;
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
    @Column(nullable = true)
    private String description;
    @Column(nullable = true)
    private String coverUrl;
    @Lob
    @Column(name = "cover_image")
    private byte[] coverImage;
//    @Lob
//    @Column(nullable = true)
//    private byte[] coverImage;
//    @ManyToOne
//    @JoinColumn(name = "partner_id", nullable = false)
//    private Partner owner;



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
    @ManyToMany
    @JoinTable(
            name = "room_amenities",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();


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
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public byte[] getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(byte[] coverImage) {
        this.coverImage = coverImage;

    }
    public Set<Amenity> getAmenities() {
        return amenities;
    }

    public void setAmenities(Set<Amenity> amenities) {
        this.amenities = amenities;
    }

//    // Геттер и сеттер для поля owner (владелец номера)
//    public Partner getOwner() {
//        return owner;
//    }
//
//    public void setOwner(Partner owner) {
//        this.owner = owner;
//    }

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