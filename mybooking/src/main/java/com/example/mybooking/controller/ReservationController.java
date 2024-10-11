package com.example.mybooking.controller;

import com.example.mybooking.model.Hotel;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String bookRoom(
            @RequestParam("userId") Long userId,
            @RequestParam("roomId") Long roomId,
            @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInDate,
            @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutDate,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        // Перевірка дат
        if (checkInDate.isEqual(checkOutDate) || checkInDate.isAfter(checkOutDate)) {
            model.addAttribute("error", "Дата виїзду повинна бути пізніше дати заїзду");
            return "redirect:/rooms/roomDetails/" + roomId;  // Повертаємо на сторінку деталей кімнати
        }

        Optional<Room> roomOptional = roomService.getRoomById(roomId);
        if (!roomOptional.isPresent()) {
            model.addAttribute("error", "Кімната не знайдена");
            return "redirect:/rooms/roomDetails/" + roomId; // Повертаємо на сторінку деталей кімнати
        }

        Room room = roomOptional.get();
        Hotel hotel = room.getHotel();  // Перевірка, що у кімнати є пов'язаний готель
        model.addAttribute("hotel", hotel);  // Передаємо готель в модель
        // Обчислюємо кількість днів перебування
        long daysBetween = ChronoUnit.DAYS.between(checkInDate.toLocalDate(), checkOutDate.toLocalDate());
        if (daysBetween < 1) {
            model.addAttribute("error", "Мінімальний термін бронювання - 1 день");
            return "redirect:/rooms/roomDetails/" + roomId; // Повертаємо на сторінку деталей кімнати
        }

        // Розраховуємо загальну ціну
        double totalPrice = room.getPrice() * daysBetween;

        // Створюємо нове бронювання
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);
        reservation.setReservationDate(LocalDateTime.now()); // Дата бронювання
        reservation.setTotalPrice(totalPrice);

        // Зберігаємо бронювання
        reservationService.saveReservation(reservation);

        // Додаємо повідомлення про успішне бронювання
        model.addAttribute("message", "Бронювання успішно оформлено!");
        redirectAttributes.addFlashAttribute("message", "Бронювання успішно оформлено! Деталі можна переглянути в кабінеті користувача. Оплату треба здійснити протягом доби.");

        // Повертаємо на ту саму сторінку з повідомленням
        return "redirect:/rooms/roomDetails/" + roomId;
    }


}
