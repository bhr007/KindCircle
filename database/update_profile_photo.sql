-- Update profile_photo_url column to TEXT to support base64 images
-- Run this in phpMyAdmin or MySQL command line

ALTER TABLE users MODIFY COLUMN profile_photo_url TEXT;
