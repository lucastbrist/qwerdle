package com.ltb.qwerdle.services;

import com.ltb.qwerdle.models.User;
import com.ltb.qwerdle.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    @Transactional
    public void registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("A user with that username already exists. " +
                    "Please enter a new username.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("A user with that email already exists. " +
                    "Please try again with a new email or log in instead.");
        }

        validatePassword(password);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setRole("ROLE_USER");

        log.info("Registering new user: {}", username);
        userRepository.save(user);
    }

    @Transactional
    public User updateUser(User updatedUser) {
        User user = getUserByUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setEnabled(updatedUser.isEnabled());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }

    @Transactional
    public void recordWin(String username) {
        User user = getUserByUsername(username);

        user.setGamesWon(user.getGamesWon() + 1);
        user.setCurrentStreak(user.getCurrentStreak() + 1);

        if (user.getCurrentStreak() > user.getMaxStreak()) {
            user.setMaxStreak(user.getCurrentStreak());
        }

        userRepository.save(user);
        log.info("Recorded win for user: {}", username);
    }

    @Transactional
    public void recordLoss(String username) {
        User user = getUserByUsername(username);

        user.setGamesLost(user.getGamesLost() + 1);
        user.setCurrentStreak(0);

        userRepository.save(user);
        log.info("Recorded loss for user: {}", username);
    }

}