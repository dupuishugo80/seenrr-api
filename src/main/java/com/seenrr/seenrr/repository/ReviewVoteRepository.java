package com.seenrr.seenrr.repository;

import com.seenrr.seenrr.entity.Review;
import com.seenrr.seenrr.entity.ReviewVote;
import com.seenrr.seenrr.entity.ReviewVote.VoteType;
import com.seenrr.seenrr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
    ReviewVote findByReviewAndUser(Review review, User user);
    void deleteByReviewAndUser(Review review, User user);
    long countByReviewAndVoteType(Review review, ReviewVote.VoteType voteType);

    Boolean existsByReviewAndUserAndVoteType(Review review, User user, VoteType like);
}