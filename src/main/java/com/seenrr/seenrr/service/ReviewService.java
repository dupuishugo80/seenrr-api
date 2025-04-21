package com.seenrr.seenrr.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.seenrr.seenrr.entity.Media;
import com.seenrr.seenrr.entity.Review;
import com.seenrr.seenrr.entity.User;
import com.seenrr.seenrr.repository.ReviewRepository;

@Service
public class ReviewService {

    @Autowired
    private MediaService mediaService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewRepository reviewRepository;

    public Review createReview(Integer mediaId, Integer userId, String reviewText, double rating) {
        if (mediaId == null || userId == null || reviewText == null || rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Un des champs est invalide.");
        }

        Media media = mediaService.getMediaById(mediaId, "movie");
        if (media == null) {
            throw new IllegalArgumentException("Le média n'existe pas.");
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur n'existe pas.");
        }

        Review review = new Review();
        review.setMedia(media);
        review.setUser(user);
        review.setReviewText(reviewText);
        review.setRating(rating);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }
    
}
