package com.seenrr.seenrr.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.seenrr.seenrr.dto.ApiResponseDto;
import com.seenrr.seenrr.dto.ReviewDto;
import com.seenrr.seenrr.dto.UserDto;
import com.seenrr.seenrr.service.UserService;

import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto> getUser(@RequestHeader("Authorization") String authHeader, @PathVariable("id") Integer id) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            UserDto user = userService.getUserDtoById(id, token);
            return new ApiResponseDto(true, "", user);
        });
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponseDto> getUserReviews(@RequestHeader("Authorization") String authHeader, @PathVariable("id") Integer id,
    @RequestParam(defaultValue = "0") int page,
     @RequestParam(defaultValue = "15") int size) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            Page<ReviewDto> reviews = userService.getUserReviews(id, token, page, size);
            return new ApiResponseDto(true, "", reviews);
        });
    }

    @GetMapping("/{id}/follow")
    public ResponseEntity<ApiResponseDto> followUser(@RequestHeader("Authorization") String authHeader, @PathVariable("id") Integer id) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            userService.followUser(id, token);
            return new ApiResponseDto(true, "Vous suivez maintenant cet utilisateur.", null);
        });
    }

    @GetMapping("/{id}/unfollow")
    public ResponseEntity<ApiResponseDto> unfollowUser(@RequestHeader("Authorization") String authHeader, @PathVariable("id") Integer id) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            userService.unfollowUser(id, token);
            return new ApiResponseDto(true, "Vous ne suivez plus cet utilisateur.", null);
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
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto(false, "Une erreur inattendue est survenue.", null));
        }
    }
    
    private <T> ResponseEntity<ApiResponseDto> executeWithTokenAndHandleExceptions(
        String authHeader, 
        java.util.function.Function<String, ApiResponseDto> action) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto(false, "Token manquant ou invalide", null));
        }
        
        String token = authHeader.substring(7);
        return executeAndHandleExceptions(() -> action.apply(token));
    }
}
