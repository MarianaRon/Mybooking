package com.example.mybooking.controller;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Review;
import com.example.mybooking.model.User;
import com.example.mybooking.service.HotelService;
import com.example.mybooking.service.ReviewService;
import com.example.mybooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    @GetMapping("/review_list")
    public String reviewList(Model model) {
        List<Review> reviews = reviewService.getAllReviews();
        model.addAttribute("reviews", reviews);
        return "reviews/review_list";
    }

    @GetMapping("/edit_review/{id}")
    public String editReviewForm(@PathVariable Long id, Model model) {
        Optional<Review> review = reviewService.getReviewById(id);
        if (review.isPresent()) {
            model.addAttribute("review", review.get());
            model.addAttribute("hotels", hotelService.getAllHotels());
            model.addAttribute("users", userService.getAllUsers());
            return "reviews/edit_review";
        } else {
            return "redirect:/reviews/review_list";
        }
    }

    @PostMapping("/edit_review/{id}")
    public String updateReview(@PathVariable Long id, @ModelAttribute Review reviewDetails) {
        Optional<Review> optionalReview = reviewService.getReviewById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setContent(reviewDetails.getContent());
            review.setRating(reviewDetails.getRating());
            review.setReviewDate(reviewDetails.getReviewDate());
            review.setHotel(reviewDetails.getHotel());
            review.setUser(reviewDetails.getUser());
            reviewService.saveReview(review);
        }
        return "redirect:/reviews/review_list";
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return "redirect:/reviews/review_list";
    }

    @GetMapping("/new_review")
    public String newReviewForm(Model model) {
        model.addAttribute("hotels", hotelService.getAllHotels());
        model.addAttribute("users", userService.getAllUsers());
        return "reviews/new_review";
    }

    @PostMapping("/new_review")
    public String createReview(@ModelAttribute Review review) {
        reviewService.saveReview(review);
        return "redirect:/reviews/review_list";
    }
}
