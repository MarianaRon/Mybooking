package com.example.mybooking.controller;

import com.example.mybooking.model.Amenity;
import com.example.mybooking.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/amenities")
public class AmenityController {

    @Autowired
    private AmenityService amenityService;

    @GetMapping("/amenity_list")
    public String amenityList(Model model) {
        List<Amenity> amenities = amenityService.getAllAmenities();
        model.addAttribute("amenities", amenities);
        return "amenities/amenity_list";
    }

    @PostMapping("/create")
    public String createAmenity(@RequestParam String name) {
        Amenity amenity = new Amenity(name);
        amenityService.saveAmenity(amenity);
        return "redirect:/amenities/amenity_list";
    }

    @GetMapping("/edit/{id}")
    public String editAmenity(@PathVariable Long id, Model model) {
        Amenity amenity = amenityService.getAmenityById(id)
                .orElseThrow(() -> new RuntimeException("Amenity not found"));
        model.addAttribute("amenity", amenity);
        return "amenities/edit_amenity";
    }

    @PostMapping("/edit/{id}")
    public String updateAmenity(@PathVariable Long id, @RequestParam String name) {
        Amenity amenity = amenityService.getAmenityById(id)
                .orElseThrow(() -> new RuntimeException("Amenity not found"));
        amenity.setName(name);
        amenityService.saveAmenity(amenity);
        return "redirect:/amenities/amenity_list";
    }

    @PostMapping("/delete/{id}")
    public String deleteAmenity(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return "redirect:/amenities/amenity_list";
    }
}
