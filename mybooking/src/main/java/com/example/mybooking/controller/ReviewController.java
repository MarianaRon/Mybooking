package com.example.mybooking.controller;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Review;
import com.example.mybooking.model.User;
import com.example.mybooking.service.HotelService;
import com.example.mybooking.service.ReviewService;
import com.example.mybooking.service.UserService;
import jakarta.servlet.http.HttpSession;
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
        Review review = reviewService.getReviewById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        model.addAttribute("review", review);
        model.addAttribute("hotels", hotelService.getAllHotels());
        model.addAttribute("users", userService.getAllUsers());
        return "reviews/edit_review";
    }

    @PostMapping("/edit_review/{id}")
    public String updateReview1(@PathVariable Long id, @ModelAttribute Review reviewDetails) {
        Review review = reviewService.getReviewById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setContent(reviewDetails.getContent());
        review.setRating(reviewDetails.getRating());
        review.setReviewDate(reviewDetails.getReviewDate());
        review.setHotel(reviewDetails.getHotel());
        review.setUser(reviewDetails.getUser());

        reviewService.saveReview(review);
        return "redirect:/reviews/review_list";
    }

    @PostMapping("/delete/{id}")
    public String deleteReview1(@PathVariable Long id) {
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
    public String createReview1(@ModelAttribute Review review) {
        reviewService.saveReview(review);
        return "redirect:/reviews/review_list";
    }

    @GetMapping
    public String getAllReviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "reviews/review_list";
    }

    // REST API методи

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewService.getReviewById(id);
        return review.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        Review savedReview = reviewService.saveReview(review);
        return ResponseEntity.ok(savedReview);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review reviewDetails) {
        Review review = reviewService.getReviewById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setContent(reviewDetails.getContent());
        review.setRating(reviewDetails.getRating());
        review.setReviewDate(reviewDetails.getReviewDate());
        review.setHotel(reviewDetails.getHotel());
        review.setUser(reviewDetails.getUser());

        Review updatedReview = reviewService.saveReview(review);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }



////    для каруселі на сторінку Про нас
//@GetMapping("/review_slider")
//public String getReviewSlider(Model model) {
//    // Отримуємо всі відгуки
//    List<Review> allReviews = reviewService.getAllReviews();
//
//    // Перша трійка відгуків для початкового відображення
//    List<Review> firstThreeReviews = allReviews.subList(0, Math.min(3, allReviews.size()));
//
//    model.addAttribute("reviews", firstThreeReviews);
//    model.addAttribute("totalReviews", allReviews.size());
//    return "about_us";
//}

    @PostMapping("/add")
    public String addReview(@RequestParam("hotelId") Long hotelId, @RequestParam("content") String content,
                            @RequestParam("rating") Integer rating, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            reviewService.addReview(hotelId, content, rating, currentUser);
        }
        return "redirect:/hotels/" + hotelId;
    }
    @PostMapping("/admin/hotels/{hotelId}/addReview")
    public String addReviewAdmin(@PathVariable Long hotelId,
                                 @RequestParam String content,
                                 @RequestParam Integer rating,
                                 HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        reviewService.addReview(hotelId, content, rating, user);
        return "redirect:/admin/hotels/" + hotelId;
    }

    @PostMapping("/admin/hotels/{hotelId}/deleteReview/{reviewId}")
    public String deleteReviewAdmin(@PathVariable Long hotelId, @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return "redirect:/admin/hotels/" + hotelId;
    }
    @PostMapping("/hotels/{hotelId}/addReview")
    public String addReviewHotelPage(@PathVariable Long hotelId,
                                     @RequestParam String content,
                                     @RequestParam Integer rating,
                                     HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        reviewService.addReview(hotelId, content, rating, user);
        return "redirect:/hotels/" + hotelId;
    }
    @PostMapping("/delete/user_account/{id}")
    public String deleteReview(@PathVariable("id") Long reviewId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            reviewService.deleteReview(reviewId, currentUser);
        }
        return "redirect:/user_account";
    }


//    // Видалення відгуку зі сторінки готелю
//    @PostMapping("/hotel/{hotelId}/deleteReview/{id}")
//    public String deleteReviewFromHotel(@PathVariable Long hotelId, @PathVariable Long id) {
//        reviewService.deleteReview(id);
//        return "redirect:/hotels/" + hotelId;
//    }


}
