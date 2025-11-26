-- Comprehensive Test Data for Microtasking Platform
-- This populates the database with realistic data for all features

USE microtask_tool;

-- Clear existing test data (keep admin users)
DELETE FROM activity_log;
DELETE FROM quality_flags;
DELETE FROM worker_performance;
DELETE FROM consensus_result;
DELETE FROM payment_export;
DELETE FROM answer;
DELETE FROM task;
DELETE FROM image;
DELETE FROM question WHERE id >= 1;

-- Insert test questions
INSERT INTO question (id, created_at, updated_at, is_paused, name) VALUES
(1, NOW(), NOW(), 0, 'Land Classification - Kenya'),
(2, NOW(), NOW(), 0, 'Building Detection - Nairobi'),
(3, NOW(), NOW(), 1, 'Road Quality Assessment'),
(4, NOW(), NOW(), 0, 'Agricultural Land Use'),
(5, NOW(), NOW(), 0, 'Water Body Identification');

-- Insert test images for each question (10 images per question)
INSERT INTO image (created_at, url, question_id) VALUES
-- Question 1 images
('2025-11-01 10:00:00', 'https://storage.spatialcollective.com/images/land1.jpg', 1),
('2025-11-01 10:05:00', 'https://storage.spatialcollective.com/images/land2.jpg', 1),
('2025-11-01 10:10:00', 'https://storage.spatialcollective.com/images/land3.jpg', 1),
('2025-11-01 10:15:00', 'https://storage.spatialcollective.com/images/land4.jpg', 1),
('2025-11-01 10:20:00', 'https://storage.spatialcollective.com/images/land5.jpg', 1),
('2025-11-01 10:25:00', 'https://storage.spatialcollective.com/images/land6.jpg', 1),
('2025-11-01 10:30:00', 'https://storage.spatialcollective.com/images/land7.jpg', 1),
('2025-11-01 10:35:00', 'https://storage.spatialcollective.com/images/land8.jpg', 1),
('2025-11-01 10:40:00', 'https://storage.spatialcollective.com/images/land9.jpg', 1),
('2025-11-01 10:45:00', 'https://storage.spatialcollective.com/images/land10.jpg', 1),
-- Question 2 images
('2025-11-05 09:00:00', 'https://storage.spatialcollective.com/images/building1.jpg', 2),
('2025-11-05 09:05:00', 'https://storage.spatialcollective.com/images/building2.jpg', 2),
('2025-11-05 09:10:00', 'https://storage.spatialcollective.com/images/building3.jpg', 2),
('2025-11-05 09:15:00', 'https://storage.spatialcollective.com/images/building4.jpg', 2),
('2025-11-05 09:20:00', 'https://storage.spatialcollective.com/images/building5.jpg', 2),
('2025-11-05 09:25:00', 'https://storage.spatialcollective.com/images/building6.jpg', 2),
('2025-11-05 09:30:00', 'https://storage.spatialcollective.com/images/building7.jpg', 2),
('2025-11-05 09:35:00', 'https://storage.spatialcollective.com/images/building8.jpg', 2),
('2025-11-05 09:40:00', 'https://storage.spatialcollective.com/images/building9.jpg', 2),
('2025-11-05 09:45:00', 'https://storage.spatialcollective.com/images/building10.jpg', 2),
-- Question 4 images  
('2025-11-10 08:00:00', 'https://storage.spatialcollective.com/images/agri1.jpg', 4),
('2025-11-10 08:05:00', 'https://storage.spatialcollective.com/images/agri2.jpg', 4),
('2025-11-10 08:10:00', 'https://storage.spatialcollective.com/images/agri3.jpg', 4),
('2025-11-10 08:15:00', 'https://storage.spatialcollective.com/images/agri4.jpg', 4),
('2025-11-10 08:20:00', 'https://storage.spatialcollective.com/images/agri5.jpg', 4),
-- Question 5 images
('2025-11-15 07:00:00', 'https://storage.spatialcollective.com/images/water1.jpg', 5),
('2025-11-15 07:05:00', 'https://storage.spatialcollective.com/images/water2.jpg', 5),
('2025-11-15 07:10:00', 'https://storage.spatialcollective.com/images/water3.jpg', 5),
('2025-11-15 07:15:00', 'https://storage.spatialcollective.com/images/water4.jpg', 5),
('2025-11-15 07:20:00', 'https://storage.spatialcollective.com/images/water5.jpg', 5);

