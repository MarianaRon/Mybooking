package com.example.mybooking.service;

import com.example.mybooking.model.Amenity;
import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Image;
import com.example.mybooking.model.Partner;
import com.example.mybooking.repository.IAmenityRepository;
import com.example.mybooking.repository.IHotelRepository;
import com.example.mybooking.repository.IImageRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//import static jdk.nio.zipfs.ZipFileAttributeView.AttrID.owner;

@Service
public class HotelService {

    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);
    @Autowired
    private IImageRepository imageRepository;
    @Autowired
    private IHotelRepository hotelRepository;

    @Autowired
    private IAmenityRepository amenityRepository;  // Инжектируем AmenityRepository


    //    /**
//     * Получение всех отелей из базы данных.
//     *
//     * @return список всех отелей.
//     */
    public List<Hotel> getAllHotels() {
        logger.debug("Fetching all hotels");
        return hotelRepository.findAll();
    }


    //для роботи з картою на головній сторінці
    public List<Hotel> get_AllHotels() {
        logger.debug("Fetching all hotels");
        List<Hotel> hotels = hotelRepository.findAll();
        logger.debug("Hotels found: " + hotels.size()); // Додаємо лог для перевірки
        return hotels;
    }

    /**
     * Получение отеля по ID.
     *
     * @param id ID отеля
     * @return Optional с отелем, если найден
     */
    public Optional<Hotel> getHotelById(Long id) {

        logger.debug("Fetching hotel with ID: {}", id);
        return hotelRepository.findById(id);
    }


    @Transactional
    public Hotel saveHotelWithPartner(Hotel hotel) {
        return hotelRepository.save(hotel);
    }
    /**
     * Удаление отеля по ID.
     *
     * @param id ID отеля для удаления
     */
    public boolean deleteHotelById(Long id) {
        logger.debug("Attempting to delete hotel with ID: {}", id);

        if (hotelRepository.existsById(id)) {
            hotelRepository.deleteById(id);
            logger.info("Hotel with ID: {} successfully deleted", id);
            return true;
        } else {
            logger.warn("Hotel with ID: {} not found", id);
            return false;
        }
    }

    /**
     * Поиск отелей по названию или описанию.
     *
     * @param searchTerm поисковый запрос
     * @return список отелей, соответствующих запросу
     */
    public List<Hotel> searchHotelsByNameOrDescription(String searchTerm) {
        logger.debug("Searching for hotels by name or description with term: {}", searchTerm);
        return hotelRepository.findByNameContainingOrDescriptionContaining(searchTerm, searchTerm);
    }

    /**
     * Получение всех отелей, принадлежащих партнеру.
     *
     * @param owner объект партнера (владельца)
     * @return список отелей, принадлежащих партнеру
     */
    public List<Hotel> getHotelsByOwner(Partner owner) {
        logger.debug("Fetching hotels for partner: {}", owner.getId());
        return hotelRepository.findByOwner(owner);
    }


    /**
     * Обновление существующего отеля.
     *
     * @param id ID отеля для обновления
     * @param hotelDetails объект с новыми данными для отеля
     */
    public void updateHotel(Long id, Hotel hotelDetails) {
        hotelRepository.findById(id).ifPresent(hotel -> {
            logger.debug("Updating hotel with ID: {}", id);
            hotel.setName(hotelDetails.getName());
            hotel.setDescription(hotelDetails.getDescription());
            hotel.setLatitude(hotelDetails.getLatitude());
            hotel.setLongitude(hotelDetails.getLongitude());
            hotel.setOwner(hotelDetails.getOwner());
            hotel.setHousingType(hotelDetails.getHousingType());
            hotelRepository.save(hotel);
            logger.info("Hotel with ID: {} successfully updated", id);
        });
    }

    //для виведення готелей по місту
    // Метод для збереження готелю
    public void save(Hotel hotel) {
        hotelRepository.save(hotel);
    }
}