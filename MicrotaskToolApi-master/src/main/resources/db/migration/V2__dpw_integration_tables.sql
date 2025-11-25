-- DPW Integration Database Tables
-- Migration Version 2
-- Date: 2025-11-25

-- Table 1: Consensus Results
-- Stores the ground truth (majority vote) for each image in a question
CREATE TABLE IF NOT EXISTS consensus_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    image_id BIGINT NOT NULL,
    ground_truth VARCHAR(255),
    total_responses INT,
    consensus_percentage DECIMAL(5,2),
    requires_review BOOLEAN DEFAULT FALSE,
    review_status VARCHAR(50) DEFAULT 'pending',
    reviewed_by VARCHAR(255),
    reviewed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    INDEX idx_question_image (question_id, image_id),
    INDEX idx_review (requires_review, review_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 2: Worker Performance
-- Tracks daily performance metrics, consensus scores, and payments for each worker
CREATE TABLE IF NOT EXISTS worker_performance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_unique_id VARCHAR(30) NOT NULL,
    question_id BIGINT NOT NULL,
    date DATE NOT NULL,
    tasks_completed INT DEFAULT 0,
    correct_answers INT DEFAULT 0,
    incorrect_answers INT DEFAULT 0,
    consensus_score DECIMAL(5,2),
    average_time_per_task DECIMAL(10,2),
    flagged_tasks INT DEFAULT 0,
    quality_tier VARCHAR(20),
    base_pay DECIMAL(10,2),
    bonus_amount DECIMAL(10,2),
    total_payment DECIMAL(10,2),
    payment_status VARCHAR(50) DEFAULT 'pending',
    payment_reference VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    UNIQUE KEY unique_worker_daily (worker_unique_id, question_id, date),
    INDEX idx_worker (worker_unique_id),
    INDEX idx_date (date),
    INDEX idx_payment (payment_status, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 3: Quality Flags
-- Records quality issues for workers (low consensus, high speed, etc.)
CREATE TABLE IF NOT EXISTS quality_flags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_unique_id VARCHAR(30) NOT NULL,
    question_id BIGINT NOT NULL,
    flag_type VARCHAR(50),
    severity VARCHAR(20),
    description TEXT,
    flagged_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_by VARCHAR(255),
    resolved_at DATETIME,
    resolution_notes TEXT,
    INDEX idx_worker (worker_unique_id),
    INDEX idx_resolved (resolved),
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 4: System Health
-- Stores server health metrics (CPU, memory, disk, response time, error rate)
CREATE TABLE IF NOT EXISTS system_health (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    metric_type VARCHAR(50),
    metric_value DECIMAL(10,2),
    status VARCHAR(20),
    alert_sent BOOLEAN DEFAULT FALSE,
    alert_recipients TEXT,
    INDEX idx_timestamp (timestamp),
    INDEX idx_status (status, alert_sent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 5: Activity Log
-- Comprehensive audit trail for all user/worker actions
CREATE TABLE IF NOT EXISTS activity_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(255),
    worker_unique_id VARCHAR(30),
    action VARCHAR(100),
    question_id BIGINT,
    metadata JSON,
    ip_address VARCHAR(50),
    user_agent TEXT,
    INDEX idx_timestamp (timestamp),
    INDEX idx_user (user_id),
    INDEX idx_worker (worker_unique_id),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 6: Payment Export
-- Tracks batch payment exports and sync status with DPW App
CREATE TABLE IF NOT EXISTS payment_export (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    export_date DATE NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_workers INT,
    total_amount DECIMAL(12,2),
    export_format VARCHAR(20),
    file_path VARCHAR(512),
    exported_by VARCHAR(255),
    exported_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    dpw_sync_status VARCHAR(50) DEFAULT 'pending',
    dpw_sync_at DATETIME,
    dpw_reference VARCHAR(255),
    INDEX idx_export_date (export_date),
    INDEX idx_sync_status (dpw_sync_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
