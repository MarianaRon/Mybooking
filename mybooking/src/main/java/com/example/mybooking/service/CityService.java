package com.example.mybooking.service;

import com.example.mybooking.model.City;
import com.example.mybooking.repository.ICityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CityService {

    @Autowired
    private ICityRepository cityRepository;

    public List<City> getAllCities() {
        List<City> cities = cityRepository.findAll();
        System.out.println("Полученные города: " + cities);  // Логирование списка городов
        return cities;
    }

    public Optional<City> getCityById(Long id) {
        Optional<City> city = cityRepository.findById(id);
        System.out.println("Найденный город: " + city);  // Логирование найденного города
        return city;
    }

    public City saveCity(City city) {
        System.out.println("Сохраняем город: " + city);  // Логирование сохраненного города
        return cityRepository.save(city);
    }

    public void deleteCity(Long id) {
        System.out.println("Удаляем город с ID: " + id);  // Логирование удаления города
        cityRepository.deleteById(id);
    }

    public List<City> findCitiesByNameContaining(String name) {
        List<City> cities = cityRepository.findByNameContainingIgnoreCase(name);
        System.out.println("Найденные города по имени: " + name + " -> " + cities);  // Логирование найденных городов
        return cities;
    }
}
