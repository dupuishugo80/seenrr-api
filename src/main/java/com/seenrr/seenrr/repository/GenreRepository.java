package com.seenrr.seenrr.repository;

import com.seenrr.seenrr.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Integer> {
    
}
