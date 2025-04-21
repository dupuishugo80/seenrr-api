package com.seenrr.seenrr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.seenrr.seenrr.dto.MediaDto;
import com.seenrr.seenrr.entity.Genre;
import com.seenrr.seenrr.entity.LinkMediaGenre;
import com.seenrr.seenrr.entity.Media;
import com.seenrr.seenrr.repository.GenreRepository;
import com.seenrr.seenrr.repository.LinkMediaGenreRepository;
import com.seenrr.seenrr.repository.MediaRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:application.properties")
public class MediaService {
    @Autowired
    private Environment env;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private GenreRepository genreRepository;
    
    @Autowired
    private LinkMediaGenreRepository linkMediaGenreRepository;

    private RestTemplate restTemplate = new RestTemplate();

    public Media getMediaById(Integer id, String type) {
        Media media = mediaRepository.findByTmdbId(id);
        if (media != null) {
            return media;
        }
        
        String apiKey = env.getProperty("tmbd.key");
        String apiUrl = "https://api.themoviedb.org/3/" + type + "/" + id + "?api_key=" + apiKey + "&language=fr-FR";
        MediaDto mediaDto;
        try {
            mediaDto = restTemplate.getForObject(apiUrl, MediaDto.class);
            if (mediaDto == null) {
                throw new IllegalArgumentException("Aucun média correspond à cet ID.");
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Aucun média correspond à cet ID.");
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Erreur lors de l'appel à l'API : " + e.getStatusCode(), e);
        }
        
        Media mediaFetch = new Media();
        mediaFetch.setTmdbId(mediaDto.getId());
        mediaFetch.setTitle(mediaDto.getTitle());
        mediaFetch.setDescription(mediaDto.getDescription());
        mediaFetch.setMediaType(type);
        
        if (mediaDto.getReleaseDate() != null && !mediaDto.getReleaseDate().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(mediaDto.getReleaseDate(), DateTimeFormatter.ISO_DATE);
                mediaFetch.setReleaseDate(date);
            } catch (Exception e) {
                System.err.println("Erreur de parsing de la date: " + mediaDto.getReleaseDate());
            }
        }
        
        if (mediaDto.getCountries() != null && !mediaDto.getCountries().isEmpty()) {
            mediaFetch.setCountry(String.join(", ", mediaDto.getCountries()));
        }
        
        if (mediaDto.getPosterPath() != null) {
            mediaFetch.setCoverUrl("https://image.tmdb.org/t/p/original" + mediaDto.getPosterPath());
        }
        
        mediaFetch.setPublicRating(mediaDto.getVoteAverage());
        
        mediaFetch = mediaRepository.save(mediaFetch);
        
        if (mediaDto.getGenres() != null) {
            List<LinkMediaGenre> links = new ArrayList<>();
            for (MediaDto.GenreDto genreDto : mediaDto.getGenres()) {
                Genre genre = genreRepository.findById(genreDto.getId())
                    .orElseGet(() -> {
                        Genre newGenre = new Genre();
                        newGenre.setId(genreDto.getId());
                        newGenre.setName(genreDto.getName());
                        return genreRepository.save(newGenre);
                    });
                
                LinkMediaGenre linkMediaGenre = new LinkMediaGenre(mediaFetch, genre);
                linkMediaGenre = linkMediaGenreRepository.save(linkMediaGenre);
                links.add(linkMediaGenre);
            }
            mediaFetch.setMediaGenres(links);
            mediaFetch = mediaRepository.save(mediaFetch);
        }
        
        return mediaRepository.findById(mediaFetch.getId()).orElse(null);
    }

    public void deleteMediaById(Integer id) {
        Media media = mediaRepository.findByTmdbId(id);
        if (media == null) {
            throw new IllegalArgumentException("Aucun média trouvé avec cet ID.");
        }
        mediaRepository.delete(media);
    }
}