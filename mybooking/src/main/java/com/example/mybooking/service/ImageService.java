package com.example.mybooking.service;

import com.example.mybooking.model.Image;
import com.example.mybooking.repository.IImageRepository;
import com.example.mybooking.repository.IImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImageService {

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

}
