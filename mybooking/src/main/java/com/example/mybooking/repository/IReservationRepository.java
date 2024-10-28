package com.example.mybooking.repository;

import com.example.mybooking.model.Reservation;
import com.example.mybooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByRoomId(Long roomId);
}