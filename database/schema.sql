-- KindCircle Database Schema
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS kindcircle;
USE kindcircle;

-- Users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    phone_verified BOOLEAN DEFAULT FALSE,
    full_name VARCHAR(100) NOT NULL,
    blood_group VARCHAR(5),
    city VARCHAR(100),
    location VARCHAR(255),
    profile_photo_url TEXT,
    current_impact_score INT DEFAULT 0,
    total_blood_donations INT DEFAULT 0,
    total_social_work INT DEFAULT 0,
    upvotes_received INT DEFAULT 0,
    downvotes_received INT DEFAULT 0,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_blood_group (blood_group),
    INDEX idx_city (city),
    INDEX idx_impact_score (current_impact_score)
);

-- Impact Score Ledger (Transaction History)
CREATE TABLE impact_score_ledger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    transaction_type ENUM('EARNED', 'SPENT', 'ADJUSTED') NOT NULL,
    activity_type ENUM('BLOOD_DONATION', 'FEEDING', 'WINTER_CLOTHES', 'TREE_PLANTATION', 'BLOOD_REQUEST', 'ADMIN_ADJUSTMENT') NOT NULL,
    points INT NOT NULL,
    description TEXT,
    reference_id BIGINT, -- Links to blood_donations, social_work_posts, etc.
    reference_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

-- Social Work Posts
CREATE TABLE social_work_posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    activity_type ENUM('BLOOD_DONATION', 'FEEDING', 'WINTER_CLOTHES', 'TREE_PLANTATION') NOT NULL,
    title VARCHAR(200),
    description TEXT NOT NULL,
    location VARCHAR(255),
    activity_date DATE NOT NULL,
    impact_score_earned INT DEFAULT 0,
    verification_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    verified_by BIGINT, -- Admin user ID
    verified_at TIMESTAMP NULL,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_verification_status (verification_status),
    INDEX idx_created_at (created_at)
);

-- Media Proofs (Photos/Videos)
CREATE TABLE media_proofs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    media_type ENUM('PHOTO', 'VIDEO') NOT NULL,
    media_url TEXT NOT NULL,
    thumbnail_url TEXT,
    file_size BIGINT,
    mime_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES social_work_posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id)
);

-- Blood Requests (Emergency Feed)
CREATE TABLE blood_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    requester_id BIGINT NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    bags_required INT NOT NULL DEFAULT 1,
    hospital_name VARCHAR(200) NOT NULL,
    hospital_address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    urgency_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    description TEXT,
    status ENUM('OPEN', 'MATCHED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'OPEN',
    impact_score_required INT NOT NULL, -- Must be >= 100 * bags_required
    matched_donor_id BIGINT,
    matched_at TIMESTAMP NULL,
    location_sharing_enabled BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (matched_donor_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_blood_group (blood_group),
    INDEX idx_status (status),
    INDEX idx_city (city),
    INDEX idx_created_at (created_at)
);

-- Blood Donations (Completed)
CREATE TABLE blood_donations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT,
    donor_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    donation_date DATE NOT NULL,
    hospital_name VARCHAR(200) NOT NULL,
    impact_score_donor INT DEFAULT 100, -- Donor gains
    impact_score_requester INT DEFAULT -100, -- Requester loses
    status ENUM('PENDING_CONFIRMATION', 'CONFIRMED', 'COMPLETED') DEFAULT 'PENDING_CONFIRMATION',
    donor_confirmed BOOLEAN DEFAULT FALSE,
    requester_confirmed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES blood_requests(id) ON DELETE SET NULL,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_donor_id (donor_id),
    INDEX idx_requester_id (requester_id),
    INDEX idx_status (status)
);

-- Comments on Posts
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id BIGINT, -- For nested comments
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES social_work_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
);

-- Votes (Upvotes/Downvotes)
CREATE TABLE votes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    vote_type ENUM('UPVOTE', 'DOWNVOTE') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_post_vote (user_id, post_id),
    FOREIGN KEY (post_id) REFERENCES social_work_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
);

-- Awareness Articles (Admin Only)
CREATE TABLE awareness_articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    category VARCHAR(50), -- e.g., 'DENGUE', 'WINTER', 'RAMADAN', 'HEATSTROKE'
    is_published BOOLEAN DEFAULT FALSE,
    views_count INT DEFAULT 0,
    shares_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_category (category),
    INDEX idx_is_published (is_published)
);

-- Chats (Private Messaging)
CREATE TABLE chats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    related_request_id BIGINT, -- Links to blood_requests if chat is about an emergency
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (related_request_id) REFERENCES blood_requests(id) ON DELETE SET NULL,
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_related_request_id (related_request_id),
    INDEX idx_created_at (created_at)
);

-- Location Sessions (Real-time Location Sharing)
CREATE TABLE location_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    donor_location_lat DECIMAL(10, 8),
    donor_location_lng DECIMAL(11, 8),
    requester_location_lat DECIMAL(10, 8),
    requester_location_lng DECIMAL(11, 8),
    is_active BOOLEAN DEFAULT FALSE,
    started_at TIMESTAMP NULL,
    ended_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES blood_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_request_id (request_id),
    INDEX idx_is_active (is_active)
);

-- Reports (Abuse/Flag System)
CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT,
    reported_post_id BIGINT,
    report_type ENUM('ABUSE', 'FAKE', 'SPAM', 'INAPPROPRIATE', 'OTHER') NOT NULL,
    description TEXT,
    status ENUM('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED') DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_post_id) REFERENCES social_work_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_status (status)
);

-- Daily Blood Request Limit Tracking
CREATE TABLE daily_blood_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    request_date DATE NOT NULL,
    requests_count INT DEFAULT 1,
    UNIQUE KEY unique_user_date (user_id, request_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, request_date)
);

-- Insert default admin user (password: password - should be changed in production)
-- Password hash for 'password' using BCrypt
INSERT INTO users (username, email, password_hash, phone_number, full_name, role, phone_verified, current_impact_score)
VALUES ('admin', 'admin@kindcircle.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+1234567890', 'System Admin', 'ADMIN', TRUE, 0);
