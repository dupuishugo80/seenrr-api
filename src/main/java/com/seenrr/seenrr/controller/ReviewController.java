package com.seenrr.seenrr.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seenrr.seenrr.dto.ApiResponseDto;
import com.seenrr.seenrr.dto.ReviewDto;
import com.seenrr.seenrr.entity.Review;
import com.seenrr.seenrr.service.ReviewService;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto> createReview(@RequestBody ReviewDto reviewDto) {
        return executeAndHandleExceptions(() -> {
            Review review;
            review = reviewService.createReview(
                    reviewDto.getMediaId(),
                    reviewDto.getUserId(),
                    reviewDto.getReviewText(),
                    reviewDto.getRating()
            );
            return new ApiResponseDto(true, "", review);
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto> getReview(@PathVariable("id") Integer id) {
        return executeAndHandleExceptions(() -> {
            Review review = reviewService.getReviewById(id);
            return new ApiResponseDto(true, "", review);
        });
    }

    @GetMapping("/{id}/delete")
    public ResponseEntity<ApiResponseDto> deleteReview(@PathVariable("id") Integer id) {
        return executeAndHandleExceptions(() -> {
            reviewService.deleteReview(id);
            return new ApiResponseDto(true, "La review a été supprimée avec succès.", null);
        });
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<ApiResponseDto> updateReview(@PathVariable("id") Integer id, @RequestBody ReviewDto reviewDto) {
        return executeAndHandleExceptions(() -> {
            Review review = reviewService.updateReview(id, reviewDto.getReviewText(), reviewDto.getRating());
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
    
}