-- Insert test workers as tasks (15 workers)
INSERT INTO task (created_at, updated_at, phone_number, progress, start_date, worker_unique_id, question_id) VALUES
('2025-11-01 09:00:00', '2025-11-25 18:00:00', '+254712345678', 100, '2025-11-01 09:00:00', '254712345678', 1),
('2025-11-01 09:05:00', '2025-11-25 17:45:00', '+254723456789', 85, '2025-11-01 09:05:00', '254723456789', 1),
('2025-11-01 09:10:00', '2025-11-25 17:30:00', '+254734567890', 90, '2025-11-01 09:10:00', '254734567890', 1),
('2025-11-01 09:15:00', '2025-11-25 17:00:00', '+254745678901', 75, '2025-11-01 09:15:00', '254745678901', 1),
('2025-11-01 09:20:00', '2025-11-25 16:30:00', '+254756789012', 95, '2025-11-01 09:20:00', '254756789012', 1),
('2025-11-01 09:25:00', '2025-11-25 16:00:00', '+254767890123', 60, '2025-11-01 09:25:00', '254767890123', 1),
('2025-11-01 09:30:00', '2025-11-25 15:30:00', '+254778901234', 88, '2025-11-01 09:30:00', '254778901234', 1),
('2025-11-01 09:35:00', '2025-11-25 15:00:00', '+254789012345', 92, '2025-11-01 09:35:00', '254789012345', 1),
('2025-11-05 08:00:00', '2025-11-25 14:30:00', '+254790123456', 80, '2025-11-05 08:00:00', '254790123456', 2),
('2025-11-05 08:05:00', '2025-11-25 14:00:00', '+254701234567', 70, '2025-11-05 08:05:00', '254701234567', 2),
('2025-11-10 07:00:00', '2025-11-25 13:30:00', '+254712340000', 65, '2025-11-10 07:00:00', '254712340000', 4),
('2025-11-10 07:05:00', '2025-11-25 13:00:00', '+254723450001', 78, '2025-11-10 07:05:00', '254723450001', 4),
('2025-11-15 06:00:00', '2025-11-25 12:30:00', '+254734560002', 83, '2025-11-15 06:00:00', '254734560002', 5),
('2025-11-15 06:05:00', '2025-11-25 12:00:00', '+254745670003', 91, '2025-11-15 06:05:00', '254745670003', 5),
('2025-11-01 10:00:00', '2025-11-25 11:30:00', '+254756780004', 55, '2025-11-01 10:00:00', '254756780004', 1);

