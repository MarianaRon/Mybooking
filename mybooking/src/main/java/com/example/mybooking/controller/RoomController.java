package com.example.mybooking.controller;

import com.example.mybooking.model.Room;
import com.example.mybooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Optional<Room> room = roomService.getRoomById(id);
        if (room.isPresent()) {
            return ResponseEntity.ok(room.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        return roomService.saveRoom(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        Optional<Room> optionalRoom = roomService.getRoomById(id);
        if (optionalRoom.isPresent()) {
            Room room = optionalRoom.get();
            room.setType(roomDetails.getType());
            room.setPrice(roomDetails.getPrice());
            room.setCapacity(roomDetails.getCapacity());
            room.setHotel(roomDetails.getHotel());
            room.setReservations(roomDetails.getReservations());
            room.setImages(roomDetails.getImages());
            return ResponseEntity.ok(roomService.saveRoom(room));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
