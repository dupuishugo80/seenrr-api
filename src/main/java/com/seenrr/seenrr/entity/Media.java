package com.seenrr.seenrr.entity;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seenrr.seenrr.dto.GenreDto;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String mediaType;
    @NotNull
    private String title;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDate releaseDate;
    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LinkMediaGenre> mediaGenres;
    private String country;
    private String coverUrl;
    @NotNull
    private Integer tmdbId;
    private double publicRating;

    @JsonGetter("genres")
    public List<GenreDto> getGenres() {
        if (mediaGenres == null) {
            return new ArrayList<>();
        }
        
        List<GenreDto> result = new ArrayList<>();
        for (LinkMediaGenre link : mediaGenres) {
            Genre genre = link.getGenre();
            if (genre != null) {
                result.add(new GenreDto(genre.getId(), genre.getName()));
            }
        }
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Integer getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(Integer tmdbId) {
        this.tmdbId = tmdbId;
    }

    public double getPublicRating() {
        return publicRating;
    }

    public void setPublicRating(double publicRating) {
        this.publicRating = publicRating;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public List<LinkMediaGenre> getMediaGenres() {
        return mediaGenres;
    }

    public void setMediaGenres(List<LinkMediaGenre> mediaGenres) {
        this.mediaGenres = mediaGenres;
    }

    public Media() {
    }

    public Media(String title, String description, LocalDate releaseDate, String country, String coverUrl, Integer tmdbId, double publicRating, String mediaType, List<LinkMediaGenre> mediaGenres) {
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.country = country;
        this.coverUrl = coverUrl;
        this.tmdbId = tmdbId;
        this.publicRating = publicRating;
        this.mediaType = mediaType;
        this.mediaGenres = mediaGenres;
    }
}