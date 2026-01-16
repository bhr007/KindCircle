-- Add last_donation_date field to users table
USE kindcircle;

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS last_donation_date DATE NULL;
