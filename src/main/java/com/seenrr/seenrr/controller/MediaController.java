package com.seenrr.seenrr.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seenrr.seenrr.dto.ApiResponseDto;
import com.seenrr.seenrr.entity.Media;
import com.seenrr.seenrr.service.MediaService;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaService MediaService;
    
    @GetMapping("/fetch/{id}")
    public ResponseEntity<ApiResponseDto> getMediaTest(@PathVariable("id") Integer id) {
        return executeAndHandleExceptions(() -> {
            Media media = MediaService.getMediaById(id, "movie");
            return new ApiResponseDto(true, "", media);
        });
    }
    
    @GetMapping("/delete/{id}")
    public ResponseEntity<ApiResponseDto> deleteMedia(@PathVariable("id") Integer id) {
        return executeAndHandleExceptions(() -> {
            MediaService.deleteMediaById(id);
            return new ApiResponseDto(true, "", null);
        });
    }

    private <T> ResponseEntity<ApiResponseDto> executeAndHandleExceptions(Supplier<ApiResponseDto> action) {
        try {
            return ResponseEntity.ok(action.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto(false, "Une erreur inattendue est survenue.", null));
        }
    }
    
}
