package com.seenrr.seenrr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seenrr.seenrr.dto.ReviewVoteDto;
import com.seenrr.seenrr.entity.Review;
import com.seenrr.seenrr.entity.ReviewVote;
import com.seenrr.seenrr.entity.User;
import com.seenrr.seenrr.repository.ReviewRepository;
import com.seenrr.seenrr.repository.ReviewVoteRepository;

@Service
public class ReviewVoteService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewVoteRepository reviewVoteRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public ReviewVoteDto voteReview(Integer reviewId, ReviewVote.VoteType voteType, String token) {
        validateToken(token);
        
        Review review = reviewRepository.findById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("La review est introuvable.");
        }

        String tokenUsername = JwtService.extractUsername(token);
        User user = userService.getUserByUsername(tokenUsername);
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }

        ReviewVote existingVote = reviewVoteRepository.findByReviewAndUser(review, user);

        if (existingVote != null) {
            if (existingVote.getVoteType() == voteType) {
                review.removeVote(existingVote);
                reviewVoteRepository.delete(existingVote);
                existingVote = null;
            } else {
                existingVote.setVoteType(voteType);
                existingVote.setUpdatedAt(java.time.LocalDateTime.now());
                reviewVoteRepository.save(existingVote);
            }
        } else {
            ReviewVote newVote = new ReviewVote(review, user, voteType);
            review.addVote(newVote);
            reviewVoteRepository.save(newVote);
            existingVote = newVote;
        }

        ReviewVoteDto voteDto = new ReviewVoteDto(
            review.getId(), 
            user.getId(), 
            existingVote != null ? existingVote.getVoteType() : null,
            review.getLikesCount(),
            review.getDislikesCount()
        );

        return voteDto;
    }

    @Transactional(readOnly = true)
    public ReviewVoteDto getReviewVoteStatus(Integer reviewId, String token) {
        validateToken(token);
        
        Review review = reviewRepository.findById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("La review est introuvable.");
        }

        String tokenUsername = JwtService.extractUsername(token);
        User user = userService.getUserByUsername(tokenUsername);
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }

        ReviewVote existingVote = reviewVoteRepository.findByReviewAndUser(review, user);

        return new ReviewVoteDto(
            review.getId(), 
            user.getId(), 
            existingVote != null ? existingVote.getVoteType() : null,
            review.getLikesCount(),
            review.getDislikesCount()
        );
    }

    public Boolean userHasLiked(Review review, User user) {
        return reviewVoteRepository.existsByReviewAndUserAndVoteType(review, user, ReviewVote.VoteType.LIKE);
    }

    public Boolean userHasDisliked(Review review, User user) {
        return reviewVoteRepository.existsByReviewAndUserAndVoteType(review, user, ReviewVote.VoteType.DISLIKE);
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
}