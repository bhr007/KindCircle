-- Update media_url column to TEXT to support base64 images
-- Run this in phpMyAdmin or MySQL command line

ALTER TABLE media_proofs MODIFY COLUMN media_url TEXT NOT NULL;
ALTER TABLE media_proofs MODIFY COLUMN thumbnail_url TEXT;
