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

    public Review createReview(Long mediaId, Long userId, String reviewText, double rating, String token) {
        validateToken(token);

        if (mediaId == null || userId == null || reviewText == null || rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Un des champs est invalide.");
        }

        Media media = mediaService.getMediaById(mediaId, "movie");
        if (media == null) {
            throw new IllegalArgumentException("Le média n'existe pas.");
        }

        User user = userService.getUserById(userId, token);
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur n'existe pas.");
        }

        Review existingReview = reviewRepository.findByMediaAndUser(media, user);
        if (existingReview != null) {
            throw new IllegalArgumentException("Une review existe déja pour ce média.");
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

    public Review getReviewById(Integer id, String token) {
        validateToken(token);

        Review review = reviewRepository.findById(id);
        if (review == null) {
            throw new IllegalArgumentException("La review est introuvable.");
        }
        return review;
    }

    public void deleteReview(Integer id, String token) {
        validateToken(token);
        checkReviewOwner(id, token);
        Review review = reviewRepository.findById(id);
        if (review == null) {
            throw new IllegalArgumentException("La review est introuvable.");
        }
        reviewRepository.delete(review);
    }

    public Review updateReview(Integer id, String reviewText, double rating, String token) {
        validateToken(token);
        checkReviewOwner(id, token);

        Review review = reviewRepository.findById(id);
        if (review == null) {
            throw new IllegalArgumentException("La review est introuvable.");
        }

        if (reviewText != null) {
            review.setReviewText(reviewText);
        }
        if (rating > 0 && rating <= 5) {
            review.setRating(rating);
        }
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    private void validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token requis.");
        }
        if (!JwtService.isValidToken(token)) {
            throw new IllegalArgumentException("Token invalide.");
        }
        if (JwtService.isTokenExpired(token)) {
            throw new IllegalArgumentException("Token expiré.");
        }
    }

    private void checkReviewOwner(Integer reviewId, String token) {
        Review review = reviewRepository.findById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("La review est introuvable.");
        }
        String tokenUsername = JwtService.extractUsername(token);
        User user = userService.getUserByUsername(tokenUsername);
        if(user != review.getUser()) {
            throw new IllegalArgumentException("Vous n'êtes pas le propriétaire de cette review.");
        }
    }
}
