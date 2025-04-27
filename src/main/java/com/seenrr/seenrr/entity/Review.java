package com.seenrr.seenrr.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tmdb_id", referencedColumnName = "tmdbId", nullable = false)
    private Media media;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "review_text", nullable = false, length = 1000)
    private String reviewText;

    @Column(name = "rating", nullable = false)
    private double rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewVote> votes = new HashSet<>();

    public Review() {
    }

    public Review(Media media, User user, String reviewText, double rating) {
        this.media = media;
        this.user = user;
        this.reviewText = reviewText;
        this.rating = rating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Long getMediaId() {
        return Long.valueOf(media.getTmdbId());
    }

    public Set<ReviewVote> getVotes() {
        return votes;
    }
    
    public void setVotes(Set<ReviewVote> votes) {
        this.votes = votes;
    }
    
    public int getLikesCount() {
        return (int) votes.stream()
            .filter(vote -> vote.getVoteType() == ReviewVote.VoteType.LIKE)
            .count();
    }
    
    public int getDislikesCount() {
        return (int) votes.stream()
            .filter(vote -> vote.getVoteType() == ReviewVote.VoteType.DISLIKE)
            .count();
    }
    
    public void addVote(ReviewVote vote) {
        votes.add(vote);
        vote.setReview(this);
    }
    
    public void removeVote(ReviewVote vote) {
        votes.remove(vote);
        vote.setReview(null);
    }
}
