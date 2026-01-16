package com.kindcircle.service;

import com.kindcircle.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User authenticate(String email, String password) {
        User user = null;
        try {
            // Try to find by email
            user = userService.findByEmail(email);
        } catch (Exception e) {
            // If not found by email, try by username
            try {
                user = userService.findByUsername(email);
            } catch (Exception ex) {
                throw new RuntimeException("Invalid email or password");
            }
        }
        
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        return user;
    }
}
