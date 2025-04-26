package com.seenrr.seenrr.dto;

import java.time.LocalDateTime;

public class ReviewDto {
    private String reviewText;
    private double rating;
    private Long mediaTmdbId;
    private Long userId;
    private String media;
    private String user;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReviewDto(String reviewText, double rating, long mediaTmdbId, long userId, String media, String user, String type,LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reviewText = reviewText;
        this.rating = rating;
        this.mediaTmdbId = mediaTmdbId;
        this.userId = userId;
        this.media = media;
        this.user = user;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Long getMediaTmdbId() {
        return mediaTmdbId;
    }

    public void setMedia(Long mediaTmdbId) {
        this.mediaTmdbId = mediaTmdbId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserID(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
