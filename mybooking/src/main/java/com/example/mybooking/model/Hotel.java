
package com.example.mybooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "address_street", nullable = false)
    private String addressStreet;

    @Column
    private double latitude;

    @Column
    private double longitude;

    @Column(nullable = false)
    private Double price;

//    @Column
//    private String coverUrl;

    @Lob
    @Column(nullable = false)
    private byte[] coverImage;


    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    @JsonIgnore
    private City city;

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner owner;

    @Column(nullable = false)
    private String housingType;

    @ManyToMany
    @JoinTable(
            name = "hotel_amenities",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Image> images = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Room> rooms = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

    // Конструктор без параметров
    public Hotel() {
    }

    // Конструктор с параметрами
    public Hotel(String name, String addressStreet, Double price, City city, Partner owner, Set<Amenity> amenities) {
        this.name = name;
        this.addressStreet = addressStreet;
        this.price = price;
        this.city = city;
        this.owner = owner;
        this.amenities = amenities != null ? amenities : new HashSet<>();

    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
//
//    public String getCoverUrl() {
//        return coverUrl;
//    }
//
//    public void setCoverUrl(String coverUrl) {
//        this.coverUrl = coverUrl;
//    }

    public byte[] getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(byte[] coverImage) {
        this.coverImage = coverImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Partner getOwner() {
        return owner;
    }

    public void setOwner(Partner owner) {
        this.owner = owner;
    }

    public String getHousingType() {
        return housingType;
    }

    public void setHousingType(String housingType) {
        this.housingType = housingType;
    }

    public Set<Amenity> getAmenities() {
        return amenities;
    }

    public void setAmenities(Set<Amenity> amenities) {
        this.amenities = amenities;
    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

    public Set<Room> getRooms() {
        return rooms;
    }

    public void setRooms(Set<Room> rooms) {
        this.rooms = rooms;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }
}
//package com.example.mybooking.model;

//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//public class Hotel {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//
//    @Column(nullable = false)
//    private String name;
//
//    @Column(name = "address_street", nullable = false)
//    private String addressStreet;
//
//    @Column
//    private double latitude;
//
//    @Column
//    private double longitude;
//
//    @Column(nullable = false)
//    private Double price;
//
//    @Column
//    private String coverUrl;
//
//    @Lob
//    @Column(nullable = false)
//    private byte[] coverImage;  // Обложка отеля в байтовом формате
//
//    @Transient
//    private String coverImageBase64; // Новое поле для хранения изображения в Base64
//
//    @Column
//    private String description;
//
//    @ManyToOne
//    @JoinColumn(name = "city_id", nullable = false)
//    @JsonIgnore  // Ігноруємо це поле при серіалізації
//    private City city;
//
//    @ManyToOne
//    @JoinColumn(name = "partner_id", nullable = false)
//    private Partner owner;
//
//    @Column
//    private String housingType;
//
//    @ManyToMany
//    @JoinTable(
//            name = "hotel_amenities",// Имя промежуточной таблицы
//            joinColumns = @JoinColumn(name = "hotel_id"),// Связь с таблицей Hotel
//            inverseJoinColumns = @JoinColumn(name = "amenity_id")// Связь с таблицей Amenity
//    )
//    private Set<Amenity> amenities = new HashSet<>();
//
//    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Image> images = new HashSet<>();
//
//    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Room> rooms = new HashSet<>(); // Связанные изображения
//
//    // Додаємо зв'язок з відгуками
//    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Review> reviews = new HashSet<>();
//
//    public Hotel(String name, String addressStreet, Double price, City city, Partner owner,Set<Amenity> amenities, byte[] coverImage, Set<Image> images) {
//        this.name = name;
//        this.addressStreet = addressStreet;
//        this.price = price;
//        this.city = city;
//        this.owner = owner;
//        this.amenities = amenities != null ? amenities : new HashSet<>();
//        this.coverImage = coverImage;
//        this.images = images != null ? images : new HashSet<>(); // Инициализируем изображения, если они не null
//    }
//
//}
//
//
