package com.seenrr.seenrr.controller;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seenrr.seenrr.dto.ApiResponseDto;
import com.seenrr.seenrr.dto.Login2FADto;
import com.seenrr.seenrr.dto.LoginDto;
import com.seenrr.seenrr.dto.PasswordResetDto;
import com.seenrr.seenrr.dto.UserDto;
import com.seenrr.seenrr.entity.User;
import com.seenrr.seenrr.service.UserService;

@RestController
@RequestMapping("/security")
public class SecurityController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto> registerUser(@RequestBody UserDto userDto) {
        return executeAndHandleExceptions(() -> {
            User createdUser;
            try {
                createdUser = userService.createUser(userDto.getUsername(), userDto.getEmail(), userDto.getPassword());
                return new ApiResponseDto(true, "Utilisateur enregistré avec succès", createdUser);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto> loginUser(@RequestBody LoginDto LoginDto) {
        return executeAndHandleExceptions(() -> {
            Map<String, String> userInfo;
            try {
                userInfo = userService.logUser(LoginDto.getUsername(), LoginDto.getPassword());
                if (userInfo == null) {
                    throw new IllegalArgumentException("Nom d'utilisateur ou mot de passe incorrect.");
                }
                return new ApiResponseDto(true, "Connexion réussie", userInfo);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @PostMapping("/login-2fa")
    public ResponseEntity<ApiResponseDto> loginUser2FA(@RequestBody Login2FADto request) {
        return executeAndHandleExceptions(() -> {
            Map<String, String> userInfo;
            try {
                userInfo = userService.logUser2FA(
                        request.getUsername(), 
                        request.getPassword(), 
                        request.getCode());
                if (userInfo == null) {
                    throw new IllegalArgumentException("Nom d'utilisateur, mot de passe ou code 2FA incorrect.");
                }
                return new ApiResponseDto(true, "Connexion avec 2FA réussie", userInfo);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @GetMapping("/enable-2fa")
    public ResponseEntity<ApiResponseDto> enable2FA(@RequestHeader("Authorization") String authHeader) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            Map<String, String> result = userService.enable2FA(token);
            return new ApiResponseDto(true, "2FA activé avec succès", Map.of(
                    "qrCodeUrl", result.get("qrUrl"), 
                    "secret", result.get("secret")));
        });
    }

    @GetMapping("/disable-2fa")
    public ResponseEntity<ApiResponseDto> disable2FA(@RequestHeader("Authorization") String authHeader) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            Map<String, String> result = userService.disable2FA(token);
            return new ApiResponseDto(true, "2FA désactivé avec succès", result);
        });
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDto> forgotPassword(@RequestBody UserDto user) {
        return executeAndHandleExceptions(() -> {
            Map<String, String> response = userService.forgotPassword(user.getEmail());
            return new ApiResponseDto(true, "Email de réinitialisation envoyé", response);
        });
    }

    @PostMapping("/verify-password-reset")
    public ResponseEntity<ApiResponseDto> verifyPasswordReset(@RequestBody PasswordResetDto request) {
        return executeAndHandleExceptions(() -> {
            Map<String, String> response;
            try {
                response = userService.verifyPasswordReset(request.getEmail(), request.getToken());
                return new ApiResponseDto(true, "Token vérifié avec succès", response);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @PostMapping("/password-reset")
    public ResponseEntity<ApiResponseDto> resetPassword(@RequestBody PasswordResetDto request) {
        return executeAndHandleExceptions(() -> {
            Map<String, String> response;
            try {
                response = userService.resetPassword(
                        request.getEmail(), 
                        request.getToken(), 
                        request.getPassword());
                return new ApiResponseDto(true, "Mot de passe réinitialisé avec succès", response);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDto> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        return executeWithTokenAndHandleExceptions(authHeader, token -> {
            User user = userService.getUserProfile(token);
            return new ApiResponseDto(true, "Profil récupéré avec succès", user);
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
