package com.example.mybooking.repository;

import com.example.mybooking.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IImageRepository extends JpaRepository<Image, Long> {
}
