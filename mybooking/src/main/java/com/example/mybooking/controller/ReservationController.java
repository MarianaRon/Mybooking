package com.example.mybooking.controller;

import com.example.mybooking.model.Reservation;
import com.example.mybooking.model.User;
import com.example.mybooking.model.Room;
import com.example.mybooking.service.ReservationService;
import com.example.mybooking.service.UserService;
import com.example.mybooking.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

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

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

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

    @GetMapping
    public String getAllReservations(Model model) {
        model.addAttribute("reservations", reservationService.getAllReservations());
        return "reservations/reservation_list";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Optional<Reservation> reservation = reservationService.getReservationById(id);
        return reservation.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

//    @PostMapping
//    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
//        Reservation savedReservation = reservationService.saveReservation(reservation);
//        return ResponseEntity.ok(savedReservation);
//    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable Long id, @RequestBody Reservation reservationDetails) {
        Reservation reservation = reservationService.getReservationById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        reservation.setUser(reservationDetails.getUser());
        reservation.setRoom(reservationDetails.getRoom());
        reservation.setCheckInDate(reservationDetails.getCheckInDate());
        reservation.setCheckOutDate(reservationDetails.getCheckOutDate());
        reservation.setReservationDate(reservationDetails.getReservationDate());
        reservation.setApprovalDate(reservationDetails.getApprovalDate());
        reservation.setTotalPrice(reservationDetails.getTotalPrice());

        Reservation updatedReservation = reservationService.saveReservation(reservation);
        return ResponseEntity.ok(updatedReservation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationRest(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/book")
    public String bookRoom(@RequestParam("selectedRooms") List<Long> selectedRoomIds,
                           @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
                           @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
                           HttpSession session,
                           Model model) {

        // Перевірка, чи користувач залогінений
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login"; // Якщо не залогінений, перенаправляємо на сторінку входу
        }

        // Перевіряємо, чи дати коректні
        if (checkInDate.isAfter(checkOutDate)) {
            model.addAttribute("error", "Дата виїзду повинна бути пізніше дати заїзду");
            return "redirect:/hotels"; // Можна додати обробку помилок
        }

        // Отримуємо список вибраних кімнат
        List<Room> selectedRooms = roomService.getRoomsByIds(selectedRoomIds);

        for (Room room : selectedRooms) {
            // Створюємо нове бронювання для кожної вибраної кімнати
            Reservation reservation = new Reservation();
            reservation.setUser(user);  // Користувач з сесії
            reservation.setRoom(room);
            reservation.setCheckInDate(checkInDate.atStartOfDay());
            reservation.setCheckOutDate(checkOutDate.atStartOfDay());
            reservation.setReservationDate(LocalDateTime.now());

            // Рахуємо загальну вартість бронювання
            long daysBetween = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            double totalPrice = room.getPrice() * daysBetween;
            reservation.setTotalPrice(totalPrice);

            // Зберігаємо бронювання
            reservationService.saveReservation(reservation);
        }

        // Після успішного бронювання відображаємо повідомлення
        model.addAttribute("message", "Бронювання успішно оформлено!");
        return "hotel_details";
    }
}
