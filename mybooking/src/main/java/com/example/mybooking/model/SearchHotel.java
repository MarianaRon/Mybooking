package com.example.mybooking.model;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class SearchHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int guests;

    // Конструктори, геттери і сеттери
    public SearchHotel() {}

    public SearchHotel(City city, LocalDate date, int guests) {
        this.city = city;
        this.date = date;
        this.guests = guests;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public City getCity() { return city; }

    public void setCity(City city) { this.city = city; }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public int getGuests() { return guests; }

    public void setGuests(int guests) { this.guests = guests; }
}

