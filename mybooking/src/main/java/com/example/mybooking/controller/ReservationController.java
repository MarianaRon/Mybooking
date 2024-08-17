package com.example.mybooking.controller;

import com.example.mybooking.model.Reservation;
import com.example.mybooking.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Optional<Reservation> reservation = reservationService.getReservationById(id);
        if (reservation.isPresent()) {
            return ResponseEntity.ok(reservation.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservation) {
        return reservationService.saveReservation(reservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable Long id, @RequestBody Reservation reservationDetails) {
        Optional<Reservation> optionalReservation = reservationService.getReservationById(id);
        if (optionalReservation.isPresent()) {
            Reservation reservation = optionalReservation.get();
            reservation.setUser(reservationDetails.getUser());
            reservation.setRoom(reservationDetails.getRoom());
            reservation.setCheckInDate(reservationDetails.getCheckInDate());
            reservation.setCheckOutDate(reservationDetails.getCheckOutDate());
            reservation.setReservationDate(reservationDetails.getReservationDate());
            reservation.setApprovalDate(reservationDetails.getApprovalDate());
            reservation.setTotalPrice(reservationDetails.getTotalPrice());
            return ResponseEntity.ok(reservationService.saveReservation(reservation));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}