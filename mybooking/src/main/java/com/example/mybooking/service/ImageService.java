package com.example.mybooking.service;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Image;
import com.example.mybooking.repository.IImageRepository;
import com.example.mybooking.repository.IImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    @Autowired
    private IImageRepository imageRepository;

    // Получение всех изображений
    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }


// Сохранение изображения
    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }

    public void deleteImage(Long id) {
        imageRepository.deleteById(id);
    }

    // Получение изображений по списку ID
    public Optional<Image> getImageById(Long id) {
        return imageRepository.findById(id);

    }
    public List<Image> getImagesByIds(List<Long> imageIds) {
        return imageRepository.findAllById(imageIds);
    }

    /////////////////////////

    public void saveAll(Set<Image> images) {
        imageRepository.saveAll(images);
    }

    public List<Image> getImagesByHotelId(Long hotelId) {
        return imageRepository.findByHotelId(hotelId);
    }


}
