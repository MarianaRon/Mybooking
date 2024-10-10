package com.example.mybooking.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Пользователь, который сделал бронирование
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // Номер, который был забронирован
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    // Дата и время заезда
    @Column(nullable = false)
    private LocalDateTime checkInDate;
    // Дата и время выезда
    @Column(nullable = false)
    private LocalDateTime checkOutDate;
    // Дата и время создания бронирования
    @Column(nullable = false)
    private LocalDateTime reservationDate;

    //дата підтвердження запиту
    private LocalDate approvalDate;

    // Общая стоимость бронирования
    @Column(nullable = false)
    private Double totalPrice;

    // Конструктор за замовчуванням
    public Reservation() {}

    // Повний конструктор
    public Reservation(User user, Room room, LocalDateTime checkInDate, LocalDateTime checkOutDate,
                       LocalDateTime reservationDate, LocalDate approvalDate, Boolean isApproved, Double totalPrice) {
        this.user = user;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.reservationDate = reservationDate;
        //this.dateDeparture = dateDeparture;
        this.approvalDate = approvalDate;
        //this.isApproved = isApproved;
        this.totalPrice = totalPrice;
    }

    // Геттери та сеттери
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDateTime getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDateTime checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDateTime getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDateTime checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    //public LocalDate getDateDeparture() { return dateDeparture; }
    //public void setDateDeparture(LocalDate dateDeparture) { this.dateDeparture = dateDeparture; }
    //public Boolean getIsApproved() {return isApproved;}
    // public void setIsApproved(Boolean isApproved) {this.isApproved = isApproved; }
    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
    }



    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
