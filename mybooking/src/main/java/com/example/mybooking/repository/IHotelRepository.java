package com.example.mybooking.repository;

import com.example.mybooking.model.City;
import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IHotelRepository extends JpaRepository<Hotel, Long> {

    //Метод реализует поиск отелей по частичному совпадению названия или описания.
    List<Hotel> findByNameContainingOrDescriptionContaining(String name, String description);

    // Метод для поиска отелей по владельцу
    List<Hotel> findByOwner(Partner owner);



    //для сортуваня готелів по місту
        List<Hotel> findByCity(City city);

}