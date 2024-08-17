package com.example.mybooking.repository;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IHotelRepository extends JpaRepository<Hotel, Long> {
    // Метод для поиска отелей по названию или описанию
    //Метод реализует поиск отелей по частичному совпадению названия или описания.
    List<Hotel> findByNameContainingOrDescriptionContaining(String name, String description);

    // Метод для поиска отелей по владельцу
    List<Hotel> findByOwner(User owner);
}