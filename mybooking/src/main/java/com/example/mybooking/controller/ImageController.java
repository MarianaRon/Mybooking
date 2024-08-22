package com.example.mybooking.controller;

import com.example.mybooking.model.Image;
import com.example.mybooking.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@Controller

@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @GetMapping
    public List<Image> getAllImages() {
        return imageService.getAllImages();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Image> getImageById(@PathVariable Long id) {
        Optional<Image> image = imageService.getImageById(id);
        if (image.isPresent()) {
            return ResponseEntity.ok(image.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Image createImage(@RequestBody Image image) {
        return imageService.saveImage(image);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Image> updateImage(@PathVariable Long id, @RequestBody Image imageDetails) {
        Optional<Image> optionalImage = imageService.getImageById(id);
        if (optionalImage.isPresent()) {
            Image image = optionalImage.get();
            image.setUrl(imageDetails.getUrl());
            image.setHotel(imageDetails.getHotel());
            image.setRoom(imageDetails.getRoom());
            return ResponseEntity.ok(imageService.saveImage(image));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }


    // Метод для відображення сторінки списку зображень
    @GetMapping("/image_list")
    public String imageList(Model model) {
        List<Image> images = imageService.getAllImages();
        model.addAttribute("images", images);
        return "images/image_list"; // Назва HTML-файлу, що відображатиме список зображень
    }
}




