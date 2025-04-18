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

    @Autowired
    private MailjetEmailService mailjetEmailService;

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

    public Map<String, String> forgotPassword(String entity) {
        if (entity == null || entity.isEmpty()) {
            throw new IllegalArgumentException("Adresse e-mail requise.");
        }
        if (!isValid(entity, PATTERN_Email)) {
            throw new IllegalArgumentException("Adresse e-mail invalide.");
        }
        User foundUser = userRepository.findByEmail(entity);
        if (foundUser == null) {
            throw new IllegalArgumentException("Aucun utilisateur trouvé avec cette adresse e-mail.");
        }
        String passwordResetToken = JwtService.generateTemporaryToken(30);
        foundUser.setPasswordResetToken(passwordResetToken);
        userRepository.save(foundUser);
        String resetLink = "http://localhost:8080/security/reset-password?token=" + passwordResetToken; // A CHANGER AVEC URL FRONT
        try {
            mailjetEmailService.sendEmail("socooolmeen@gmail.com", entity, foundUser.getUsername(), "Réinitialisation de mot de passe", resetLink, resetLink);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Map.of("result", "Un e-mail de réinitialisation de mot de passe a été envoyé à l'adresse fournie.");
    }

    public Map<String, String> verifyPasswordReset(String email, String token) throws NoSuchAlgorithmException {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token requis.");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email requis.");
        }
        if (!isValid(email, PATTERN_Email)) {
            throw new IllegalArgumentException("Adresse e-mail invalide.");
        }
        User foundUser = userRepository.findByEmail(email);
        if (foundUser == null) {
            throw new IllegalArgumentException("Aucun utilisateur trouvé avec cette adresse e-mail.");
        }
        if (!foundUser.getPasswordResetToken().equals(token)) {
            throw new IllegalArgumentException("Token invalide.");
        }
        return Map.of("isValid", "true");
    }

    public Map<String, String> resetPassword(String email, String token, String newPassword) throws NoSuchAlgorithmException {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email requis.");
        }
        if (!isValid(email, PATTERN_Email)) {
            throw new IllegalArgumentException("Adresse e-mail invalide.");
        }
        User foundUser = userRepository.findByEmail(email);
        if (foundUser == null) {
            throw new IllegalArgumentException("Aucun utilisateur trouvé avec cette adresse e-mail.");
        }
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token requis.");
        }
        if (!foundUser.getPasswordResetToken().equals(token)) {
            throw new IllegalArgumentException("Token invalide.");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("Nouveau mot de passe requis.");
        }
        if (!isValid(newPassword, PATTERN_Password)) {
            throw new IllegalArgumentException("Compléxité du mot de passe insuffisante.");
        }
        String encodedPassword = encoderService.encodeToSha256(newPassword);
        foundUser.setPassword(encodedPassword);
        foundUser.setPasswordResetToken(null);
        userRepository.save(foundUser);
        return Map.of("result", "Mot de passe réinitialisé avec succès.");
    }
    
}
