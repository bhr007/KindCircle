package com.kindcircle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    /**
     * Check if email is configured
     */
    public boolean isEmailConfigured() {
        return fromEmail != null && !fromEmail.isEmpty() && !fromEmail.equals("your-email@gmail.com");
    }
    
    /**
     * Send email verification email
     */
    public void sendVerificationEmail(String toEmail, String token, String userName) {
        if (!isEmailConfigured()) {
            System.out.println("Email not configured. Skipping verification email to: " + toEmail);
            System.out.println("Verification token (for testing): " + token);
            return; // Don't throw error if email not configured
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify Your KindCircle Account");
            
            String htmlContent = buildVerificationEmailHtml(userName, verificationUrl);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("Verification email sent to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            e.printStackTrace();
            // Log but don't fail registration if email fails
            System.out.println("Verification token (for manual testing): " + token);
        }
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String token, String userName) {
        if (!isEmailConfigured()) {
            System.out.println("Email not configured. Skipping password reset email to: " + toEmail);
            System.out.println("Reset token (for testing): " + token);
            throw new RuntimeException("Email service is not configured. Please configure email settings.");
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your KindCircle Password");
            
            String htmlContent = buildPasswordResetEmailHtml(userName, resetUrl);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("Password reset email sent to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    private String buildVerificationEmailHtml(String userName, String verificationUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #dc3545;'>Welcome to KindCircle!</h2>" +
                "<p>Hello " + userName + ",</p>" +
                "<p>Thank you for registering with KindCircle. Please verify your email address by clicking the button below:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + verificationUrl + "' style='background-color: #dc3545; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Verify Email</a>" +
                "</div>" +
                "<p>Or copy and paste this link into your browser:</p>" +
                "<p style='word-break: break-all; color: #666;'>" + verificationUrl + "</p>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>If you didn't create an account with KindCircle, please ignore this email.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #666; font-size: 12px;'>KindCircle - Making a difference, one act of kindness at a time.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private String buildPasswordResetEmailHtml(String userName, String resetUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #dc3545;'>Password Reset Request</h2>" +
                "<p>Hello " + userName + ",</p>" +
                "<p>We received a request to reset your password. Click the button below to reset it:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + resetUrl + "' style='background-color: #dc3545; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Reset Password</a>" +
                "</div>" +
                "<p>Or copy and paste this link into your browser:</p>" +
                "<p style='word-break: break-all; color: #666;'>" + resetUrl + "</p>" +
                "<p>This link will expire in 1 hour.</p>" +
                "<p>If you didn't request a password reset, please ignore this email. Your password will remain unchanged.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #666; font-size: 12px;'>KindCircle - Making a difference, one act of kindness at a time.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
