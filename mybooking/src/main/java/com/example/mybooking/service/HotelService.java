package com.example.mybooking.service;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Partner;
import com.example.mybooking.repository.IHotelRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {
    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);

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
//    public Hotel saveHotel(Hotel hotel) {
//        return hotelRepository.save(hotel);
//    }

    // Сохранение отеля с партнером (владельцем)
    @Transactional // Обеспечивает корректность транзакции
    public Hotel saveHotelWithPartner(Hotel hotel, Partner partner) {
        logger.debug("Entering saveHotelWithPartner with hotel: {} and partner: {}", hotel, partner);
        hotel.setOwner(partner);

        // Логируем состояние отеля перед сохранением
        logger.info("Attempting to save hotel: {}", hotel);

        try {
            Hotel savedHotel = hotelRepository.save(hotel);
            logger.info("Hotel with partner saved successfully: {}", savedHotel);
            return savedHotel;
        } catch (Exception e) {
            logger.error("Error saving hotel with partner: {}", e.getMessage(), e);
            throw e; // Перебрасываем исключение после логирования
        }
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
    // Обновление данных отеля
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