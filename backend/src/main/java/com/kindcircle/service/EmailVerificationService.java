package com.kindcircle.service;

import com.kindcircle.model.EmailVerificationToken;
import com.kindcircle.model.User;
import com.kindcircle.repository.EmailVerificationTokenRepository;
import com.kindcircle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService {
    
    @Autowired
    private EmailVerificationTokenRepository tokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Generate and save email verification token
     */
    @Transactional
    public String generateVerificationToken(User user) {
        // Delete any existing tokens for this user
        List<EmailVerificationToken> existingTokens = tokenRepository.findByUserId(user.getId());
        tokenRepository.deleteAll(existingTokens);
        
        // Generate new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(24); // 24 hours expiry
        
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setTokenType(EmailVerificationToken.TokenType.EMAIL_VERIFICATION);
        verificationToken.setExpiresAt(expiry);
        verificationToken.setUsed(false);
        
        tokenRepository.save(verificationToken);
        
        // Also save token in user table for quick access
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(expiry);
        userRepository.save(user);
        
        return token;
    }
    
    /**
     * Verify email using token
     */
    @Transactional
    public boolean verifyEmail(String token) {
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByTokenAndTokenType(
            token, EmailVerificationToken.TokenType.EMAIL_VERIFICATION);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        EmailVerificationToken verificationToken = tokenOpt.get();
        
        // Check if already used
        if (verificationToken.getUsed()) {
            return false;
        }
        
        // Check if expired
        if (verificationToken.isExpired()) {
            return false;
        }
        
        // Mark token as used and verify user email
        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);
        
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
        
        return true;
    }
    
    /**
     * Generate password reset token
     */
    @Transactional
    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Delete any existing password reset tokens for this user
        List<EmailVerificationToken> existingTokens = tokenRepository.findByUserId(user.getId());
        existingTokens.stream()
            .filter(token -> token.getTokenType() == EmailVerificationToken.TokenType.PASSWORD_RESET)
            .forEach(tokenRepository::delete);
        
        // Generate new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1); // 1 hour expiry
        
        EmailVerificationToken resetToken = new EmailVerificationToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setTokenType(EmailVerificationToken.TokenType.PASSWORD_RESET);
        resetToken.setExpiresAt(expiry);
        resetToken.setUsed(false);
        
        tokenRepository.save(resetToken);
        
        // Also save in user table
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(expiry);
        userRepository.save(user);
        
        return token;
    }
    
    /**
     * Verify password reset token
     */
    @Transactional
    public User verifyPasswordResetToken(String token) {
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByTokenAndTokenType(
            token, EmailVerificationToken.TokenType.PASSWORD_RESET);
        
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Invalid or expired password reset token");
        }
        
        EmailVerificationToken resetToken = tokenOpt.get();
        
        // Check if already used
        if (resetToken.getUsed()) {
            throw new RuntimeException("Password reset token has already been used");
        }
        
        // Check if expired
        if (resetToken.isExpired()) {
            throw new RuntimeException("Password reset token has expired");
        }
        
        return resetToken.getUser();
    }
    
    /**
     * Mark password reset token as used
     */
    @Transactional
    public void markPasswordResetTokenAsUsed(String token) {
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByTokenAndTokenType(
            token, EmailVerificationToken.TokenType.PASSWORD_RESET);
        
        if (tokenOpt.isPresent()) {
            EmailVerificationToken resetToken = tokenOpt.get();
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
            
            User user = resetToken.getUser();
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);
            userRepository.save(user);
        }
    }
    
    /**
     * Resend verification email
     */
    @Transactional
    public void resendVerificationEmail(User user) {
        if (user.getEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }
        
        String token = generateVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), token, user.getFullName());
    }
}
