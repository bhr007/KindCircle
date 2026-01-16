-- Add is_archived and is_deleted fields to chats table
USE kindcircle;

ALTER TABLE chats
ADD COLUMN is_archived BOOLEAN DEFAULT FALSE,
ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE,
ADD COLUMN archived_by_user_id BIGINT,
ADD INDEX idx_archived (is_archived),
ADD INDEX idx_deleted (is_deleted);
