package com.example.mybooking.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String region;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Hotel> hotels;

    @Column
    private Integer numberOfHotels;

    @Column
    private String photoUrl;

    @Lob
    @Column
    private byte[] photoBytes;

    // Constructors
    public City() {
    }

    public City(String name, String region) {
        this.name = name;
        this.region = region;
    }

    // Getters and Setters

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Set<Hotel> getHotels() {
        return hotels;
    }

    public void setHotels(Set<Hotel> hotels) {
        this.hotels = hotels;
        this.numberOfHotels = (hotels != null) ? hotels.size() : 0; // Update number of hotels
    }

    public Integer getNumberOfHotels() {
        return numberOfHotels;
    }

    public void setNumberOfHotels(Integer numberOfHotels) {
        this.numberOfHotels = numberOfHotels;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public byte[] getPhotoBytes() {
        return photoBytes;
    }

    public void setPhotoBytes(byte[] photoBytes) {
        this.photoBytes = photoBytes;
    }
}
