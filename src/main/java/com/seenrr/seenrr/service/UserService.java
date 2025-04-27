package com.seenrr.seenrr.service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.seenrr.seenrr.dto.ReviewDto;
import com.seenrr.seenrr.dto.UserDto;
import com.seenrr.seenrr.entity.Review;
import com.seenrr.seenrr.entity.ReviewVote;
import com.seenrr.seenrr.entity.User;
import com.seenrr.seenrr.repository.ReviewRepository;
import com.seenrr.seenrr.repository.ReviewVoteRepository;
import com.seenrr.seenrr.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewVoteRepository reviewVoteRepository;

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
        validateRequiredFields(username, "Nom d'utilisateur");
        validateRequiredFields(email, "Email");
        validateRequiredFields(password, "Mot de passe");
        validateEmail(email);
        validatePassword(password);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Nom d'utilisateur déjà utilisé.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Adresse e-mail déjà utilisée.");
        }

        String encodedPassword = encoderService.encodeToSha256(password);
        User user = new User(username, email, encodedPassword);
        return userRepository.save(user);
    }

    public Map<String, String> logUser(String username, String password) throws NoSuchAlgorithmException {
        validateRequiredFields(username, "Nom d'utilisateur");
        validateRequiredFields(password, "Mot de passe");
        String encodedPassword = encoderService.encodeToSha256(password);
        User foundUser = userRepository.findByUsernameAndPassword(username, encodedPassword);
        if (foundUser == null) {
            throw new IllegalArgumentException("Nom d'utilisateur ou mot de passe incorrect.");
        }
        String token = JwtService.generateToken(foundUser.getUsername());
        Map<String, String> loggedUser = new HashMap<>();
        loggedUser.put("id", foundUser.getId().toString());
        loggedUser.put("username", foundUser.getUsername());
        loggedUser.put("email", foundUser.getEmail());
        loggedUser.put("token", token);
        return loggedUser;
    }

    public Map<String, String> logUser2FA(String username, String password, String code) throws NoSuchAlgorithmException {
        validateRequiredFields(username, "Nom d'utilisateur");
        validateRequiredFields(password, "Mot de passe");
        validateRequiredFields(code, "Code 2FA");
        
        String encodedPassword = encoderService.encodeToSha256(password);
        User foundUser = userRepository.findByUsernameAndPassword(username, encodedPassword);
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
        return null;
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

    public UserDto getUserDtoById(Integer userId, String token) {
        validateToken(token);
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }
        return toDto(user);
    }

    public User getUserById(Long userId, String token) {
        validateToken(token);
        Integer id = Math.toIntExact(userId);
        User user = userRepository.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }
        return user;
    }

    public Map<String, String> forgotPassword(String email) {
        validateRequiredFields(email, "Email");
        validateEmail(email);
        User foundUser = userRepository.findByEmail(email);
        if (foundUser == null) {
            throw new IllegalArgumentException("Aucun utilisateur trouvé avec cette adresse e-mail.");
        }
        String passwordResetToken = JwtService.generateTemporaryToken(30);
        foundUser.setPasswordResetToken(passwordResetToken);
        userRepository.save(foundUser);
        String resetLink = "http://localhost:8080/security/reset-password?token=" + passwordResetToken; // A CHANGER AVEC URL FRONT
        try {
            mailjetEmailService.sendEmail("socooolmeen@gmail.com", email, foundUser.getUsername(), "Réinitialisation de mot de passe", resetLink, resetLink);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> verifyPasswordReset(String email, String token) throws NoSuchAlgorithmException {
        validateRequiredFields(email, "Email");
        validateRequiredFields(token, "Token");
        validateEmail(email);
        User foundUser = userRepository.findByEmail(email);
        if (foundUser == null) {
            throw new IllegalArgumentException("Aucun utilisateur trouvé avec cette adresse e-mail.");
        }
        if (!foundUser.getPasswordResetToken().equals(token)) {
            throw new IllegalArgumentException("Token invalide.");
        }
        return null;
    }

    public Map<String, String> resetPassword(String email, String token, String newPassword) throws NoSuchAlgorithmException {
        validateRequiredFields(email, "Email");
        validateRequiredFields(token, "Token");
        validateRequiredFields(newPassword, "Nouveau mot de passe");
        
        validateEmail(email);
        validatePassword(newPassword);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Aucun utilisateur trouvé avec cette adresse e-mail.");
        }
        
        if (user.getPasswordResetToken() == null || !user.getPasswordResetToken().equals(token)) {
            throw new IllegalArgumentException("Token invalide.");
        }
        String encodedPassword = encoderService.encodeToSha256(newPassword);
        user.setPassword(encodedPassword);
        user.setPasswordResetToken(null);
        userRepository.save(user);
        return null;
    }

    public User getUserByUsername(String tokenUsername) {
        User user = userRepository.findByUsername(tokenUsername);
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }
        return user;
    }

    public Set<ReviewDto> getUserReviews(Integer id, String token) {
        validateToken(token);
        Set<ReviewDto> reviews = userRepository.findById(id).getReviews();
        if (reviews == null) {
            throw new IllegalArgumentException("Aucune critique trouvée pour cet utilisateur.");
        }
        return reviews;
    }

    public void followUser(Integer id, String token) {
        validateToken(token);
        User userToFollow = userRepository.findById(id);
        if (userToFollow == null) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }
        String tokenUsername = JwtService.extractUsername(token);
        User loggedUser = userRepository.findByUsername(tokenUsername);
        if (loggedUser == null) {
            throw new IllegalArgumentException("Token invalide.");
        }
        loggedUser.follow(userToFollow);
        userRepository.save(loggedUser);
    }

    public void unfollowUser(Integer id, String token) {
        validateToken(token);
        User userToUnfollow = userRepository.findById(id);
        if (userToUnfollow == null) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }
        String tokenUsername = JwtService.extractUsername(token);
        User loggedUser = userRepository.findByUsername(tokenUsername);
        if (loggedUser == null) {
            throw new IllegalArgumentException("Token invalide.");
        }
        loggedUser.unfollow(userToUnfollow);
        userRepository.save(loggedUser);
    }

    public Page<ReviewDto> getFollowingReviews(String token, int page, int size) {
        validateToken(token);
        String tokenUsername = JwtService.extractUsername(token);
        User loggedUser = userRepository.findByUsername(tokenUsername);

        if (loggedUser == null) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }

        List<ReviewDto> reviews = new ArrayList<>();
        reviews.addAll(loggedUser.getReviews());

        if (loggedUser.getFollowing() != null && !loggedUser.getFollowing().isEmpty()) {
            for (User user : loggedUser.getFollowing()) {
                reviews.addAll(user.getReviews());
            }
        }

        if (reviews.isEmpty()) {
            throw new IllegalArgumentException("Aucune critique trouvée pour les utilisateurs suivis.");
        }

        for (ReviewDto review : reviews) {
            Optional<Review> loadedReview = reviewRepository.findById(review.getId());
            Boolean userHasLiked = reviewVoteRepository.existsByReviewAndUserAndVoteType(loadedReview.get(), loggedUser, ReviewVote.VoteType.LIKE);
            Boolean userHasDisliked = reviewVoteRepository.existsByReviewAndUserAndVoteType(loadedReview.get(), loggedUser, ReviewVote.VoteType.DISLIKE);
            review.setIsLiked(userHasLiked);
            review.setIsDisliked(userHasDisliked);
        }

        reviews.sort(Comparator.comparing(ReviewDto::getCreatedAt).reversed());

        int start = Math.min(page * size, reviews.size());
        int end = Math.min(start + size, reviews.size());

        List<ReviewDto> pagedList = reviews.subList(start, end);

        return new PageImpl<>(pagedList, PageRequest.of(page, size), reviews.size());
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }

    private void validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token requis.");
        }
        if (!JwtService.isValidToken(token)) {
            throw new IllegalArgumentException("Token invalide.");
        }
        if (JwtService.isTokenExpired(token)) {
            throw new IllegalArgumentException("Token expiré.");
        }
    }
    
    private void validateRequiredFields(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " requis.");
        }
    }
    
    private void validateEmail(String email) {
        if (!PATTERN_Email.matcher(email).matches()) {
            throw new IllegalArgumentException("Adresse e-mail invalide.");
        }
    }
    
    private void validatePassword(String password) {
        if (!PATTERN_Password.matcher(password).matches()) {
            throw new IllegalArgumentException("Complexité du mot de passe insuffisante.");
        }
    }
}
