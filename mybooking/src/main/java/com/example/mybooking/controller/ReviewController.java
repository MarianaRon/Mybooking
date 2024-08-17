package com.example.mybooking.controller;

import com.example.mybooking.model.Review;
import com.example.mybooking.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewService.getReviewById(id);
        if (review.isPresent()) {
            return ResponseEntity.ok(review.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewService.saveReview(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review reviewDetails) {
        Optional<Review> optionalReview = reviewService.getReviewById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setContent(reviewDetails.getContent());
            review.setRating(reviewDetails.getRating());
            review.setReviewDate(reviewDetails.getReviewDate());
            review.setHotel(reviewDetails.getHotel());
            review.setUser(reviewDetails.getUser());
            return ResponseEntity.ok(reviewService.saveReview(review));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}

