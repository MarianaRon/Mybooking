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

    public List<Amenity> getAmenitiesByIds(List<Long> amenityIds) {
        return amenityRepository.findAllById(amenityIds); // Предполагается, что используете JpaRepository
    }
    public List<Amenity> getAllAmenitiesByIds(List<Long> amenityIds) {
        return amenityRepository.findAllById(amenityIds);
    }
    public List<Amenity> getAllAmenities() {
        List<Amenity> amenities = amenityRepository.findAll();
        if (amenities.isEmpty()) {
            logger.warn("Список удобств пуст");
        } else {
            logger.info("Количество удобств: {}", amenities.size());
        }
        return amenities;
    }
//    public List<Amenity> getAllAmenities() {
//        return amenityRepository.findAll();
//    }

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
