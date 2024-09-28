package com.example.mybooking.controller;

import com.example.mybooking.model.City;
import com.example.mybooking.model.Hotel;
import com.example.mybooking.repository.ICityRepository;
import com.example.mybooking.repository.IHotelRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cities")
public class CityController {

    @Autowired
    private CityService cityService;
    @Autowired
    private ICityRepository cityRepository;

    @Autowired
    private IHotelRepository hotelRepository;
    // Вспомогательный класс для передачи координат
    public static class CityCoordinates {
        private Double latitude;
        private Double longitude;

        public CityCoordinates(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }
    }


    // метод для сортування готелів за містом
    @GetMapping("/sort_hotels_by_city/{cityId}")
    public String sortHotelsByCity(@PathVariable Long cityId, Model model) {
        // Отримуємо місто по ID
        City city = cityRepository.findById(cityId).orElseThrow(() -> new IllegalArgumentException("Invalid city Id:" + cityId));
        // Отримуємо список готелів, які належать до цього міста
        List<Hotel> hotels = hotelRepository.findByCity(city);

        // Додаємо список готелів та місто у модель для відображення на сторінці
        model.addAttribute("city", city);
        model.addAttribute("hotels", hotels);

        return "sort_hotels_by_city"; // Повертаємо назву шаблону сторінки
    }

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
                             @RequestParam(value = "latitude", required = false) Double latitude, // Додаємо широту
                             @RequestParam(value = "longitude", required = false) Double longitude, // Додаємо довготу
                             @RequestParam("photoBytes") MultipartFile photoBytes
    ) {
        City city = cityService.getCityById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid city Id: " + id));
        city.setName(name);
        city.setRegion(region);
        city.setPhotoUrl(photoUrl);
        city.setLatitude(latitude);
        city.setLongitude(longitude);
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
            @RequestParam(value = "latitude", required = false) Double latitude, // Додаємо широту
            @RequestParam(value = "longitude", required = false) Double longitude, // Додаємо довготу
            @RequestParam("photoBytes") MultipartFile photoBytes) {

        City city = new City();
        city.setName(name);
        city.setRegion(region);
        city.setPhotoUrl(photoUrl);
        city.setLatitude(latitude);
        city.setLongitude(longitude);

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
    /////////////////////
    // Новый метод для поиска городов по имени (для Select2)
//    @GetMapping("/search")
//    @ResponseBody
//    public List<City> searchCities(@RequestParam("term") String term) {
//        return cityService.findCitiesByNameContaining(term);
//    }
    @GetMapping("/search")
    @ResponseBody
    public List<Map<String, Object>> searchCities(@RequestParam("term") String term) {
        List<City> cities = cityService.findCitiesByNameContaining(term);
        List<Map<String, Object>> result = new ArrayList<>();
        for (City city : cities) {
            Map<String, Object> cityData = new HashMap<>();
            cityData.put("id", city.getId());
            cityData.put("name", city.getName());
            result.add(cityData);
        }
        return result;
    }

    //для каруселі міст


    //для виведення сортування готелів по назві міста - перехід з головної сторінки за пошуком
    @PostMapping("/search")
    public String searchHotels(
            @RequestParam("city") Long cityId,
            @RequestParam("date") String date,
            @RequestParam("guests") int guests,
            Model model) {

        // Проверяем, существует ли город с указанным ID
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid city Id:" + cityId));

        // Получаем список отелей в этом городе
        List<Hotel> hotels = hotelRepository.findByCity(city);

        // Добавляем город и список отелей в модель
        model.addAttribute("city", city); // Эта строка добавляет объект city
        model.addAttribute("hotels", hotels); // Эта строка добавляет список отелей

        return "search_results"; // Возвращаем страницу с результатами
    }



    // Новый метод для получения координат города по ID
    @GetMapping("/{cityId}/coordinates")
    @ResponseBody
    public ResponseEntity<CityCoordinates> getCityCoordinates(@PathVariable Long cityId) {
        City city = cityService.getCityById(cityId)
                .orElseThrow(() -> new RuntimeException("Город не найден"));
        return ResponseEntity.ok(new CityCoordinates(city.getLatitude(), city.getLongitude()));
    }


    //////////////


}
