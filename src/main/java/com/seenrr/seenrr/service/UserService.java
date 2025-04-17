package com.seenrr.seenrr.service;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.seenrr.seenrr.entity.User;
import com.seenrr.seenrr.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncoderService encoderService;

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$";
    private static final Pattern PATTERN_Password = Pattern.compile(PASSWORD_REGEX);

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern PATTERN_Email = Pattern.compile(EMAIL_REGEX);

    public static boolean isValid(String input, Pattern pattern) {
        return pattern.matcher(input).matches();
    }

    public User createUser(String username, String email, String password) throws NoSuchAlgorithmException {
        if (username == null || username.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Tous les champs sont requis.");
        }
        if(!isValid(email, PATTERN_Email)) {
            throw new IllegalArgumentException("Adresse e-mail invalide.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Nom d'utilisateur déjà utilisé.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Adresse e-mail déjà utilisée.");
        }
        if (!isValid(password, PATTERN_Password)) {
            throw new IllegalArgumentException("Compléxité du mot de passe insuffisante.");
        }
        String encodedPassword = encoderService.encodeToSha256(password);
        User user = new User(username, email, encodedPassword);
        return userRepository.save(user);
    }

    public Map<String, String> logUser(String username, String password) throws NoSuchAlgorithmException {
        String encodedPassword = encoderService.encodeToSha256(password);
        User foundUser = userRepository.findByUsernameAndPassword(username, encodedPassword);
        if (foundUser == null) {
            throw new IllegalArgumentException("Identifiants invalide.");
        }
        if(foundUser.isTwoFaEnabled()) {
            throw new IllegalArgumentException("2FA activé. Veuillez le désactiver pour vous connecter.");
        }
        String token = JwtService.generateToken(foundUser.getUsername());
        Map<String, String> loggedUser = new HashMap<>();
        loggedUser.put("username", foundUser.getUsername());
        loggedUser.put("email", foundUser.getEmail());
        loggedUser.put("token", token);
        return loggedUser;
    }

    public Map<String, String> logUser2FA(String username, String password, String code) throws NoSuchAlgorithmException {
        if(code.isEmpty() || !code.matches("\\d+")) {
            throw new IllegalArgumentException("Code 2FA mal formaté.");
        }
        String encodedPassword = encoderService.encodeToSha256(password);
        User foundUser = userRepository.findByUsernameAndPassword(username, encodedPassword);
        if (foundUser == null) {
            throw new IllegalArgumentException("Identifiants invalide.");
        }
        if (!foundUser.isTwoFaEnabled()) {
            throw new IllegalArgumentException("2FA non activé. Veuillez utiliser la méthoque classique pour vous connecter.");
        } 
        if(!TwoFactorAuthService.verifyCode(foundUser.getTwoFaSecret(), Integer.parseInt(code))) {
            throw new IllegalArgumentException("Code 2FA invalide.");
        }
        
        String token = JwtService.generateToken(foundUser.getUsername());
        Map<String, String> loggedUser = new HashMap<>();
        loggedUser.put("username", foundUser.getUsername());
        loggedUser.put("email", foundUser.getEmail());
        loggedUser.put("token", token);
        return loggedUser;
    }

    public Map<String, String> enable2FA(String token) {
        if (!JwtService.isValidToken(token)) {
            throw new IllegalArgumentException("Token invalide.");
        }
        if (JwtService.isTokenExpired(token)) {
            throw new IllegalArgumentException("Token expiré.");
        }
        String tokenUsername = JwtService.extractUsername(token);
        User user = userRepository.findByUsername(tokenUsername);
        if (user.isTwoFaEnabled()) {
            throw new IllegalArgumentException("2FA déjà activé.");
        }

        GoogleAuthenticatorKey secret = TwoFactorAuthService.generateSecretKey();
        String secretKey = secret.getKey();
        user.setTwoFaSecret(secretKey);
        user.setTwoFaEnabled(true);
        userRepository.save(user);
        String qrUrl = TwoFactorAuthService.getQrCodeUrl(user.getEmail(), secret);
        Map<String, String> result = new HashMap<>();
        result.put("qrUrl", qrUrl);
        result.put("secret", secretKey);
        return result;
    }

    public Map<String, String> disable2FA(String token) {
        if (!JwtService.isValidToken(token)) {
            throw new IllegalArgumentException("Token invalide.");
        }
        if (JwtService.isTokenExpired(token)) {
            throw new IllegalArgumentException("Token expiré.");
        }
        String tokenUsername = JwtService.extractUsername(token);
        User user = userRepository.findByUsername(tokenUsername);
        if (!user.isTwoFaEnabled()) {
            throw new IllegalArgumentException("2FA non activé.");
        } 
        user.setTwoFaEnabled(false);
        user.setTwoFaSecret(null);
        userRepository.save(user);
        Map<String, String> result = new HashMap<>();
        result.put("result", "2FA désactivé avec succès.");
        return result;
    }

    public User getUserProfile(String token) {
        if (!JwtService.isValidToken(token)) {
            throw new IllegalArgumentException("Token invalide.");
        }
        if (JwtService.isTokenExpired(token)) {
            throw new IllegalArgumentException("Token expiré.");
        }
        
        String tokenUsername = JwtService.extractUsername(token);
        User user = userRepository.findByUsername(tokenUsername);
        return user;
    }
    
}
