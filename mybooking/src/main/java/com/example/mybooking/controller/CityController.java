package com.example.mybooking.controller;

import com.example.mybooking.model.City;
import com.example.mybooking.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/cities")
public class CityController {

    @Autowired
    private CityService cityService;

    @GetMapping("/city_list")
    public String listCities(Model model) {
        model.addAttribute("cities", cityService.getAllCities());
        return "cities/city_list";
    }
    @GetMapping("/photo/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        City city = cityService.getCityById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid city Id: " + id));
        byte[] photoBytes = city.getPhotoBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(photoBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/edit/{id}")
    public String editCityForm(@PathVariable Long id, Model model) {
        City city = cityService.getCityById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid city Id: " + id));
        model.addAttribute("city", city);
        return "cities/edit_list";
    }

    @PostMapping("/edit/{id}")
    public String updateCity(@PathVariable Long id,
                             @RequestParam("name") String name,
                             @RequestParam("region") String region,
                             @RequestParam("photoUrl") String photoUrl,
                             @RequestParam("photoBytes") MultipartFile photoBytes) {
        City city = cityService.getCityById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid city Id: " + id));

        city.setName(name);
        city.setRegion(region);
        city.setPhotoUrl(photoUrl);

        // Перевірка, чи файл не порожній
        if (!photoBytes.isEmpty()) {
            try {
                // Конвертуємо файл в масив байтів
                city.setPhotoBytes(photoBytes.getBytes());
            } catch (IOException e) {
                e.printStackTrace();  // Логування помилки
            }
        }

        // Зберігаємо оновлене місто
        cityService.saveCity(city);
        return "redirect:/cities/city_list";
    }

    @PostMapping("/delete/{id}")
    public String deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return "redirect:/cities/city_list";
    }

    @PostMapping("/add")
    public String addCity(
            @RequestParam("name") String name,
            @RequestParam("region") String region,
            @RequestParam("photoUrl") String photoUrl,
            @RequestParam("photoBytes") MultipartFile photoBytes) {

        City city = new City();
        city.setName(name);
        city.setRegion(region);
        city.setPhotoUrl(photoUrl);

        if (!photoBytes.isEmpty()) {
            try {
                city.setPhotoBytes(photoBytes.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cityService.saveCity(city);
        return "redirect:/cities/city_list";
    }

//    @GetMapping("/carousel")
//    public String getCarouselCities(Model model) {
//        List<City> cities = cityService.getAllCities(); // Отримуємо всі міста
//        model.addAttribute("cities", cities);
//        return "/home";
//    }
//    @GetMapping("/")
//    public String showHomePage(Model model) {
//        return "redirect:/home";
//    }


}
