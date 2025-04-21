package com.seenrr.seenrr.dto;

public class ReviewDto {
    private String reviewText;
    private double rating;
    private Integer mediaId;
    private Integer userId;

    public ReviewDto(String reviewText, double rating, Integer mediaId, Integer userId) {
        this.reviewText = reviewText;
        this.rating = rating;
        this.mediaId = mediaId;
        this.userId = userId;
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

    public Integer getMediaId() {
        return mediaId;
    }

    public void setMediaId(Integer mediaId) {
        this.mediaId = mediaId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
