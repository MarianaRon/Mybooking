package com.example.mybooking.service;

import com.example.mybooking.model.Amenity;
import com.example.mybooking.repository.IAmenityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AmenityService {

    @Autowired
    private IAmenityRepository amenityRepository;
    private static final Logger logger = LoggerFactory.getLogger(AmenityService.class);

    // Возвращаем все удобства по их ID
    public List<Amenity> getAllAmenitiesByIds(List<Long> amenityIds) {
        return amenityRepository.findAllById(amenityIds);
    }
    public List<Amenity> getAllAmenities() {
        return amenityRepository.findAll();  // Запрос всех удобств из базы данных
    }

    // Метод для получения удобств по типу ('general' или 'room')
    public List<Amenity> getAmenitiesByType(String type) {
         return amenityRepository.findAll().stream()
            .filter(amenity -> amenity.getType().equals(type))
            .toList();
}
    public Optional<Amenity> getAmenityById(Long id) {
        return amenityRepository.findById(id);
    }

    public Amenity saveAmenity(Amenity amenity) {
        return amenityRepository.save(amenity);
    }

    public void deleteAmenity(Long id) {
        amenityRepository.deleteById(id);
    }
}
