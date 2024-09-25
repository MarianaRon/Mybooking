package com.example.mybooking.repository;

import com.example.mybooking.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICityRepository extends JpaRepository<City, Long> {
    // Метод для поиска города по названию
    City searchByName(String name);
    List<City> findByNameContainingIgnoreCase(String name);
}
