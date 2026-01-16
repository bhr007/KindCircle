-- Add email verification fields to users table
ALTER TABLE users 
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE AFTER phone_verified,
ADD COLUMN email_verification_token VARCHAR(255) AFTER email_verified,
ADD COLUMN email_verification_token_expiry TIMESTAMP NULL AFTER email_verification_token,
ADD COLUMN password_reset_token VARCHAR(255) NULL AFTER email_verification_token_expiry,
ADD COLUMN password_reset_token_expiry TIMESTAMP NULL AFTER password_reset_token;

-- Create email verification tokens table (optional - can use users table fields instead)
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    token_type ENUM('EMAIL_VERIFICATION', 'PASSWORD_RESET') NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_token (token),
    INDEX idx_expires_at (expires_at)
);