-- Insert realistic answers (multiple workers answering multiple images)
-- Question 1 answers (forest, agriculture, urban, water classification)
INSERT INTO answer (created_at, updated_at, answer, image_id, url, worker_unique_id, question_id) VALUES
-- Image 1 (consensus: forest)
('2025-11-01 10:00:00', '2025-11-01 10:00:00', 'forest', 1, 'https://storage.spatialcollective.com/images/land1.jpg', '254712345678', 1),
('2025-11-01 10:01:00', '2025-11-01 10:01:00', 'forest', 1, 'https://storage.spatialcollective.com/images/land1.jpg', '254723456789', 1),
('2025-11-01 10:02:00', '2025-11-01 10:02:00', 'forest', 1, 'https://storage.spatialcollective.com/images/land1.jpg', '254734567890', 1),
('2025-11-01 10:03:00', '2025-11-01 10:03:00', 'agriculture', 1, 'https://storage.spatialcollective.com/images/land1.jpg', '254745678901', 1),
('2025-11-01 10:04:00', '2025-11-01 10:04:00', 'forest', 1, 'https://storage.spatialcollective.com/images/land1.jpg', '254756789012', 1),
-- Image 2 (consensus: agriculture)
('2025-11-01 11:00:00', '2025-11-01 11:00:00', 'agriculture', 2, 'https://storage.spatialcollective.com/images/land2.jpg', '254712345678', 1),
('2025-11-01 11:01:00', '2025-11-01 11:01:00', 'agriculture', 2, 'https://storage.spatialcollective.com/images/land2.jpg', '254723456789', 1),
('2025-11-01 11:02:00', '2025-11-01 11:02:00', 'agriculture', 2, 'https://storage.spatialcollective.com/images/land2.jpg', '254734567890', 1),
('2025-11-01 11:03:00', '2025-11-01 11:03:00', 'agriculture', 2, 'https://storage.spatialcollective.com/images/land2.jpg', '254756789012', 1),
('2025-11-01 11:04:00', '2025-11-01 11:04:00', 'forest', 2, 'https://storage.spatialcollective.com/images/land2.jpg', '254767890123', 1),
-- Image 3 (consensus: urban)
('2025-11-01 12:00:00', '2025-11-01 12:00:00', 'urban', 3, 'https://storage.spatialcollective.com/images/land3.jpg', '254712345678', 1),
('2025-11-01 12:01:00', '2025-11-01 12:01:00', 'urban', 3, 'https://storage.spatialcollective.com/images/land3.jpg', '254723456789', 1),
('2025-11-01 12:02:00', '2025-11-01 12:02:00', 'urban', 3, 'https://storage.spatialcollective.com/images/land3.jpg', '254778901234', 1),
('2025-11-01 12:03:00', '2025-11-01 12:03:00', 'urban', 3, 'https://storage.spatialcollective.com/images/land3.jpg', '254789012345', 1),
('2025-11-01 12:04:00', '2025-11-01 12:04:00', 'agriculture', 3, 'https://storage.spatialcollective.com/images/land3.jpg', '254756780004', 1),
-- Image 4 (consensus: water)
('2025-11-01 13:00:00', '2025-11-01 13:00:00', 'water', 4, 'https://storage.spatialcollective.com/images/land4.jpg', '254712345678', 1),
('2025-11-01 13:01:00', '2025-11-01 13:01:00', 'water', 4, 'https://storage.spatialcollective.com/images/land4.jpg', '254734567890', 1),
('2025-11-01 13:02:00', '2025-11-01 13:02:00', 'water', 4, 'https://storage.spatialcollective.com/images/land4.jpg', '254756789012', 1),
('2025-11-01 13:03:00', '2025-11-01 13:03:00', 'urban', 4, 'https://storage.spatialcollective.com/images/land4.jpg', '254767890123', 1),
-- Image 5 (consensus: forest)
('2025-11-01 14:00:00', '2025-11-01 14:00:00', 'forest', 5, 'https://storage.spatialcollective.com/images/land5.jpg', '254712345678', 1),
('2025-11-01 14:01:00', '2025-11-01 14:01:00', 'forest', 5, 'https://storage.spatialcollective.com/images/land5.jpg', '254723456789', 1),
('2025-11-01 14:02:00', '2025-11-01 14:02:00', 'forest', 5, 'https://storage.spatialcollective.com/images/land5.jpg', '254734567890', 1),
('2025-11-01 14:03:00', '2025-11-01 14:03:00', 'forest', 5, 'https://storage.spatialcollective.com/images/land5.jpg', '254745678901', 1),
('2025-11-01 14:04:00', '2025-11-01 14:04:00', 'agriculture', 5, 'https://storage.spatialcollective.com/images/land5.jpg', '254756780004', 1);

-- Insert consensus results
INSERT INTO consensus_result (question_id, image_id, ground_truth, total_responses, consensus_percentage, requires_review, review_status, created_at, updated_at) VALUES
(1, 1, 'forest', 5, 80.00, FALSE, 'approved', NOW(), NOW()),
(1, 2, 'agriculture', 5, 80.00, FALSE, 'approved', NOW(), NOW()),
(1, 3, 'urban', 5, 80.00, FALSE, 'approved', NOW(), NOW()),
(1, 4, 'water', 4, 75.00, FALSE, 'approved', NOW(), NOW()),
(1, 5, 'forest', 5, 80.00, FALSE, 'approved', NOW(), NOW());

