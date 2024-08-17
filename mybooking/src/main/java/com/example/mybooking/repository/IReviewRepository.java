package com.example.mybooking.repository;

import com.example.mybooking.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IReviewRepository extends JpaRepository<Review, Long> {
}