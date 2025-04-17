package com.seenrr.seenrr.service;

import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.seenrr.seenrr.entity.User;
import com.seenrr.seenrr.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncoderService encoderService;

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_REGEX);

    public static boolean isValid(String input) {
        return PATTERN.matcher(input).matches();
    }

    public User createUser(String username, String email, String password) throws NoSuchAlgorithmException {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Nom d'utilisateur déjà utilisé.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Adresse e-mail déjà utilisée.");
        }
        if (!isValid(password)) {
            throw new IllegalArgumentException("Compléxité du mot de passe insuffisante.");
        }
        String encodedPassword = encoderService.encodeToSha256(password);
        User user = new User(username, email, encodedPassword);
        return userRepository.save(user);
    }
}
