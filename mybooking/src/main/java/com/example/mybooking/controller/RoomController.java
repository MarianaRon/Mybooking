package com.example.mybooking.controller;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Room;
import com.example.mybooking.service.HotelService;
import com.example.mybooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private HotelService hotelService;

    @GetMapping("/room_list")
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("hotels", hotelService.getAllHotels()); // Передаємо список готелів для форми
        return "rooms/room_list";
    }

    @GetMapping("/edit/{id}")
    public String editRoomForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id: " + id));
        model.addAttribute("room", room);
        model.addAttribute("hotels", hotelService.getAllHotels()); // Передаємо список готелів для редагування
        return "rooms/edit_room";
    }

    @PostMapping("/edit/{id}")
    public String updateRoom1(@PathVariable Long id, @ModelAttribute Room roomDetails) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id: " + id));

        room.setType(roomDetails.getType());
        room.setPrice(roomDetails.getPrice());
        room.setCapacity(roomDetails.getCapacity());
        room.setHotel(roomDetails.getHotel());

        roomService.saveRoom(room);
        return "redirect:/rooms/room_list";
    }

    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return "redirect:/rooms/room_list";
    }

    @PostMapping("/add")
    public String addRoom(@RequestParam String type,
                          @RequestParam Double price,
                          @RequestParam Integer capacity,
                          @RequestParam Long hotelId) {
        Hotel hotel = hotelService.getHotelById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid hotel Id: " + hotelId));
        Room room = new Room(type, price, capacity, hotel);
        roomService.saveRoom(room);
        return "redirect:/rooms/room_list";
    }

    @GetMapping
    public String getAllRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        return "rooms/room_list";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Optional<Room> room = roomService.getRoomById(id);
        return room.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        Room savedRoom = roomService.saveRoom(room);
        return ResponseEntity.ok(savedRoom);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom2(@PathVariable Long id, @RequestBody Room roomDetails) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id: " + id));

        room.setType(roomDetails.getType());
        room.setPrice(roomDetails.getPrice());
        room.setCapacity(roomDetails.getCapacity());
        room.setHotel(roomDetails.getHotel());
        room.setReservations(roomDetails.getReservations());
        room.setImages(roomDetails.getImages());

        Room updatedRoom = roomService.saveRoom(room);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomRest(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
