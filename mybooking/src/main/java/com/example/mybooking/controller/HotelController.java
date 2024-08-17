package com.example.mybooking.controller;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.User;
import com.example.mybooking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AmenityService amenityService;

    @Autowired
    private ImageService imageService;

    // Получение всех отелей, зарегистрированных партнером
    @GetMapping("/by-partner")
    public String getHotelsByPartner(@SessionAttribute("loggedInUser") User loggedInUser, Model model) {
        model.addAttribute("hotels", hotelService.getHotelsByOwner(loggedInUser));
        return "hotels_by_partner";
    }

    // Получение всех отелей
    @GetMapping
    public String getAllHotels(Model model) {
        model.addAttribute("hotels", hotelService.getAllHotels());
        return "hotels";
    }

    // Показ формы регистрации отеля
    @GetMapping("/hotel_registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("hotel", new Hotel());
        model.addAttribute("users", userService.getAllUsers());
        return "hotel_form";
    }

    // Создание нового отеля
    @PostMapping("/add")
    public String createHotel(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam String owner,
            @RequestParam String housingType) {

        User ownerUser = userService.getUserById(Long.parseLong(owner))
                .orElseThrow(() -> new IllegalArgumentException("Invalid owner ID"));

        Hotel hotel = new Hotel(name, description, address, latitude, longitude, ownerUser, housingType);
        hotelService.saveHotel(hotel);

        return "redirect:/hotels";
    }

    // Получение отеля по ID
    @GetMapping("/{id}")
    public String getHotelById(@PathVariable Long id, Model model) {
        return hotelService.getHotelById(id)
                .map(hotel -> {
                    model.addAttribute("hotel", hotel);
                    return "hotel_details";
                })
                .orElse("redirect:/hotels");
    }

    // Обновление информации об отеле
    @PutMapping("/{id}")
    public String updateHotel(@PathVariable Long id, @ModelAttribute Hotel hotelDetails) {
        hotelService.updateHotel(id, hotelDetails);
        return "redirect:/hotels/" + id;
    }

    // Удаление отеля по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    // Показ формы поиска отелей
    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("searchTerm", "");
        return "search_form";
    }

    // Поиск отелей по названию или описанию
    @PostMapping("/search")
    public String searchHotels(@RequestParam String searchTerm, Model model) {
        List<Hotel> results = hotelService.searchHotelsByNameOrDescription(searchTerm);
        model.addAttribute("results", results);
        return "search_results";
    }
}