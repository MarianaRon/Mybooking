package com.example.mybooking.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Hotel {

    //Уникальный идентификатор отеля. Автоматически генерируется базой данных.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /////
    // Название отеля. Обязательное поле.
    @Column(nullable = false)
    private String name;
    /////
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    /////
    @Column(nullable = false)
    private String address;
    //////
    //Географическая широта отеля. Может использоваться для отображения на карте.
    private Double latitude;
    // Географическая долгота отеля. Может использоваться для отображения на карте.
    private Double longitude;


    //Владелец отеля. Ссылается на сущность User, представляющую владельца. Обязательное поле.
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    //Список номеров в отеле. Ссылается на сущность Room.Указывает, что отель может содержать множество номеров.
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Room> rooms;


    //Список отзывов об отеле. Ссылается на сущность Review. Указывает, что отель может содержать множество отзывов.
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews;

    //Набор изображений отеля.
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Image> images;

    //Список удобств, доступных в отеле. Ссылается на сущность Amenity. Указывает, что отель может предлагать множество удобств.
    @ManyToMany
    @JoinTable(
            name = "hotel_amenity",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities;
    @Column(nullable = false)
    private String housingType;


    //Список изображений отеля. Ссылается на сущность Image. Указывает, что отель может содержать множество изображений.
    //@OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    //private List<Image> images;



    public Hotel() {
    }



//    public Hotel(String name, String description, String address, Double latitude, Double longitude, User owner, HashSet<Room> rooms, String housingType, Set<String> reviewsSet, Set<String> amenitiesSet, Set<String> imagesSet) {
//        this.name = name;
//        this.description = description;
//        this.address = address;
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.owner = owner;
//        this.rooms = rooms;
//        this.housingType = housingType;
//        this.reviews = reviews;
//        this.images = images;
//        this.amenities = amenities;
//
//    }

    public Hotel(String name, String description, String address, Double latitude, Double longitude, User ownerUser, String housingType) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.owner = ownerUser;
        this.housingType = housingType;
        this.rooms = rooms;
        this.reviews = reviews;
        this.images = images;
        this.amenities = amenities;
    }

    // Геттери та сеттери
    public String getHousingType() {
        return housingType;
    }


    public void setHousingType(String housingType) {
        this.housingType = housingType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /////
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    /////

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    //координати
    public Double getLongitude() {
        return longitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }



    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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

    public Set<Amenity> getAmenities() {
        return amenities;
    }

    public void setAmenities(Set<Amenity> amenities) {
        this.amenities = amenities;
    }

    ////
    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }
}
