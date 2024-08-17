package com.example.mybooking.controller;

import com.example.mybooking.model.Reservation;
import com.example.mybooking.model.User;
import com.example.mybooking.model.Room;
import com.example.mybooking.service.ReservationService;
import com.example.mybooking.service.UserService;
import com.example.mybooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/reservation_list")
    public String reservationList(Model model) {
        List<Reservation> reservations = reservationService.getAllReservations();
        List<User> users = userService.getAllUsers();
        List<Room> rooms = roomService.getAllRooms();

        model.addAttribute("reservations", reservations);
        model.addAttribute("users", users);
        model.addAttribute("rooms", rooms);
        return "reservations/reservation_list";
    }

    @PostMapping("/create")
    public String createReservation(
            @RequestParam Long userId,
            @RequestParam Long roomId,
            @RequestParam LocalDateTime checkInDate,
            @RequestParam LocalDateTime checkOutDate,
            @RequestParam Double totalPrice) {

        User user = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Room room = roomService.getRoomById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

        Reservation reservation = new Reservation(user, room, checkInDate, checkOutDate, LocalDateTime.now(), null, null, totalPrice);
        reservationService.saveReservation(reservation);
        return "redirect:/reservations/reservation_list";
    }

    @GetMapping("/edit/{id}")
    public String editReservation(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.getReservationById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        model.addAttribute("reservation", reservation);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "reservations/edit_reservation";
    }

    @PostMapping("/edit/{id}")
    public String updateReservation(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam Long roomId,
            @RequestParam LocalDateTime checkInDate,
            @RequestParam LocalDateTime checkOutDate,
            @RequestParam Double totalPrice) {

        Reservation reservation = reservationService.getReservationById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        User user = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Room room = roomService.getRoomById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);
        reservation.setTotalPrice(totalPrice);

        reservationService.saveReservation(reservation);
        return "redirect:/reservations/reservation_list";
    }

    @PostMapping("/delete/{id}")
    public String deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return "redirect:/reservations/reservation_list";
    }
}
