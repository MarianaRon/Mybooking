package com.example.mybooking.controller;

import com.example.mybooking.model.City;
import com.example.mybooking.model.SearchHotel;
import com.example.mybooking.repository.ICityRepository;
import com.example.mybooking.service.CityService;
import com.example.mybooking.service.SearchHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class SearchController {

    @Autowired
    private CityService cityService;

    @Autowired
    private SearchHotelService searchHotelService;
    @Autowired
    private ICityRepository cityRepository;

    @PostMapping("/search")
    public String searchHotels(
            @RequestParam("city") Long cityId,
            @RequestParam("date") String date,
            @RequestParam("guests") int guests,
            Model model) {

        // Перевірка, чи існує місто з вказаним ID
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid city Id:" + cityId));

        // Додаємо місто в модель для відображення на сторінці
        model.addAttribute("city", city);
        model.addAttribute("photoBytes", city.getPhotoBytes());

        // Додаємо інші параметри, якщо вони будуть потрібні для майбутніх функцій
        model.addAttribute("date", date);
        model.addAttribute("guests", guests);

        // Перехід на сторінку результатів пошуку
        return "search_results";
    }

    //для каруселі міст
    @GetMapping("/sort_hotels_by_city/{cityId}")
    public String sortHotelsByCity(@PathVariable("cityId") Long cityId, Model model) {

        // Перевірка, чи існує місто з вказаним ID
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid city Id: " + cityId));

        // Додаємо місто в модель для відображення на сторінці
        model.addAttribute("city", city);

        // Якщо у міста є URL для фотографії, передаємо його у модель
        if (city.getPhotoUrl() != null && !city.getPhotoUrl().isEmpty()) {
            model.addAttribute("photoUrl", city.getPhotoUrl());
        }

        // Якщо у міста є байтові дані для фото, передаємо їх як окремий атрибут
        if (city.getPhotoBytes() != null) {
            model.addAttribute("photoBytes", city.getPhotoBytes());
        }

        // Повертаємо шаблон сторінки
        return "sort_hotels_by_city";
    }
}