-- Insert worker performance data (last 30 days)
INSERT INTO worker_performance (worker_unique_id, question_id, date, tasks_completed, correct_answers, incorrect_answers, consensus_score, average_time_per_task, flagged_tasks, quality_tier, base_pay, bonus_amount, total_payment, payment_status, created_at, updated_at) VALUES
-- Excellent performers
('254712345678', 1, '2025-11-25', 50, 47, 3, 94.00, 45.5, 0, 'excellent', 760.00, 228.00, 988.00, 'pending', NOW(), NOW()),
('254712345678', 1, '2025-11-24', 48, 46, 2, 95.83, 44.2, 0, 'excellent', 760.00, 228.00, 988.00, 'pending', NOW(), NOW()),
('254712345678', 1, '2025-11-23', 52, 49, 3, 94.23, 46.1, 0, 'excellent', 760.00, 228.00, 988.00, 'pending', NOW(), NOW()),
('254734567890', 1, '2025-11-25', 45, 42, 3, 93.33, 47.3, 0, 'excellent', 760.00, 228.00, 988.00, 'pending', NOW(), NOW()),
('254734567890', 1, '2025-11-24', 46, 44, 2, 95.65, 46.8, 0, 'excellent', 760.00, 228.00, 988.00, 'pending', NOW(), NOW()),
('254756789012', 1, '2025-11-25', 48, 45, 3, 93.75, 43.9, 0, 'excellent', 760.00, 228.00, 988.00, 'pending', NOW(), NOW()),
('254789012345', 1, '2025-11-25', 44, 42, 2, 95.45, 44.5, 0, 'excellent', 760.00, 228.00, 988.00, 'pending', NOW(), NOW()),
-- Good performers
('254723456789', 1, '2025-11-25', 42, 36, 6, 85.71, 52.1, 1, 'good', 760.00, 152.00, 912.00, 'pending', NOW(), NOW()),
('254723456789', 1, '2025-11-24', 40, 34, 6, 85.00, 53.2, 1, 'good', 760.00, 152.00, 912.00, 'pending', NOW(), NOW()),
('254778901234', 1, '2025-11-25', 38, 32, 6, 84.21, 54.3, 1, 'good', 760.00, 152.00, 912.00, 'pending', NOW(), NOW()),
('254790123456', 2, '2025-11-25', 35, 29, 6, 82.86, 56.7, 2, 'good', 760.00, 152.00, 912.00, 'pending', NOW(), NOW()),
-- Fair performers
('254745678901', 1, '2025-11-25', 36, 27, 9, 75.00, 62.4, 2, 'fair', 760.00, 76.00, 836.00, 'pending', NOW(), NOW()),
('254745678901', 1, '2025-11-24', 34, 25, 9, 73.53, 63.1, 2, 'fair', 760.00, 76.00, 836.00, 'pending', NOW(), NOW()),
('254701234567', 2, '2025-11-25', 32, 23, 9, 71.88, 64.8, 3, 'fair', 760.00, 76.00, 836.00, 'pending', NOW(), NOW()),
-- Poor performers
('254767890123', 1, '2025-11-25', 28, 15, 13, 53.57, 78.5, 4, 'poor', 760.00, 0.00, 760.00, 'pending', NOW(), NOW()),
('254767890123', 1, '2025-11-24', 26, 14, 12, 53.85, 79.2, 4, 'poor', 760.00, 0.00, 760.00, 'pending', NOW(), NOW()),
('254767890123', 1, '2025-11-23', 24, 12, 12, 50.00, 81.3, 5, 'poor', 760.00, 0.00, 760.00, 'pending', NOW(), NOW()),
('254756780004', 1, '2025-11-25', 22, 11, 11, 50.00, 85.4, 5, 'poor', 760.00, 0.00, 760.00, 'pending', NOW(), NOW()),
('254712340000', 4, '2025-11-25', 20, 10, 10, 50.00, 88.2, 6, 'poor', 760.00, 0.00, 760.00, 'pending', NOW(), NOW());

-- Insert quality flags
INSERT INTO quality_flags (worker_unique_id, question_id, flag_type, severity, description, flagged_at, resolved, resolved_by, resolved_at, resolution_notes) VALUES
('254767890123', 1, 'low_performance', 'high', 'Consensus score below 60% for 3 consecutive days', '2025-11-23 10:00:00', FALSE, NULL, NULL, NULL),
('254756780004', 1, 'low_performance', 'high', 'Consensus score consistently below 55%', '2025-11-24 09:00:00', FALSE, NULL, NULL, NULL),
('254712340000', 4, 'anomalous_speed', 'medium', 'Completing tasks too quickly (avg 20 sec vs expected 45 sec)', '2025-11-25 08:00:00', FALSE, NULL, NULL, NULL),
('254701234567', 2, 'low_performance', 'medium', 'Consensus score dropped to 72% from 85%', '2025-11-25 11:00:00', FALSE, NULL, NULL, NULL),
('254745678901', 1, 'manual', 'low', 'Inconsistent classifications on boundary cases', '2025-11-22 14:00:00', TRUE, 'admin@spatialcollective.com', '2025-11-23 10:00:00', 'Provided additional training on classification criteria');

