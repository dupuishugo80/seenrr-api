package com.seenrr.seenrr.dto;

import com.seenrr.seenrr.entity.ReviewVote.VoteType;

public class ReviewVoteDto {
    private Long reviewId;
    private Long userId;
    private VoteType voteType;
    private int likesCount;
    private int dislikesCount;

    public ReviewVoteDto() {
    }

    public ReviewVoteDto(Long reviewId, Long userId, VoteType voteType, int likesCount, int dislikesCount) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.voteType = voteType;
        this.likesCount = likesCount;
        this.dislikesCount = dislikesCount;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(int dislikesCount) {
        this.dislikesCount = dislikesCount;
    }
}