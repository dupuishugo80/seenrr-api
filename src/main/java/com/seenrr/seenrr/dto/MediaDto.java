package com.seenrr.seenrr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class MediaDto {
    private int id;
    private String title;

    @JsonProperty("overview")
    private String description;

    @JsonProperty("release_date")
    private String releaseDate;

    private List<GenreDto> genres;

    @JsonProperty("origin_country")
    private List<String> countries;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("vote_average")
    private double voteAverage;

    @JsonProperty("name")
    private String name;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    public static class GenreDto {
        private int id;
        private String name;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public List<GenreDto> getGenres() { return genres; }
    public void setGenres(List<GenreDto> genres) { this.genres = genres; }

    public List<String> getCountries() { return countries; }
    public void setCountries(List<String> countries) { this.countries = countries; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFirstAirDate() { return firstAirDate; }
    public void setFirstAirDate(String firstAirDate) { this.firstAirDate = firstAirDate; }
}
