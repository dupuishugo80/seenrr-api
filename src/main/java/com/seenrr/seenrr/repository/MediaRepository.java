package com.seenrr.seenrr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.seenrr.seenrr.entity.Media;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Media findByTmdbId(Long tmdbId);
    
}