package com.example.mybooking.repository;

import com.example.mybooking.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICityRepository extends JpaRepository<City, Long> {
    // Метод для поиска города по названию
    City findByName(String name);
}
