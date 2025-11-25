-- Add tech@spatialcollective.com as Admin User
-- Database: bgprxgmy_microtask

USE bgprxgmy_microtask;

-- Check if tables exist
SHOW TABLES;

-- Insert admin user (or update if exists)
-- Using email as unique identifier
INSERT INTO user (id, full_name, email, picture, role)
VALUES (
    'tech@spatialcollective.com',
    'Tech',
    'tech@spatialcollective.com',
    '',
    'Admin'
)
ON DUPLICATE KEY UPDATE
    full_name = 'Tech',
    role = 'Admin';

-- Verify the user was created
SELECT * FROM user WHERE email = 'tech@spatialcollective.com';
