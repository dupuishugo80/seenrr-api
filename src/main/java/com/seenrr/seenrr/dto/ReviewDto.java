package com.seenrr.seenrr.dto;

public class ReviewDto {
    private String reviewText;
    private double rating;
    private Long mediaId;
    private Long userId;

    public ReviewDto(String reviewText, double rating, Long mediaId, Long userId) {
        this.reviewText = reviewText;
        this.rating = rating;
        this.mediaId = mediaId;
        this.userId = userId;
    }

    public ReviewDto() {
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
