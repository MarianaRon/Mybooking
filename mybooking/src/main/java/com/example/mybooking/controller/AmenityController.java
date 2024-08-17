package com.example.mybooking.controller;

import com.example.mybooking.model.Amenity;
import com.example.mybooking.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/amenities")
public class AmenityController {

    @Autowired
    private AmenityService amenityService;

    @GetMapping
    public List<Amenity> getAllAmenities() {
        return amenityService.getAllAmenities();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Amenity> getAmenityById(@PathVariable Long id) {
        Optional<Amenity> amenity = amenityService.getAmenityById(id);
        if (amenity.isPresent()) {
            return ResponseEntity.ok(amenity.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Amenity createAmenity(@RequestBody Amenity amenity) {
        return amenityService.saveAmenity(amenity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Amenity> updateAmenity(@PathVariable Long id, @RequestBody Amenity amenityDetails) {
        Optional<Amenity> optionalAmenity = amenityService.getAmenityById(id);
        if (optionalAmenity.isPresent()) {
            Amenity amenity = optionalAmenity.get();
            amenity.setName(amenityDetails.getName());
            return ResponseEntity.ok(amenityService.saveAmenity(amenity));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmenity(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}
