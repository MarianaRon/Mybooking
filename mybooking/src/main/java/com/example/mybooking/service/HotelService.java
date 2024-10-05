package com.example.mybooking.service;

import com.example.mybooking.model.Amenity;
import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Partner;
import com.example.mybooking.repository.IAmenityRepository;
import com.example.mybooking.repository.IHotelRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HotelService {

    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);

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

    /**
     * Сохранение отеля вместе с привязкой к партнеру.
     *
     * @param hotel объект отеля
     * @param partner объект партнера
     * @return сохранённый объект отеля
     */
    @Transactional
    public Hotel saveHotelWithPartner(Hotel hotel, Partner partner, List<Long> amenityIds) {
        hotel.setOwner(partner); // Привязываем партнера
        hotel.setCity(hotel.getCity()); // Привязываем город

        // Проверяем, что список ID удобств не пуст
        if (amenityIds != null && !amenityIds.isEmpty()) {
            Set<Amenity> selectedAmenities = amenityRepository.findAllById(amenityIds).stream().collect(Collectors.toSet());
            hotel.setAmenities(selectedAmenities);  // Привязываем удобства к отелю
        }
        // Сохраняем обложку
        if (hotel.getCoverImage() != null) {
            logger.info("Saving cover image for hotel: {}", hotel.getName());
        }

        // Сохраняем отель вместе с удобствами
        hotelRepository.save(hotel);

        try {
            Hotel savedHotel = hotelRepository.save(hotel);
            logger.info("Hotel saved successfully: {}", savedHotel);
            return savedHotel;
        } catch (Exception e) {
            logger.error("Error while saving hotel: {}", e.getMessage(), e);
            throw e;  // Перебрасываем исключение для обработки на уровне контроллера
        }
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