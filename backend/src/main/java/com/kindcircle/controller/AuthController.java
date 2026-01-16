package com.kindcircle.controller;

import com.kindcircle.dto.LoginRequest;
import com.kindcircle.dto.RegisterRequest;
import com.kindcircle.dto.AuthResponse;
import com.kindcircle.model.User;
import com.kindcircle.service.AuthService;
import com.kindcircle.service.EmailService;
import com.kindcircle.service.EmailVerificationService;
import com.kindcircle.service.JwtService;
import com.kindcircle.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private com.kindcircle.repository.UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userService.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFullName(request.getFullName());
        user.setBloodGroup(request.getBloodGroup());
        user.setCity(request.getCity());
        user.setCurrentImpactScore(0);
        user.setEmailVerified(false); // Email not verified yet

        User savedUser = userService.save(user);
        
        // Generate verification token and send email
        try {
            String verificationToken = emailVerificationService.generateVerificationToken(savedUser);
            emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken, savedUser.getFullName());
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            // Continue with registration even if email fails
        }
        
        String token = jwtService.generateToken(savedUser);

        return ResponseEntity.ok(new AuthResponse(token, savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.getEmail(), request.getPassword());
        
        // Check if email is verified
        if (user.getEmailVerified() == null || !user.getEmailVerified()) {
            throw new RuntimeException("Please verify your email address before logging in. Check your inbox for the verification email.");
        }
        
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // JWT is stateless, so logout is handled client-side by removing token
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        try {
            boolean verified = emailVerificationService.verifyEmail(request.getToken());
            if (verified) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Email verified successfully");
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("Invalid or expired verification token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify email: " + e.getMessage());
        }
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@Valid @RequestBody ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        emailVerificationService.resendVerificationEmail(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification email sent successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String token = emailVerificationService.generatePasswordResetToken(request.getEmail());
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getFullName());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset email sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Don't reveal if email exists or not for security
            Map<String, String> response = new HashMap<>();
            response.put("message", "If the email exists, a password reset link has been sent");
            return ResponseEntity.ok(response);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        User user = emailVerificationService.verifyPasswordResetToken(request.getToken());
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Mark token as used
        emailVerificationService.markPasswordResetTokenAsUsed(request.getToken());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        return ResponseEntity.ok(response);
    }
    
    // Inner classes for requests
    public static class VerifyEmailRequest {
        @jakarta.validation.constraints.NotBlank(message = "Token is required")
        private String token;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
    
    public static class ResendVerificationRequest {
        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        @jakarta.validation.constraints.Email(message = "Invalid email format")
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class ForgotPasswordRequest {
        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        @jakarta.validation.constraints.Email(message = "Invalid email format")
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class ResetPasswordRequest {
        @jakarta.validation.constraints.NotBlank(message = "Token is required")
        private String token;
        
        @jakarta.validation.constraints.NotBlank(message = "New password is required")
        @jakarta.validation.constraints.Size(min = 6, message = "Password must be at least 6 characters")
        private String newPassword;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
