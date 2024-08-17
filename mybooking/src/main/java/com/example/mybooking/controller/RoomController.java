package com.example.mybooking.controller;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Room;
import com.example.mybooking.service.HotelService;
import com.example.mybooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return "rooms/edit_room";
    }

    @PostMapping("/edit/{id}")
    public String updateRoom(@PathVariable Long id, @ModelAttribute Room roomDetails) {
        roomDetails.setId(id);
        roomService.saveRoom(roomDetails);
        return "redirect:/rooms/room_list";
    }

    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id: " + id));
        roomService.deleteRoom(id);
        return "redirect:/rooms/room_list";
    }

    // Новий метод для обробки форми додавання нової кімнати
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
}
