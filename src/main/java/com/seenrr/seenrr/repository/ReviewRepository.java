package com.seenrr.seenrr.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seenrr.seenrr.entity.User;
import com.seenrr.seenrr.entity.Media;
import com.seenrr.seenrr.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Review findById(Integer id);

    Review findByMediaAndUser(Media media, User user);
}
