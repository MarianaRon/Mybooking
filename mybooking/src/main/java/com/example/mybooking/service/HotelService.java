package com.example.mybooking.service;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Partner;
import com.example.mybooking.repository.IHotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {

    @Autowired
    private IHotelRepository hotelRepository;

    // Получение всех отелей
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    // Получение отеля по ID
    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    // Сохранение нового отеля
    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    // Удаление отеля по ID
    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    // Поиск отелей по названию или описанию
    public List<Hotel> searchHotelsByNameOrDescription(String searchTerm) {
        return hotelRepository.findByNameContainingOrDescriptionContaining(searchTerm, searchTerm);
    }
    // Получение отелей по владельцу (партнеру)
    public List<Hotel> getHotelsByOwner(Partner owner) {

        return hotelRepository.findByOwner(owner);
    }
    public void updateHotel(Long id, Hotel hotelDetails) {
        hotelRepository.findById(id).ifPresent(hotel -> {
            hotel.setName(hotelDetails.getName());
            hotel.setDescription(hotelDetails.getDescription());
            hotel.setAddress(hotelDetails.getAddress());
            hotel.setLatitude(hotelDetails.getLatitude());
            hotel.setLongitude(hotelDetails.getLongitude());
            hotel.setOwner(hotelDetails.getOwner());
            hotel.setHousingType(hotelDetails.getHousingType());
            hotelRepository.save(hotel);
        });
    }
}