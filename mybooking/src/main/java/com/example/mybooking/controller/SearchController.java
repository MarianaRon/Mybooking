package com.example.mybooking.controller;

import com.example.mybooking.model.City;
import com.example.mybooking.model.SearchHotel;
import com.example.mybooking.service.CityService;
import com.example.mybooking.service.SearchHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class SearchController {

    @Autowired
    private CityService cityService;

    @Autowired
    private SearchHotelService searchHotelService;

    @PostMapping("/search")
    public String searchHotel(Long city, LocalDate date, int guests, Model model) {
        // Отримуємо Optional<City> і обробляємо його
        Optional<City> selectedCityOpt = cityService.getCityById(city);

        if (selectedCityOpt.isPresent()) {
            City selectedCity = selectedCityOpt.get();

            SearchHotel search = new SearchHotel(selectedCity, date, guests);
            searchHotelService.saveSearch(search);

            // Отримуємо результати пошуку (наприклад, список готелів)
            model.addAttribute("hotels", selectedCity.getHotels());
            return "search_results";
        } else {
            // Обробляємо випадок, коли місто не знайдено
            model.addAttribute("error", "Місто не знайдено");
            return "error_page"; // Повертайте відповідну сторінку для помилки
        }
    }
}
