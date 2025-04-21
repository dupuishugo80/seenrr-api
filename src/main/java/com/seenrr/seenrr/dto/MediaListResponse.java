package com.seenrr.seenrr.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seenrr.seenrr.entity.Media;

public class MediaListResponse {
    private int page;
    private List<Media> results;
    @JsonProperty("total_results")
    private int totalResults;
    @JsonProperty("total_pages")
    private int totalPages;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<Media> getResults() {
        return results;
    }

    public void setResults(List<Media> results) {
        this.results = results;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
