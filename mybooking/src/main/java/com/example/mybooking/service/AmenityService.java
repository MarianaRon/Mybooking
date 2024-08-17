package com.example.mybooking.service;

import com.example.mybooking.model.Amenity;
import com.example.mybooking.repository.IAmenityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AmenityService {

    @Autowired
    private IAmenityRepository amenityRepository;

    public List<Amenity> getAllAmenities() {
        return amenityRepository.findAll();
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
