package com.seenrr.seenrr.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seenrr.seenrr.dto.ReviewDto;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @Column(name = "two_fa_secret")
    private String twoFaSecret;

    @JsonIgnore
    @Column(name = "is_two_fa_enabled")
    private boolean isTwoFaEnabled;

    @JsonIgnore
    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "user_following",
        joinColumns = @JoinColumn(name = "follower_id"),
        inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private Set<User> following = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "following")
    private Set<User> followers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getTwoFaSecret() {
        return twoFaSecret;
    }
    
    public void setTwoFaSecret(String twoFaSecret) {
        this.twoFaSecret = twoFaSecret;
    }
    
    public boolean isTwoFaEnabled() {
        return isTwoFaEnabled;
    }
    
    public void setTwoFaEnabled(boolean twoFaEnabled) {
        isTwoFaEnabled = twoFaEnabled;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public Set<User> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<User> followers) {
        this.followers = followers;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public Set<ReviewDto> getReviews() {
        Set<ReviewDto> reviewDtos = new HashSet<>();
        for(Review review : reviews) {
            reviewDtos.add(new ReviewDto(
                review.getReviewText(), 
                review.getRating(),
                review.getMedia().getTmdbId(),
                review.getUser().getId(),
                review.getMedia().getTitle(), 
                review.getUser().getUsername(), 
                review.getMedia().getMediaType(),
                review.getCreatedAt(), 
                review.getUpdatedAt()));
        }
        return reviewDtos;
    }

    public void follow(User userToFollow) {
        if (userToFollow != null && userToFollow != this && !this.following.contains(userToFollow)) {
            this.following.add(userToFollow);
            userToFollow.getFollowers().add(this);
        }
    }

    public void unfollow(User userToUnfollow) {
        if (userToUnfollow != null && userToUnfollow != this && this.following.contains(userToUnfollow)) {
            this.following.remove(userToUnfollow);
            userToUnfollow.getFollowers().remove(this);
        }
    }

    public Set<User> getFollowing() {
        return following;
    }

    public void setFollowing(Set<User> following) {
        this.following = following;
    }

}
