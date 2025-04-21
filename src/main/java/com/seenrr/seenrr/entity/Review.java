package com.seenrr.seenrr.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tmdb_id;

    @Column(nullable = false)
    private String user_id;

    
}