-- Insert payment exports
INSERT INTO payment_export (export_date, period_start, period_end, total_workers, total_amount, export_format, file_path, exported_by, exported_at, dpw_sync_status, dpw_sync_at, dpw_reference) VALUES
('2025-11-20', '2025-11-01', '2025-11-15', 12, 10944.00, 'xlsx', '/exports/payments_2025-11-01_2025-11-15.xlsx', 'admin@spatialcollective.com', '2025-11-20 16:00:00', 'completed', '2025-11-21 09:00:00', 'DPW-2025-11-001'),
('2025-11-10', '2025-10-16', '2025-10-31', 10, 8760.00, 'xlsx', '/exports/payments_2025-10-16_2025-10-31.xlsx', 'admin@spatialcollective.com', '2025-11-10 15:30:00', 'completed', '2025-11-11 10:00:00', 'DPW-2025-10-002');

-- Insert activity logs
INSERT INTO activity_log (timestamp, user_id, worker_unique_id, action, question_id, metadata, ip_address, user_agent) VALUES
('2025-11-25 09:00:00', 'admin-001', NULL, 'QUESTION_CREATED', 1, '{"question_name":"Land Classification - Kenya"}', '102.210.149.40', 'Mozilla/5.0'),
('2025-11-25 09:30:00', 'admin-001', NULL, 'IMAGES_UPLOADED', 1, '{"image_count":10}', '102.210.149.40', 'Mozilla/5.0'),
('2025-11-25 10:00:00', NULL, '254712345678', 'TASK_STARTED', 1, '{"phone_number":"+254712345678"}', '41.90.181.23', 'Mozilla/5.0 (Linux; Android)'),
('2025-11-25 10:05:00', NULL, '254712345678', 'ANSWER_SUBMITTED', 1, '{"image_id":1,"answer":"forest"}', '41.90.181.23', 'Mozilla/5.0 (Linux; Android)'),
('2025-11-25 14:00:00', 'admin-001', NULL, 'PAYMENT_CALCULATED', 1, '{"total_workers":8,"total_amount":7296.00}', '102.210.149.40', 'Mozilla/5.0'),
('2025-11-25 15:00:00', 'admin-001', NULL, 'PAYMENT_APPROVED', 1, '{"approved_count":8,"total_amount":7296.00}', '102.210.149.40', 'Mozilla/5.0'),
('2025-11-25 16:00:00', 'support-001', '254767890123', 'QUALITY_FLAG_CREATED', 1, '{"flag_type":"low_performance","severity":"high"}', '102.210.149.40', 'Mozilla/5.0');

-- Insert system health metrics
INSERT INTO system_health (timestamp, metric_type, metric_value, status, alert_sent, alert_recipients) VALUES
('2025-11-25 09:00:00', 'cpu_usage', 45.2, 'normal', FALSE, NULL),
('2025-11-25 09:05:00', 'memory_usage', 62.8, 'normal', FALSE, NULL),
('2025-11-25 09:10:00', 'disk_usage', 58.3, 'normal', FALSE, NULL),
('2025-11-25 09:15:00', 'cpu_usage', 73.5, 'warning', TRUE, 'admin@spatialcollective.com,tech@spatialcollective.com'),
('2025-11-25 09:20:00', 'cpu_usage', 52.1, 'normal', FALSE, NULL),
('2025-11-25 09:25:00', 'memory_usage', 84.2, 'warning', TRUE, 'admin@spatialcollective.com,tech@spatialcollective.com');

-- Summary
SELECT 'Database seeded successfully!' AS status;
SELECT COUNT(*) AS question_count FROM question;
SELECT COUNT(*) AS image_count FROM image;
SELECT COUNT(*) AS worker_count FROM task;
SELECT COUNT(*) AS answer_count FROM answer;
SELECT COUNT(*) AS consensus_count FROM consensus_result;
SELECT COUNT(*) AS performance_records FROM worker_performance;
SELECT COUNT(*) AS quality_flags FROM quality_flags;
SELECT COUNT(*) AS payment_exports FROM payment_export;
SELECT COUNT(*) AS activity_logs FROM activity_log;
SELECT COUNT(*) AS health_metrics FROM system_health;
