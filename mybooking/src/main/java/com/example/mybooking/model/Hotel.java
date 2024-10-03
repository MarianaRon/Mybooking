package com.example.mybooking.model;

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

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
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
    private Set<Amenity> amenities;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Image> images = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Room> rooms = new HashSet<>();

    // Додаємо зв'язок з відгуками
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

    public Hotel(String name, String addressStreet, Double price, City city, Partner owner) {
        this.name = name;
        this.addressStreet = addressStreet;
        this.price = price;
        this.city = city;
        this.owner = owner;
    }
}

//package com.example.mybooking.model;
//
//import jakarta.persistence.*;
//
//import java.util.HashSet;
//import java.util.Set;
//@Entity
//public class Hotel {
//
//    //Уникальный идентификатор отеля. Автоматически генерируется базой данных.
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    /////
//    // Название отеля. Обязательное поле.
//    @Column(nullable = false)
//    private String name;
//    /////
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String description; // Описание отеля
//    /////
//    @Column(nullable = false)
//    private String address;
//    //////
//    //Географическая широта отеля. Может использоваться для отображения на карте.
//    private Double latitude;
//    // Географическая долгота отеля. Может использоваться для отображения на карте.
//    private Double longitude;
//
//
//    //Владелец отеля.
//    @ManyToOne
//    @JoinColumn(name = "owner_id", nullable = false)
//    private Partner owner;
//
//    //Список номеров в отеле. Ссылается на сущность Room.Указывает, что отель может содержать множество номеров.
//    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Room> rooms;
//
//
//    //Список отзывов об отеле. Ссылается на сущность Review. Указывает, что отель может содержать множество отзывов.
//    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Review> reviews;
//
//    //Набор изображений отеля.
//    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Image> images;
//
//    //Список удобств, доступных в отеле. Ссылается на сущность Amenity. Указывает, что отель может предлагать множество удобств.
//    @ManyToMany
//    @JoinTable(
//            name = "hotel_amenity",
//            joinColumns = @JoinColumn(name = "hotel_id"),
//            inverseJoinColumns = @JoinColumn(name = "amenity_id")
//    )
//    private Set<Amenity> amenities;
//    @Column(nullable = false)
//    private String housingType;
//
////привязка до міста - готель може бути тільки в одному місті, але у місті може бути багато готелів
//    @ManyToOne
//    @JoinColumn(name = "city_id")
//    private City city;
//
//    @Column
//    private String addressCity; // Город
//
//    @Column
//    private String addressStreet; // Улица
//
//    @Column
//    private String additionalInfo; // Дополнительная информация об адресе
//
//    @Column
//    private Double price; // Цена
//
//
//    //Список изображений отеля. Ссылается на сущность Image. Указывает, что отель может содержать множество изображений.
//    //@OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
//    //private List<Image> images;
//
//
//
//    public Hotel() {
//        this.rooms = new HashSet<>();
//        this.reviews = new HashSet<>();
//        this.images = new HashSet<>();
//        this.amenities = new HashSet<>();
//    }
//
//    public Hotel(String name, String description, String address, Double latitude, Double longitude, Partner ownerPartner, String housingType, Double price) {
//        this();
//        this.name = name;
//        this.description = description;
//        this.address = address;
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.owner = ownerPartner;
//        this.housingType = housingType;
//        this.price = price;
//    }
//
//
//    // Геттери та сеттери
//    public String getHousingType() {
//        return housingType;
//    }
//
//
//    public void setHousingType(String housingType) {
//        this.housingType = housingType;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    /////
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//    /////
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getAddress() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address = address;
//    }
//
//    //координати
//    public Double getLatitude() {
//        return latitude;
//    }
//    public void setLatitude(Double latitude) {
//        this.latitude = latitude;
//    }
//
//    public Double getLongitude() {
//        return longitude;
//    }
//
//    public void setLongitude(Double longitude) {
//        this.longitude = longitude;
//    }
//
//    public Partner getOwner() {
//        return owner;
//    }
//
//    public void setOwner(Partner owner) {
//        this.owner = owner;
//    }
//
//    public Set<Room> getRooms() {
//        return rooms;
//    }
//
//    public void setRooms(Set<Room> rooms) {
//        this.rooms = rooms;
//    }
//
//    public Set<Review> getReviews() {
//        return reviews;
//    }
//
//    public void setReviews(Set<Review> reviews) {
//        this.reviews = reviews;
//    }
//
//    public Set<Amenity> getAmenities() {
//        return amenities;
//    }
//
//    public void setAmenities(Set<Amenity> amenities) {
//        this.amenities = amenities;
//    }
//
//    ////
//    public Set<Image> getImages() {
//        return images;
//    }
//
//    public void setImages(Set<Image> images) {
//        this.images = images;
//    }
//
//    public City getCity() {
//        return city;
//    }
//
//    public void setCity(City city) {
//        this.city = city;
//    }
//    // Новые геттеры и сеттеры для добавленных полей
//    public String getAddressCity() {
//        return addressCity;
//    }
//
//    public void setAddressCity(String addressCity) {
//        this.addressCity = addressCity;
//    }
//
//    public String getAddressStreet() {
//        return addressStreet;
//    }
//
//    public void setAddressStreet(String addressStreet) {
//        this.addressStreet = addressStreet;
//    }
//
//    public String getAdditionalInfo() {
//        return additionalInfo;
//    }
//
//    public void setAdditionalInfo(String additionalInfo) {
//        this.additionalInfo = additionalInfo;
//    }
//
//    public Double getPrice() {
//        return price;
//    }
//
//    public void setPrice(Double price) {
//        this.price = price;
//    }
//}
