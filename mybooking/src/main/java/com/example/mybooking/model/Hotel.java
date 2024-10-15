package com.example.mybooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
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


    @Lob
    @Column(nullable = true)
    private byte[] coverImage;  // Обложка отеля в байтовом формате

    @Transient
    private String coverImageBase64; // Новое поле для хранения изображения в Base64

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    @JsonIgnore  // Ігноруємо це поле при серіалізації
    private City city;

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner owner;

    @Column
    private String housingType;

    @ManyToMany
    @JoinTable(
            name = "hotel_amenities",// Имя промежуточной таблицы
            joinColumns = @JoinColumn(name = "hotel_id"),// Связь с таблицей Hotel
            inverseJoinColumns = @JoinColumn(name = "amenity_id")// Связь с таблицей Amenity
    )
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Image> images = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Room> rooms = new HashSet<>(); // Связанные изображения

    // Додаємо зв'язок з відгуками
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

    public Hotel(String name, String addressStreet, Double price, City city, Partner owner, Set<Image> images) {
        this.name = name;
        this.addressStreet = addressStreet;
        this.price = price;
        this.city = city;
        this.owner = owner;
        this.images = images != null ? images : new HashSet<>(); // Инициализируем изображения, если они не null
    }
}


