-- ============================================================================
-- MICROTASKING DATABASE SETUP SCRIPT
-- Database: spatialcoke_microtask
-- Host: 169.255.58.54 (spatialcollective.co.ke)
-- Date: November 21, 2025
-- ============================================================================

-- Use the database
USE spatialcoke_microtask;

-- Drop existing tables if they exist (in correct order due to foreign keys)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS answer;
DROP TABLE IF EXISTS image;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS user;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- TABLE 1: user
-- Purpose: Stores admin users (Google OAuth authenticated)
-- ============================================================================
CREATE TABLE user (
    id VARCHAR(255) NOT NULL PRIMARY KEY COMMENT 'Google user ID from JWT',
    full_name VARCHAR(255) DEFAULT NULL COMMENT 'User full name from Google',
    email VARCHAR(255) DEFAULT NULL COMMENT 'User email address',
    picture VARCHAR(512) DEFAULT NULL COMMENT 'Profile picture URL',
    role VARCHAR(50) DEFAULT 'Worker' COMMENT 'User role: Admin or Worker'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Admin users authenticated via Google OAuth';

-- ============================================================================
-- TABLE 2: question
-- Purpose: Stores microtask questions/projects
-- ============================================================================
CREATE TABLE question (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(500) NOT NULL COMMENT 'Question text or title',
    is_paused TINYINT(1) DEFAULT 0 COMMENT 'Whether task is paused (0=active, 1=paused)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When question was created',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    INDEX idx_created_at (created_at),
    INDEX idx_paused (is_paused)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Microtask questions/projects';

-- ============================================================================
-- TABLE 3: task
-- Purpose: Worker assignments to questions (tracks progress)
-- ============================================================================
CREATE TABLE task (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    worker_unique_id VARCHAR(30) NOT NULL COMMENT 'Unique worker identifier from CSV',
    phone_number VARCHAR(20) NOT NULL COMMENT 'Worker phone number (login credential)',
    start_date TIMESTAMP NULL DEFAULT NULL COMMENT 'When worker first started working',
    progress INT DEFAULT 0 COMMENT 'Number of images answered',
    question_id BIGINT NOT NULL COMMENT 'Reference to question',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    INDEX idx_question_id (question_id),
    INDEX idx_phone_number (phone_number),
    INDEX idx_worker_unique_id (worker_unique_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Worker assignments and progress tracking';

-- ============================================================================
-- TABLE 4: image
-- Purpose: Stores image URLs to be annotated
-- ============================================================================
CREATE TABLE image (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    url TEXT NOT NULL COMMENT 'Full URL to image',
    created_at VARCHAR(50) NOT NULL COMMENT 'Upload batch date/time identifier',
    question_id BIGINT NOT NULL COMMENT 'Reference to question',
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    INDEX idx_question_id (question_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Images to be annotated by workers';

-- ============================================================================
-- TABLE 5: answer
-- Purpose: Stores worker responses/annotations
-- ============================================================================
CREATE TABLE answer (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    image_id BIGINT NOT NULL COMMENT 'Reference to image answered',
    url VARCHAR(1024) NOT NULL COMMENT 'Copy of image URL for reporting',
    worker_unique_id VARCHAR(255) NOT NULL COMMENT 'Who provided the answer',
    answer VARCHAR(255) NOT NULL COMMENT 'The answer (YES/NO or other value)',
    question_id BIGINT NOT NULL COMMENT 'Reference to question',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When answer was submitted',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    UNIQUE INDEX answered_index (worker_unique_id, image_id, question_id) COMMENT 'Prevent duplicate answers',
    INDEX question_index (question_id),
    INDEX done_index (worker_unique_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Worker answers/annotations';

-- ============================================================================
-- INSERT INITIAL ADMIN USER
-- Replace with actual Google account details after first login
-- ============================================================================
-- INSERT INTO user (id, full_name, email, picture, role) 
-- VALUES ('your-google-user-id', 'Admin Name', 'admin@spatialcollective.co.ke', '', 'Admin');

-- ============================================================================
-- VERIFY TABLE CREATION
-- ============================================================================
SHOW TABLES;

-- Show table structures
DESCRIBE user;
DESCRIBE question;
DESCRIBE task;
DESCRIBE image;
DESCRIBE answer;

-- ============================================================================
-- USEFUL QUERIES FOR MONITORING
-- ============================================================================

-- Count records in each table
SELECT 'user' AS table_name, COUNT(*) AS record_count FROM user
UNION ALL
SELECT 'question', COUNT(*) FROM question
UNION ALL
SELECT 'task', COUNT(*) FROM task
UNION ALL
SELECT 'image', COUNT(*) FROM image
UNION ALL
SELECT 'answer', COUNT(*) FROM answer;

-- Check database size
SELECT 
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES 
WHERE table_schema = 'spatialcoke_microtask'
ORDER BY (data_length + index_length) DESC;

-- ============================================================================
-- NOTES:
-- 1. Run this script using MySQL client or phpMyAdmin
-- 2. After running, test connection from Spring Boot application
-- 3. Create first admin user through Google OAuth login
-- 4. Hibernate ddl-auto=update will maintain schema going forward
-- ============================================================================
