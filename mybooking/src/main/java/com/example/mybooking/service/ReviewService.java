package com.example.mybooking.service;

import com.example.mybooking.model.Hotel;
import com.example.mybooking.model.Review;
import com.example.mybooking.model.User;
import com.example.mybooking.repository.IReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class ReviewService {

    @Autowired
    private IReviewRepository reviewRepository;
    @Autowired
    private HotelService hotelService;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
    public void addReview(Long hotelId, String content, Integer rating, User user) {
        // Отримуємо об'єкт Hotel з Optional
        Hotel hotel = hotelService.getHotelById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found"));

        // Створюємо новий об'єкт Review
        Review review = new Review();
        review.setContent(content);
        review.setRating(rating);
        review.setHotel(hotel);  // Присвоюємо об'єкт готелю
        review.setUser(user);    // Присвоюємо користувача
        review.setReviewDate(LocalDateTime.now()); // Додаємо поточну дату

        // Зберігаємо відгук у базі даних
        reviewRepository.save(review);
    }


    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review not found"));
        if (review.getUser().equals(user)) {
            reviewRepository.delete(review);
        }
    }
    // Метод для отримання відгуків за користувачем
    public List<Review> findReviewsByUser(User user) {
        return reviewRepository.findByUser(user);
    }
}