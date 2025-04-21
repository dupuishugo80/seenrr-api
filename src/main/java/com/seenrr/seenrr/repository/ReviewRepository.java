package com.seenrr.seenrr.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seenrr.seenrr.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

}
