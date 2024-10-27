package com.example.mybooking.repository;

import com.example.mybooking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IRoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);
}
