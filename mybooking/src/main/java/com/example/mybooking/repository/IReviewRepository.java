package com.example.mybooking.repository;

import com.example.mybooking.model.Review;
import com.example.mybooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUser(User user);
}