package com.seenrr.seenrr.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seenrr.seenrr.dto.ApiResponseDto;
import com.seenrr.seenrr.dto.ReviewDto;
import com.seenrr.seenrr.entity.Review;
import com.seenrr.seenrr.service.ReviewService;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto> createReview(@RequestHeader("Authorization") String authHeader, @RequestBody ReviewDto reviewDto) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            Review review;
            review = reviewService.createReview(
                    reviewDto.getMediaId(),
                    reviewDto.getUserId(),
                    reviewDto.getReviewText(),
                    reviewDto.getRating(),
                    token
            );
            return new ApiResponseDto(true, "", review);
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto> getReview(@RequestHeader("Authorization") String authHeader, @PathVariable("id") Integer id) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            Review review = reviewService.getReviewById(id, token);
            return new ApiResponseDto(true, "", review);
        });
    }

    @GetMapping("/{id}/delete")
    public ResponseEntity<ApiResponseDto> deleteReview(@RequestHeader("Authorization") String authHeader, @PathVariable("id") Integer id) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            reviewService.deleteReview(id, token);
            return new ApiResponseDto(true, "La review a été supprimée avec succès.", null);
        });
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<ApiResponseDto> updateReview(@RequestHeader("Authorization") String authHeader, @PathVariable("id") Integer id, @RequestBody ReviewDto reviewDto) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            Review review = reviewService.updateReview(id, reviewDto.getReviewText(), reviewDto.getRating(), token);
            return new ApiResponseDto(true, "La review a été mise à jour avec succès.", review);
        });
    }

    private <T> ResponseEntity<ApiResponseDto> executeAndHandleExceptions(Supplier<ApiResponseDto> action) {
        try {
            return ResponseEntity.ok(action.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto(false, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto(false, "Une erreur inattendue est survenue.", null));
        }
    }
    
    private <T> ResponseEntity<ApiResponseDto> executeWithTokenAndHandleExceptions(
        String authHeader, 
        java.util.function.Function<String, ApiResponseDto> action) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto(false, "Token manquant ou invalide", null));
        }
        
        String token = authHeader.substring(7);
        return executeAndHandleExceptions(() -> action.apply(token));
    }
}
