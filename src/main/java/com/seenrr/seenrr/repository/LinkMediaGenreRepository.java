package com.seenrr.seenrr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seenrr.seenrr.entity.LinkMediaGenre;

@Repository
public interface LinkMediaGenreRepository extends JpaRepository<LinkMediaGenre, Long> {
}