# DPW App Integration Technical Specification
## Microtasking Platform Management Module

**Version:** 1.0  
**Date:** November 25, 2025  
**Platform:** micro.spatialcollective.co.ke  
**Integration Partner:** DPW App  

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current Architecture Analysis](#current-architecture-analysis)
3. [Required Enhancements for DPW Integration](#required-enhancements)
4. [API Specifications](#api-specifications)
5. [Consensus & Quality Assurance Implementation](#consensus-implementation)
6. [Payment Processing Integration](#payment-processing)
7. [Performance Monitoring & Analytics](#performance-monitoring)
8. [Server Health Monitoring & Alerts](#server-monitoring)
9. [Logging & Audit Trail](#logging-audit)
10. [Security Considerations](#security)
11. [Implementation Roadmap](#implementation-roadmap)
12. [Appendices](#appendices)

---

## 1. Executive Summary

### Integration Objectives

The DPW App integration aims to transform the Microtasking platform into a comprehensive workforce management system with:

1. **Automated Quality Control** - Consensus-based validation with 60-80% agreement thresholds
2. **Performance-Based Payments** - Tiered bonus structure tied to daily consensus scores
3. **Real-Time Monitoring** - Server health checks, worker activity tracking, and automated alerts
4. **Data Export & Reporting** - Payment exports, quality reports, and audit logs
5. **Bidirectional Communication** - RESTful APIs for seamless DPW App integration

### Key Metrics to Track

- **Daily Consensus Score** (70-100% determines bonus tier)
- **Tasks Completed** per worker per day
- **Quality Flags** (tasks requiring manual review)
- **Average Completion Time** (outlier detection)
- **Server Health** (uptime, response time, error rates)

---

## 2. Current Architecture Analysis

### Existing Database Schema

#### Tables
```
user (id, email, full_name, picture, role)
question (id, name, isPaused, created_date)
task (id, workerUniqueId, phoneNumber, progress, question_id, start_date)
link (id, url, task_id)  // Images to be tagged
answer (id, imageId, url, workerUniqueId, answer, question_id)
```

#### Current Capabilities
✅ User authentication (Google OAuth)  
✅ Task assignment to workers by phone number  
✅ Answer collection and CSV export  
✅ Progress tracking per worker  
✅ Admin/Worker role separation  

#### **CRITICAL GAPS** for DPW Integration
❌ No consensus algorithm implementation  
❌ No quality scoring system  
❌ No payment calculation logic  
❌ No performance analytics  
❌ No server health monitoring  
❌ No automated alerting  
❌ No batch payment export  
❌ No API for external system integration  

---

## 3. Required Enhancements

### 3.1 New Database Tables

**✅ STATUS: DATABASE MIGRATION COMPLETED**

The following 6 tables have been created and are ready for use. The migration file is located at:
`MicrotaskToolApi-master/src/main/resources/db/migration/V2__dpw_integration_tables.sql`

All tables will be automatically created when the Spring Boot application starts (Flyway migration).

#### `consensus_result` Table
```sql
CREATE TABLE consensus_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    image_id BIGINT NOT NULL,
    ground_truth VARCHAR(255),  -- Final consensus answer
    total_responses INT,
    consensus_percentage DECIMAL(5,2),  -- e.g., 80.00
    requires_review BOOLEAN DEFAULT FALSE,
    review_status VARCHAR(50),  -- 'pending', 'approved', 'rejected'
    reviewed_by VARCHAR(255),
    reviewed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    INDEX idx_question_image (question_id, image_id),
    INDEX idx_review (requires_review, review_status)
);
```

#### `worker_performance` Table
```sql
CREATE TABLE worker_performance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_unique_id VARCHAR(30) NOT NULL,
    question_id BIGINT NOT NULL,
    date DATE NOT NULL,
    tasks_completed INT DEFAULT 0,
    correct_answers INT DEFAULT 0,  -- Matched consensus
    incorrect_answers INT DEFAULT 0,  -- Did not match consensus
    consensus_score DECIMAL(5,2),  -- Daily score (0-100)
    average_time_per_task DECIMAL(10,2),  -- In seconds
    flagged_tasks INT DEFAULT 0,
    quality_tier VARCHAR(20),  -- 'excellent', 'good', 'fair', 'poor'
    base_pay DECIMAL(10,2),
    bonus_amount DECIMAL(10,2),
    total_payment DECIMAL(10,2),
    payment_status VARCHAR(50) DEFAULT 'pending',  -- 'pending', 'approved', 'paid'
    payment_reference VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    UNIQUE KEY unique_worker_daily (worker_unique_id, question_id, date),
    INDEX idx_worker (worker_unique_id),
    INDEX idx_date (date),
    INDEX idx_payment (payment_status, date)
);
```

#### `quality_flags` Table
```sql
CREATE TABLE quality_flags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_unique_id VARCHAR(30) NOT NULL,
    question_id BIGINT NOT NULL,
    flag_type VARCHAR(50),  -- 'low_consensus', 'high_speed', 'low_quality', 'manual'
    severity VARCHAR(20),  -- 'low', 'medium', 'high'
    description TEXT,
    flagged_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_by VARCHAR(255),
    resolved_at DATETIME,
    resolution_notes TEXT,
    INDEX idx_worker (worker_unique_id),
    INDEX idx_resolved (resolved),
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
);
```

#### `system_health` Table
```sql
CREATE TABLE system_health (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    metric_type VARCHAR(50),  -- 'cpu', 'memory', 'disk', 'response_time', 'error_rate'
    metric_value DECIMAL(10,2),
    status VARCHAR(20),  -- 'healthy', 'warning', 'critical'
    alert_sent BOOLEAN DEFAULT FALSE,
    alert_recipients TEXT,  -- JSON array of emails/phone numbers
    INDEX idx_timestamp (timestamp),
    INDEX idx_status (status, alert_sent)
);
```

#### `activity_log` Table
```sql
CREATE TABLE activity_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(255),
    worker_unique_id VARCHAR(30),
    action VARCHAR(100),  -- 'login', 'submit_answer', 'complete_task', 'sync_data'
    question_id BIGINT,
    metadata JSON,  -- Additional context
    ip_address VARCHAR(50),
    user_agent TEXT,
    INDEX idx_timestamp (timestamp),
    INDEX idx_user (user_id),
    INDEX idx_worker (worker_unique_id),
    INDEX idx_action (action)
);
```

#### `payment_export` Table
```sql
CREATE TABLE payment_export (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    export_date DATE NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_workers INT,
    total_amount DECIMAL(12,2),
    export_format VARCHAR(20),  -- 'csv', 'excel', 'json'
    file_path VARCHAR(512),
    exported_by VARCHAR(255),
    exported_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    dpw_sync_status VARCHAR(50) DEFAULT 'pending',  -- 'pending', 'synced', 'failed'
    dpw_sync_at DATETIME,
    dpw_reference VARCHAR(255),
    INDEX idx_export_date (export_date),
    INDEX idx_sync_status (dpw_sync_status)
);
```

---

## 4. API Specifications

### 4.1 DPW Integration Endpoints

All endpoints return JSON responses following this structure:
```json
{
  "status": "success|error",
  "message": "Human-readable message",
  "data": { },
  "timestamp": "2025-11-25T14:30:00Z"
}
```

#### **A. Consensus & Quality Endpoints**

##### GET `/api/v1/consensus/calculate/{questionId}`
**Purpose:** Calculate consensus for all images in a question  
**Authorization:** API Key (DPW App)

**Response:**
```json
{
  "status": "success",
  "data": {
    "question_id": 123,
    "total_images": 1000,
    "images_processed": 1000,
    "consensus_reached": 920,
    "requires_review": 80,
    "consensus_threshold": 60,
    "results": [
      {
        "image_id": 456,
        "url": "https://...",
        "ground_truth": "Yes",
        "votes": {
          "Yes": 4,
          "No": 1
        },
        "consensus_percentage": 80.0,
        "requires_review": false
      }
    ]
  }
}
```

##### POST `/api/v1/consensus/recalculate`
**Purpose:** Force recalculation (e.g., after manual review)  
**Body:**
```json
{
  "question_id": 123,
  "image_ids": [456, 789]  // Optional: specific images only
}
```

##### GET `/api/v1/quality/flags`
**Purpose:** Retrieve quality flags for review  
**Query Parameters:**
- `worker_id` (optional)
- `resolved` (true/false)
- `severity` (low/medium/high)
- `start_date`, `end_date`

**Response:**
```json
{
  "status": "success",
  "data": {
    "total_flags": 15,
    "unresolved": 8,
    "flags": [
      {
        "id": 1,
        "worker_unique_id": "W12345",
        "flag_type": "low_consensus",
        "severity": "medium",
        "description": "Worker consensus score below 70% for 3 consecutive days",
        "flagged_at": "2025-11-23T10:00:00Z",
        "resolved": false
      }
    ]
  }
}
```

##### POST `/api/v1/quality/resolve-flag`
**Purpose:** Mark a quality flag as resolved  
**Body:**
```json
{
  "flag_id": 1,
  "resolved_by": "admin@dpw.com",
  "resolution_notes": "Worker retrained, monitoring continued"
}
```

---

#### **B. Performance & Analytics Endpoints**

##### GET `/api/v1/performance/worker/{workerId}`
**Purpose:** Get detailed worker performance metrics  
**Query Parameters:**
- `start_date`, `end_date` (optional, defaults to last 7 days)
- `question_id` (optional)

**Response:**
```json
{
  "status": "success",
  "data": {
    "worker_unique_id": "W12345",
    "phone_number": "+254712345678",
    "period": {
      "start": "2025-11-18",
      "end": "2025-11-25"
    },
    "summary": {
      "total_tasks": 350,
      "average_consensus_score": 87.5,
      "total_earnings": 6916.00,
      "quality_tier": "good"
    },
    "daily_performance": [
      {
        "date": "2025-11-25",
        "tasks_completed": 50,
        "correct_answers": 45,
        "consensus_score": 90.0,
        "average_time_per_task": 45.5,
        "quality_tier": "excellent",
        "base_pay": 760.00,
        "bonus_amount": 228.00,
        "total_payment": 988.00,
        "payment_status": "pending"
      }
    ],
    "flags": []
  }
}
```

##### GET `/api/v1/performance/question/{questionId}`
**Purpose:** Get aggregate performance for all workers on a question  
**Response:**
```json
{
  "status": "success",
  "data": {
    "question_id": 123,
    "question_name": "GoPro 360 Amenity Tagging - Batch 1",
    "total_workers": 25,
    "total_images": 1000,
    "completion_rate": 98.5,
    "average_consensus_score": 85.2,
    "workers_by_tier": {
      "excellent": 12,
      "good": 8,
      "fair": 4,
      "poor": 1
    },
    "flagged_workers": 3,
    "total_payable": 24700.00
  }
}
```

##### GET `/api/v1/analytics/dashboard`
**Purpose:** Real-time dashboard metrics for DPW App  
**Response:**
```json
{
  "status": "success",
  "data": {
    "active_questions": 3,
    "active_workers": 45,
    "today": {
      "tasks_completed": 2250,
      "average_quality": 86.3,
      "pending_payments": 34200.00
    },
    "this_week": {
      "tasks_completed": 15750,
      "average_quality": 85.1,
      "total_payments": 239400.00
    },
    "system_health": {
      "status": "healthy",
      "uptime": "99.8%",
      "avg_response_time": 125
    },
    "alerts": [
      {
        "type": "quality",
        "severity": "medium",
        "message": "3 workers flagged for low consensus scores",
        "timestamp": "2025-11-25T12:00:00Z"
      }
    ]
  }
}
```

---

#### **C. Payment Processing Endpoints**

##### POST `/api/v1/payments/calculate`
**Purpose:** Calculate payments for a period  
**Body:**
```json
{
  "period_start": "2025-11-18",
  "period_end": "2025-11-25",
  "question_id": 123,  // Optional
  "worker_ids": []  // Optional: specific workers only
}
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "period": "2025-11-18 to 2025-11-25",
    "total_workers": 25,
    "total_base_pay": 133000.00,
    "total_bonuses": 25400.00,
    "total_payment": 158400.00,
    "breakdown": [
      {
        "worker_unique_id": "W12345",
        "phone_number": "+254712345678",
        "days_worked": 7,
        "total_tasks": 350,
        "average_consensus_score": 87.5,
        "base_pay": 5320.00,
        "bonus_amount": 1064.00,
        "total_payment": 6384.00,
        "payment_tier_breakdown": {
          "excellent_days": 4,
          "good_days": 2,
          "fair_days": 1,
          "no_bonus_days": 0
        }
      }
    ]
  }
}
```

##### POST `/api/v1/payments/approve`
**Purpose:** Approve calculated payments for processing  
**Body:**
```json
{
  "payment_ids": [1, 2, 3],  // worker_performance record IDs
  "approved_by": "admin@dpw.com",
  "payment_reference": "DPW-PAY-20251125-001"
}
```

##### GET `/api/v1/payments/export`
**Purpose:** Export payment data for DPW App processing  
**Query Parameters:**
- `period_start`, `period_end`
- `format` (csv/excel/json)
- `payment_status` (pending/approved/paid)

**Response:** File download (CSV/Excel) or JSON
```csv
Worker ID,Phone Number,Days Worked,Tasks Completed,Consensus Score,Base Pay,Bonus,Total Payment,Payment Status
W12345,+254712345678,7,350,87.5,5320.00,1064.00,6384.00,approved
```

##### POST `/api/v1/payments/sync-status`
**Purpose:** Update payment status after DPW processing  
**Body:**
```json
{
  "payment_reference": "DPW-PAY-20251125-001",
  "worker_payments": [
    {
      "worker_unique_id": "W12345",
      "status": "paid",
      "transaction_id": "MPESA-ABC123",
      "paid_at": "2025-11-25T16:00:00Z"
    }
  ]
}
```

---

#### **D. Server Health & Monitoring Endpoints**

##### GET `/api/v1/health/status`
**Purpose:** Current server health status  
**Response:**
```json
{
  "status": "success",
  "data": {
    "overall_status": "healthy",
    "timestamp": "2025-11-25T14:30:00Z",
    "metrics": {
      "cpu_usage": 35.2,
      "memory_usage": 62.5,
      "disk_usage": 45.8,
      "avg_response_time": 125,
      "error_rate": 0.02,
      "active_connections": 23
    },
    "database": {
      "status": "connected",
      "response_time": 15
    },
    "uptime": "15 days, 4 hours"
  }
}
```

##### GET `/api/v1/health/history`
**Purpose:** Historical health metrics  
**Query Parameters:**
- `start_date`, `end_date`
- `metric_type` (optional)

##### POST `/api/v1/alerts/configure`
**Purpose:** Configure alert thresholds and recipients  
**Body:**
```json
{
  "alert_type": "server_health",
  "thresholds": {
    "cpu_warning": 70,
    "cpu_critical": 90,
    "memory_warning": 80,
    "memory_critical": 95,
    "response_time_warning": 500,
    "error_rate_critical": 5.0
  },
  "recipients": [
    {
      "type": "email",
      "address": "admin@dpw.com"
    },
    {
      "type": "sms",
      "phone": "+254700000000"
    }
  ]
}
```

##### GET `/api/v1/alerts/active`
**Purpose:** Get currently active alerts  
**Response:**
```json
{
  "status": "success",
  "data": {
    "total_active": 2,
    "alerts": [
      {
        "id": 1,
        "type": "quality_flag",
        "severity": "medium",
        "message": "Worker W12345 has low consensus score (65%) for 3 consecutive days",
        "created_at": "2025-11-25T10:00:00Z",
        "acknowledged": false
      },
      {
        "id": 2,
        "type": "server_health",
        "severity": "warning",
        "message": "CPU usage exceeded 70% threshold (current: 75%)",
        "created_at": "2025-11-25T14:00:00Z",
        "acknowledged": true,
        "acknowledged_by": "admin@dpw.com"
      }
    ]
  }
}
```

---

#### **E. Activity Logging Endpoints**

##### GET `/api/v1/logs/activity`
**Purpose:** Retrieve activity logs  
**Query Parameters:**
- `worker_id` (optional)
- `action` (optional: login, submit_answer, etc.)
- `start_date`, `end_date`
- `limit`, `offset` (pagination)

**Response:**
```json
{
  "status": "success",
  "data": {
    "total_records": 15432,
    "limit": 100,
    "offset": 0,
    "logs": [
      {
        "id": 15432,
        "timestamp": "2025-11-25T14:25:30Z",
        "worker_unique_id": "W12345",
        "action": "submit_answer",
        "question_id": 123,
        "metadata": {
          "image_id": 456,
          "answer": "Yes",
          "time_taken": 42
        },
        "ip_address": "102.210.149.40"
      }
    ]
  }
}
```

##### GET `/api/v1/logs/worker-session/{workerId}`
**Purpose:** Track worker session details (login/logout times, sync events)  
**Response:**
```json
{
  "status": "success",
  "data": {
    "worker_unique_id": "W12345",
    "today_sessions": [
      {
        "login_time": "2025-11-25T08:00:00Z",
        "logout_time": "2025-11-25T12:00:00Z",
        "duration_minutes": 240,
        "tasks_completed": 25,
        "sync_events": 3
      }
    ],
    "total_today_duration": 480,
    "active_session": true
  }
}
```

---

## 5. Consensus & Quality Assurance Implementation

### 5.1 Consensus Algorithm Logic

#### Kotlin Service Implementation

```kotlin
@Service
class ConsensusService(
    @Autowired private val answerRepository: AnswerRepository,
    @Autowired private val consensusResultRepository: ConsensusResultRepository,
    @Autowired private val workerPerformanceRepository: WorkerPerformanceRepository
) {
    
    companion object {
        const val DEFAULT_CONSENSUS_THRESHOLD = 60.0  // Configurable
        const val MINIMUM_RESPONSES = 3  // Minimum answers needed for consensus
    }
    
    /**
     * Calculate consensus for all images in a question
     */
    fun calculateConsensus(questionId: Long, threshold: Double = DEFAULT_CONSENSUS_THRESHOLD): ConsensusCalculationResult {
        val answers = answerRepository.findByQuestionId(questionId)
        val imageGroups = answers.groupBy { it.imageId }
        
        val results = mutableListOf<ConsensusResult>()
        var consensusReached = 0
        var requiresReview = 0
        
        imageGroups.forEach { (imageId, imageAnswers) ->
            if (imageAnswers.size >= MINIMUM_RESPONSES) {
                val consensusResult = calculateImageConsensus(
                    questionId, 
                    imageId, 
                    imageAnswers, 
                    threshold
                )
                results.add(consensusResult)
                
                if (consensusResult.requiresReview) {
                    requiresReview++
                } else {
                    consensusReached++
                }
            }
        }
        
        // Save consensus results
        consensusResultRepository.saveAll(results.map { it.toEntity() })
        
        // Update worker performance scores
        updateWorkerPerformanceScores(questionId, results)
        
        return ConsensusCalculationResult(
            questionId = questionId,
            totalImages = imageGroups.size,
            consensusReached = consensusReached,
            requiresReview = requiresReview,
            threshold = threshold,
            results = results
        )
    }
    
    /**
     * Calculate consensus for a single image
     */
    private fun calculateImageConsensus(
        questionId: Long,
        imageId: Long,
        answers: List<AnswerEntity>,
        threshold: Double
    ): ConsensusResult {
        // Count votes for each answer
        val voteCounts = answers.groupingBy { it.answer }.eachCount()
        val totalVotes = answers.size
        
        // Find the majority answer
        val majorityEntry = voteCounts.maxByOrNull { it.value }
        val groundTruth = majorityEntry?.key ?: ""
        val majorityVotes = majorityEntry?.value ?: 0
        
        // Calculate consensus percentage
        val consensusPercentage = (majorityVotes.toDouble() / totalVotes) * 100
        
        // Determine if manual review is needed
        val requiresReview = consensusPercentage < threshold
        
        return ConsensusResult(
            questionId = questionId,
            imageId = imageId,
            imageUrl = answers.first().url,
            groundTruth = groundTruth,
            votes = voteCounts,
            totalResponses = totalVotes,
            consensusPercentage = consensusPercentage,
            requiresReview = requiresReview,
            reviewStatus = if (requiresReview) "pending" else "approved"
        )
    }
    
    /**
     * Update worker performance based on consensus results
     */
    private fun updateWorkerPerformanceScores(
        questionId: Long,
        consensusResults: List<ConsensusResult>
    ) {
        val today = LocalDate.now()
        val answers = answerRepository.findByQuestionId(questionId)
        
        // Group by worker
        val workerAnswers = answers.groupBy { it.workerUniqueId }
        
        workerAnswers.forEach { (workerId, workerAnswerList) ->
            var correctAnswers = 0
            var incorrectAnswers = 0
            
            workerAnswerList.forEach { answer ->
                val consensus = consensusResults.find { it.imageId == answer.imageId }
                if (consensus != null && !consensus.requiresReview) {
                    if (answer.answer == consensus.groundTruth) {
                        correctAnswers++
                    } else {
                        incorrectAnswers++
                    }
                }
            }
            
            val totalEvaluated = correctAnswers + incorrectAnswers
            val consensusScore = if (totalEvaluated > 0) {
                (correctAnswers.toDouble() / totalEvaluated) * 100
            } else {
                0.0
            }
            
            // Get or create worker performance record
            val performance = workerPerformanceRepository
                .findByWorkerAndQuestionAndDate(workerId, questionId, today)
                .orElseGet {
                    WorkerPerformance(
                        workerUniqueId = workerId,
                        questionId = questionId,
                        date = today
                    )
                }
            
            performance.correctAnswers = correctAnswers
            performance.incorrectAnswers = incorrectAnswers
            performance.consensusScore = BigDecimal(consensusScore)
            performance.tasksCompleted = workerAnswerList.size
            
            // Calculate payment
            calculatePayment(performance)
            
            // Flag low performers
            if (consensusScore < 70.0) {
                flagWorkerForQuality(workerId, questionId, "low_consensus", 
                    "Consensus score below 70%: $consensusScore")
            }
            
            workerPerformanceRepository.save(performance)
        }
    }
}
```

### 5.2 Quality Flagging Service

```kotlin
@Service
class QualityFlaggingService(
    @Autowired private val qualityFlagRepository: QualityFlagRepository,
    @Autowired private val workerPerformanceRepository: WorkerPerformanceRepository,
    @Autowired private val alertService: AlertService
) {
    
    /**
     * Flag worker for quality issues
     */
    fun flagWorker(
        workerId: String,
        questionId: Long,
        flagType: String,
        description: String,
        severity: String = "medium"
    ) {
        val flag = QualityFlag(
            workerUniqueId = workerId,
            questionId = questionId,
            flagType = flagType,
            severity = severity,
            description = description
        )
        
        qualityFlagRepository.save(flag)
        
        // Send alert if high severity
        if (severity == "high") {
            alertService.sendQualityAlert(flag)
        }
    }
    
    /**
     * Automated quality checks (run daily)
     */
    @Scheduled(cron = "0 0 22 * * *")  // 10 PM daily
    fun runDailyQualityChecks() {
        val today = LocalDate.now()
        val performances = workerPerformanceRepository.findByDate(today)
        
        performances.forEach { performance ->
            // Check 1: Low consensus score
            if (performance.consensusScore.toDouble() < 70.0) {
                flagWorker(
                    performance.workerUniqueId,
                    performance.questionId,
                    "low_consensus",
                    "Daily consensus score: ${performance.consensusScore}%",
                    severity = if (performance.consensusScore.toDouble() < 50.0) "high" else "medium"
                )
            }
            
            // Check 2: Suspiciously fast completion
            if (performance.averageTimePerTask.toDouble() < 20.0) {  // Less than 20 seconds per task
                flagWorker(
                    performance.workerUniqueId,
                    performance.questionId,
                    "high_speed",
                    "Average time per task: ${performance.averageTimePerTask}s (threshold: 20s)",
                    severity = "high"
                )
            }
            
            // Check 3: Consecutive poor performance
            val last3Days = workerPerformanceRepository
                .findByWorkerAndQuestion(performance.workerUniqueId, performance.questionId)
                .filter { it.date >= today.minusDays(3) }
                .sortedByDescending { it.date }
                .take(3)
            
            if (last3Days.size == 3 && last3Days.all { it.consensusScore.toDouble() < 70.0 }) {
                flagWorker(
                    performance.workerUniqueId,
                    performance.questionId,
                    "consecutive_low_quality",
                    "Low consensus score for 3 consecutive days",
                    severity = "high"
                )
            }
        }
    }
}
```

---

## 6. Payment Processing Integration

### 6.1 Payment Calculation Service

```kotlin
@Service
class PaymentCalculationService(
    @Autowired private val workerPerformanceRepository: WorkerPerformanceRepository
) {
    
    companion object {
        val BASE_PAY = BigDecimal("760.00")  // KES per day
        val BONUS_TIER_EXCELLENT = BigDecimal("0.30")  // 30% bonus for 90%+ score
        val BONUS_TIER_GOOD = BigDecimal("0.20")  // 20% bonus for 80-89% score
        val BONUS_TIER_FAIR = BigDecimal("0.10")  // 10% bonus for 70-79% score
    }
    
    /**
     * Calculate payment for a worker's daily performance
     */
    fun calculatePayment(performance: WorkerPerformance) {
        performance.basePay = BASE_PAY
        
        val score = performance.consensusScore.toDouble()
        val bonusRate = when {
            score >= 90.0 -> BONUS_TIER_EXCELLENT
            score >= 80.0 -> BONUS_TIER_GOOD
            score >= 70.0 -> BONUS_TIER_FAIR
            else -> BigDecimal.ZERO
        }
        
        performance.bonusAmount = BASE_PAY.multiply(bonusRate)
        performance.totalPayment = BASE_PAY.add(performance.bonusAmount)
        
        performance.qualityTier = when {
            score >= 90.0 -> "excellent"
            score >= 80.0 -> "good"
            score >= 70.0 -> "fair"
            else -> "poor"
        }
    }
    
    /**
     * Generate payment export for DPW App
     */
    fun generatePaymentExport(
        startDate: LocalDate,
        endDate: LocalDate,
        questionId: Long? = null
    ): PaymentExportData {
        val performances = if (questionId != null) {
            workerPerformanceRepository.findByQuestionAndDateRange(questionId, startDate, endDate)
        } else {
            workerPerformanceRepository.findByDateRange(startDate, endDate)
        }
        
        // Group by worker for period totals
        val workerTotals = performances.groupBy { it.workerUniqueId }
            .map { (workerId, workerPerformances) ->
                WorkerPaymentSummary(
                    workerUniqueId = workerId,
                    phoneNumber = getWorkerPhoneNumber(workerId),
                    daysWorked = workerPerformances.size,
                    totalTasks = workerPerformances.sumOf { it.tasksCompleted },
                    averageConsensusScore = workerPerformances.map { it.consensusScore }.average(),
                    basePay = workerPerformances.sumOf { it.basePay },
                    bonusAmount = workerPerformances.sumOf { it.bonusAmount },
                    totalPayment = workerPerformances.sumOf { it.totalPayment },
                    tierBreakdown = calculateTierBreakdown(workerPerformances)
                )
            }
        
        return PaymentExportData(
            periodStart = startDate,
            periodEnd = endDate,
            totalWorkers = workerTotals.size,
            totalBasePay = workerTotals.sumOf { it.basePay },
            totalBonuses = workerTotals.sumOf { it.bonusAmount },
            totalPayment = workerTotals.sumOf { it.totalPayment },
            workerSummaries = workerTotals
        )
    }
}
```

### 6.2 DPW Payment Sync Service

```kotlin
@Service
class DPWPaymentSyncService(
    @Autowired private val paymentExportRepository: PaymentExportRepository,
    @Autowired private val workerPerformanceRepository: WorkerPerformanceRepository,
    @Autowired private val restTemplate: RestTemplate
) {
    
    @Value("\${dpw.api.base-url}")
    private lateinit var dpwApiBaseUrl: String
    
    @Value("\${dpw.api.key}")
    private lateinit var dpwApiKey: String
    
    /**
     * Send payment data to DPW App
     */
    fun syncPaymentsWithDPW(exportId: Long): DPWSyncResult {
        val export = paymentExportRepository.findById(exportId)
            .orElseThrow { RuntimeException("Payment export not found") }
        
        // Prepare DPW payload
        val payload = DPWPaymentPayload(
            period_start = export.periodStart,
            period_end = export.periodEnd,
            total_amount = export.totalAmount,
            workers = loadWorkerPaymentsForExport(export)
        )
        
        // Send to DPW API
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $dpwApiKey")
        headers.contentType = MediaType.APPLICATION_JSON
        
        val request = HttpEntity(payload, headers)
        
        return try {
            val response = restTemplate.postForEntity(
                "$dpwApiBaseUrl/api/microtasking/payments",
                request,
                DPWPaymentResponse::class.java
            )
            
            if (response.statusCode.is2xxSuccessful && response.body != null) {
                export.dpwSyncStatus = "synced"
                export.dpwSyncAt = LocalDateTime.now()
                export.dpwReference = response.body!!.reference
                paymentExportRepository.save(export)
                
                DPWSyncResult(success = true, message = "Synced successfully", reference = response.body!!.reference)
            } else {
                export.dpwSyncStatus = "failed"
                paymentExportRepository.save(export)
                
                DPWSyncResult(success = false, message = "DPW API returned error: ${response.statusCode}")
            }
        } catch (e: Exception) {
            export.dpwSyncStatus = "failed"
            paymentExportRepository.save(export)
            
            DPWSyncResult(success = false, message = "Sync failed: ${e.message}")
        }
    }
    
    /**
     * Receive payment status updates from DPW App
     */
    fun updatePaymentStatus(dpwReference: String, workerPayments: List<WorkerPaymentStatus>) {
        val export = paymentExportRepository.findByDpwReference(dpwReference)
            ?: throw RuntimeException("Payment export not found for reference: $dpwReference")
        
        workerPayments.forEach { payment ->
            workerPerformanceRepository
                .findByWorkerAndDateRange(payment.workerUniqueId, export.periodStart, export.periodEnd)
                .forEach { performance ->
                    performance.paymentStatus = payment.status
                    performance.paymentReference = payment.transactionId
                    workerPerformanceRepository.save(performance)
                }
        }
    }
}
```

---

## 7. Performance Monitoring & Analytics

### 7.1 Real-Time Dashboard Service

```kotlin
@Service
class DashboardAnalyticsService(
    @Autowired private val workerPerformanceRepository: WorkerPerformanceRepository,
    @Autowired private val questionRepository: QuestionRepository,
    @Autowired private val taskRepository: TaskRepository,
    @Autowired private val qualityFlagRepository: QualityFlagRepository,
    @Autowired private val systemHealthRepository: SystemHealthRepository
) {
    
    /**
     * Generate dashboard metrics for DPW App
     */
    fun getDashboardMetrics(): DashboardMetrics {
        val today = LocalDate.now()
        val weekStart = today.minusDays(7)
        
        return DashboardMetrics(
            activeQuestions = questionRepository.countByIsPaused(false),
            activeWorkers = taskRepository.countDistinctWorkersByStartDateAfter(weekStart),
            today = getTodayMetrics(today),
            thisWeek = getWeekMetrics(weekStart, today),
            systemHealth = getSystemHealth(),
            alerts = getActiveAlerts()
        )
    }
    
    private fun getTodayMetrics(date: LocalDate): PeriodMetrics {
        val performances = workerPerformanceRepository.findByDate(date)
        
        return PeriodMetrics(
            tasksCompleted = performances.sumOf { it.tasksCompleted },
            averageQuality = performances.map { it.consensusScore.toDouble() }.average(),
            pendingPayments = performances
                .filter { it.paymentStatus == "pending" }
                .sumOf { it.totalPayment }
        )
    }
    
    private fun getSystemHealth(): SystemHealthStatus {
        val latestHealth = systemHealthRepository.findTopByOrderByTimestampDesc()
        
        return SystemHealthStatus(
            status = latestHealth?.status ?: "unknown",
            uptime = calculateUptime(),
            avgResponseTime = latestHealth?.metricValue?.toInt() ?: 0
        )
    }
}
```

### 7.2 Worker Activity Tracking

```kotlin
@Service
class ActivityLoggingService(
    @Autowired private val activityLogRepository: ActivityLogRepository
) {
    
    /**
     * Log worker activity
     */
    fun logActivity(
        workerId: String,
        action: String,
        questionId: Long? = null,
        metadata: Map<String, Any>? = null,
        request: HttpServletRequest
    ) {
        val log = ActivityLog(
            workerUniqueId = workerId,
            action = action,
            questionId = questionId,
            metadata = metadata?.let { objectMapper.writeValueAsString(it) },
            ipAddress = request.remoteAddr,
            userAgent = request.getHeader("User-Agent")
        )
        
        activityLogRepository.save(log)
    }
    
    /**
     * Get worker session analytics
     */
    fun getWorkerSession(workerId: String, date: LocalDate): WorkerSessionData {
        val logs = activityLogRepository.findByWorkerAndDate(workerId, date)
        
        val loginLogs = logs.filter { it.action == "login" }
        val logoutLogs = logs.filter { it.action == "logout" }
        val submitLogs = logs.filter { it.action == "submit_answer" }
        
        val sessions = mutableListOf<SessionInfo>()
        loginLogs.forEachIndexed { index, login ->
            val logout = logoutLogs.getOrNull(index)
            sessions.add(
                SessionInfo(
                    loginTime = login.timestamp,
                    logoutTime = logout?.timestamp,
                    durationMinutes = logout?.let { 
                        ChronoUnit.MINUTES.between(login.timestamp, it.timestamp).toInt() 
                    },
                    tasksCompleted = submitLogs.count { 
                        it.timestamp >= login.timestamp && 
                        (logout == null || it.timestamp <= logout.timestamp)
                    }
                )
            )
        }
        
        return WorkerSessionData(
            workerUniqueId = workerId,
            date = date,
            sessions = sessions,
            totalDuration = sessions.sumOf { it.durationMinutes ?: 0 },
            activeSession = logoutLogs.size < loginLogs.size
        )
    }
}
```

---

## 8. Server Health Monitoring & Alerts

### 8.1 Health Monitoring Service

```kotlin
@Service
class ServerHealthMonitoringService(
    @Autowired private val systemHealthRepository: SystemHealthRepository,
    @Autowired private val alertService: AlertService
) {
    
    private val runtime = Runtime.getRuntime()
    private val systemInfo = SystemInfo()
    private val hal = systemInfo.hardware
    
    /**
     * Collect system metrics (runs every 5 minutes)
     */
    @Scheduled(fixedDelay = 300000)  // 5 minutes
    fun collectHealthMetrics() {
        val metrics = listOf(
            collectCPUMetric(),
            collectMemoryMetric(),
            collectDiskMetric(),
            collectResponseTimeMetric(),
            collectErrorRateMetric()
        )
        
        systemHealthRepository.saveAll(metrics)
        
        // Check thresholds and send alerts
        metrics.forEach { metric ->
            checkThresholdAndAlert(metric)
        }
    }
    
    private fun collectCPUMetric(): SystemHealth {
        val processor = hal.processor
        val cpuUsage = processor.getSystemCpuLoad(1000) * 100
        
        val status = when {
            cpuUsage >= 90 -> "critical"
            cpuUsage >= 70 -> "warning"
            else -> "healthy"
        }
        
        return SystemHealth(
            metricType = "cpu",
            metricValue = BigDecimal(cpuUsage),
            status = status
        )
    }
    
    private fun collectMemoryMetric(): SystemHealth {
        val memory = hal.memory
        val totalMemory = memory.total
        val availableMemory = memory.available
        val usedMemory = totalMemory - availableMemory
        val memoryUsage = (usedMemory.toDouble() / totalMemory) * 100
        
        val status = when {
            memoryUsage >= 95 -> "critical"
            memoryUsage >= 80 -> "warning"
            else -> "healthy"
        }
        
        return SystemHealth(
            metricType = "memory",
            metricValue = BigDecimal(memoryUsage),
            status = status
        )
    }
    
    private fun collectDiskMetric(): SystemHealth {
        val fileStore = hal.diskStores.firstOrNull()
        val diskUsage = if (fileStore != null) {
            val total = fileStore.size
            val usable = Files.getFileStore(Paths.get("/")).usableSpace
            ((total - usable).toDouble() / total) * 100
        } else 0.0
        
        val status = when {
            diskUsage >= 90 -> "critical"
            diskUsage >= 75 -> "warning"
            else -> "healthy"
        }
        
        return SystemHealth(
            metricType = "disk",
            metricValue = BigDecimal(diskUsage),
            status = status
        )
    }
    
    private fun checkThresholdAndAlert(metric: SystemHealth) {
        if (metric.status in listOf("warning", "critical") && !metric.alertSent) {
            alertService.sendHealthAlert(metric)
            metric.alertSent = true
            systemHealthRepository.save(metric)
        }
    }
}
```

### 8.2 Alert Service

```kotlin
@Service
class AlertService(
    @Autowired private val mailSender: JavaMailSender,
    @Autowired private val smsService: SMSService
) {
    
    @Value("\${alert.recipients.emails}")
    private lateinit var alertEmails: List<String>
    
    @Value("\${alert.recipients.sms}")
    private lateinit var alertPhones: List<String>
    
    /**
     * Send health alert
     */
    fun sendHealthAlert(metric: SystemHealth) {
        val subject = "[${metric.status.uppercase()}] Microtasking Server Alert"
        val message = """
            Server Health Alert
            
            Metric: ${metric.metricType.uppercase()}
            Value: ${metric.metricValue}%
            Status: ${metric.status.uppercase()}
            Timestamp: ${metric.timestamp}
            
            Please investigate immediately.
        """.trimIndent()
        
        // Send email alerts
        alertEmails.forEach { email ->
            sendEmailAlert(email, subject, message)
        }
        
        // Send SMS for critical alerts
        if (metric.status == "critical") {
            alertPhones.forEach { phone ->
                smsService.sendSMS(phone, "CRITICAL: ${metric.metricType} at ${metric.metricValue}%")
            }
        }
    }
    
    /**
     * Send quality alert
     */
    fun sendQualityAlert(flag: QualityFlag) {
        val subject = "[QUALITY] Worker Quality Alert"
        val message = """
            Quality Issue Flagged
            
            Worker: ${flag.workerUniqueId}
            Flag Type: ${flag.flagType}
            Severity: ${flag.severity.uppercase()}
            Description: ${flag.description}
            Timestamp: ${flag.flaggedAt}
            
            Please review and take action.
        """.trimIndent()
        
        alertEmails.forEach { email ->
            sendEmailAlert(email, subject, message)
        }
    }
    
    private fun sendEmailAlert(to: String, subject: String, body: String) {
        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true)
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(body)
            mailSender.send(message)
        } catch (e: Exception) {
            // Log error but don't fail
            log.error("Failed to send email alert to $to", e)
        }
    }
}
```

---

## 9. Logging & Audit Trail

### 9.1 Comprehensive Logging Strategy

**What to Log:**

1. **User Actions**
   - Login/logout events
   - Answer submissions
   - Task assignments
   - Data synchronization

2. **System Events**
   - API requests/responses
   - Database queries (slow queries)
   - Error occurrences
   - Health check results

3. **Business Events**
   - Consensus calculations
   - Payment calculations
   - Quality flags raised/resolved
   - Payment status changes

4. **Security Events**
   - Failed login attempts
   - Unauthorized access attempts
   - API key usage
   - Permission changes

### 9.2 Log Retention Policy

```kotlin
@Service
class LogRetentionService(
    @Autowired private val activityLogRepository: ActivityLogRepository,
    @Autowired private val systemHealthRepository: SystemHealthRepository
) {
    
    /**
     * Archive old logs (runs monthly)
     */
    @Scheduled(cron = "0 0 2 1 * *")  // 2 AM on 1st of each month
    fun archiveOldLogs() {
        val cutoffDate = LocalDateTime.now().minusMonths(6)
        
        // Archive activity logs older than 6 months
        val oldActivityLogs = activityLogRepository.findByTimestampBefore(cutoffDate)
        if (oldActivityLogs.isNotEmpty()) {
            exportToArchive(oldActivityLogs, "activity_logs")
            activityLogRepository.deleteAll(oldActivityLogs)
        }
        
        // Archive health metrics older than 3 months
        val oldHealthMetrics = systemHealthRepository.findByTimestampBefore(
            LocalDateTime.now().minusMonths(3)
        )
        if (oldHealthMetrics.isNotEmpty()) {
            exportToArchive(oldHealthMetrics, "health_metrics")
            systemHealthRepository.deleteAll(oldHealthMetrics)
        }
    }
    
    private fun exportToArchive(data: List<Any>, filename: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        val archivePath = "/var/log/microtasking/archives/$filename-$timestamp.json"
        
        File(archivePath).writeText(objectMapper.writeValueAsString(data))
    }
}
```

---

## 10. Security Considerations

### 10.1 API Authentication

**API Key Management for DPW App:**

```kotlin
@Configuration
class SecurityConfig : WebSecurityConfigurerAdapter() {
    
    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll()
            .and()
            .addFilterBefore(ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }
}

@Component
class ApiKeyAuthFilter : OncePerRequestFilter() {
    
    @Value("\${dpw.api.keys}")
    private lateinit var validApiKeys: List<String>
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader("X-API-Key")
        
        if (request.requestURI.startsWith("/api/v1/")) {
            if (apiKey == null || !validApiKeys.contains(apiKey)) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.writer.write("""{"error": "Invalid or missing API key"}""")
                return
            }
        }
        
        filterChain.doFilter(request, response)
    }
}
```

### 10.2 Data Privacy

- **PII Protection**: Worker phone numbers encrypted at rest
- **GDPR Compliance**: Data deletion upon request
- **Audit Logging**: All data access logged
- **Role-Based Access Control**: Admin vs Worker permissions

### 10.3 Rate Limiting

```kotlin
@Component
class RateLimitFilter : OncePerRequestFilter() {
    
    private val requestCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val resetTimes = ConcurrentHashMap<String, Long>()
    
    companion object {
        const val MAX_REQUESTS_PER_MINUTE = 60
    }
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientId = request.getHeader("X-API-Key") ?: request.remoteAddr
        val now = System.currentTimeMillis()
        
        val resetTime = resetTimes.getOrPut(clientId) { now + 60000 }
        
        if (now > resetTime) {
            requestCounts[clientId] = AtomicInteger(0)
            resetTimes[clientId] = now + 60000
        }
        
        val count = requestCounts.getOrPut(clientId) { AtomicInteger(0) }
        
        if (count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            response.status = HttpServletResponse.SC_TOO_MANY_REQUESTS
            response.writer.write("""{"error": "Rate limit exceeded"}""")
            return
        }
        
        filterChain.doFilter(request, response)
    }
}
```

---

## 11. Implementation Roadmap

### Phase 1: Database & Core Services (Week 1-2)
- ✅ **COMPLETED:** Database tables created (V2 migration)
- ⏳ Implement ConsensusService
- ⏳ Implement PaymentCalculationService
- ⏳ Implement QualityFlaggingService
- ⏳ Write unit tests

### Phase 2: API Development (Week 2-3)
- ⏳ Implement consensus endpoints
- ⏳ Implement performance endpoints
- ⏳ Implement payment endpoints
- ⏳ Implement health monitoring endpoints
- ⏳ API documentation (Swagger/OpenAPI)

### Phase 3: Monitoring & Alerts (Week 3-4)
- ⏳ Implement ServerHealthMonitoringService
- ⏳ Implement AlertService (email + SMS)
- ⏳ Configure alert thresholds
- ⏳ Dashboard analytics service

### Phase 4: DPW Integration (Week 4-5)
- ⏳ DPW API client implementation
- ⏳ Payment sync service
- ⏳ Webhook endpoints for status updates
- ⏳ Integration testing

### Phase 5: Testing & Deployment (Week 5-6)
- ⏳ End-to-end testing
- ⏳ Load testing
- ⏳ Security audit
- ⏳ Production deployment
- ⏳ Documentation handoff

---

## 12. Appendices

### Appendix A: Configuration Properties

```properties
# application.properties additions

# DPW Integration
dpw.api.base-url=https://dpw.example.com
dpw.api.key=YOUR_API_KEY_HERE
dpw.api.keys=KEY1,KEY2,KEY3  # Multiple keys for different environments

# Consensus Configuration
consensus.default-threshold=60.0
consensus.minimum-responses=3

# Payment Configuration
payment.base-pay=760.00
payment.bonus.excellent=0.30
payment.bonus.good=0.20
payment.bonus.fair=0.10

# Alert Configuration
alert.recipients.emails=admin@dpw.com,manager@dpw.com
alert.recipients.sms=+254700000000,+254711111111
alert.thresholds.cpu.warning=70
alert.thresholds.cpu.critical=90
alert.thresholds.memory.warning=80
alert.thresholds.memory.critical=95

# Health Monitoring
health.check.interval=300000  # 5 minutes in milliseconds
health.retention.days=90

# Logging
logging.level.com.spatialcollective.microtasktoolapi=DEBUG
logging.file.name=/var/log/microtasking/application.log
logging.file.max-size=100MB
logging.file.max-history=30
```

### Appendix B: DPW App Requirements

**What the DPW App Needs to Implement:**

1. **API Endpoints to Receive Data:**
   - `POST /api/microtasking/payments` - Receive payment data from Microtasking platform
   - `POST /api/microtasking/performance` - Receive performance metrics
   - `POST /api/microtasking/alerts` - Receive system/quality alerts

2. **Webhook Endpoints to Call:**
   - `POST micro.spatialcollective.co.ke/api/v1/payments/sync-status` - Update payment status after processing

3. **Data Storage:**
   - Store worker payment records
   - Store performance history
   - Store alert logs

4. **UI Components:**
   - Microtasking dashboard widget
   - Worker performance reports
   - Payment approval interface
   - Quality flag review interface

5. **Export Formats Supported:**
   - CSV (for M-Pesa bulk upload)
   - Excel (for reporting)
   - JSON (for programmatic access)

### Appendix C: Sample Data Flows

**Flow 1: Daily Performance Calculation**
```
1. Worker completes tasks during the day
2. At 10 PM, ConsensusService calculates consensus for all images
3. Worker performance scores calculated based on consensus
4. Payments calculated using tiered bonus system
5. Low performers flagged automatically
6. Alerts sent to managers if high-severity flags
7. Performance data available via API for DPW App
```

**Flow 2: Payment Processing**
```
1. Admin generates payment export for period (e.g., weekly)
2. Microtasking platform calculates all worker payments
3. Export synced to DPW App via API
4. DPW App displays payment approval interface
5. Admin approves payments in DPW App
6. DPW App processes M-Pesa payments
7. DPW App sends payment status update to Microtasking platform
8. Worker performance records updated with payment status
```

**Flow 3: Quality Flag Resolution**
```
1. System flags worker for low consensus score
2. Alert sent to supervisor
3. Supervisor reviews flag via DPW App
4. Supervisor interviews worker, provides retraining
5. Supervisor marks flag as resolved via API
6. Worker continues working, monitored closely
7. If performance improves, no further action
8. If performance doesn't improve, worker may be removed from project
```

---

## Summary of Deliverables

### For Microtasking Platform Developers:

1. **Database Migrations** - SQL scripts for 6 new tables
2. **Service Layer** - 8 new Kotlin services (Consensus, Payment, Quality, Health, etc.)
3. **REST API** - 25+ new endpoints for DPW integration
4. **Scheduled Jobs** - Daily quality checks, health monitoring, log archival
5. **Alert System** - Email + SMS notifications
6. **Documentation** - API docs, deployment guide, operations manual

### For DPW App Developers:

1. **Integration Guide** - How to connect to Microtasking APIs
2. **Webhook Specifications** - Endpoints to implement for bidirectional communication
3. **Data Schemas** - JSON structures for all API exchanges
4. **UI Requirements** - Dashboard widgets and management interfaces
5. **Test Data** - Sample API responses for development

### Timeline: **6 weeks** from development start to production deployment

### Cost Estimate:
- Development: 4-5 developers × 6 weeks
- Infrastructure: Health monitoring tools, SMS gateway
- Testing: QA resources for integration testing

---

**End of Technical Specification**

For questions or clarifications, contact:  
Technical Lead: tech@spatialcollective.com  
Project Repository: https://github.com/SpatialCollectiveLtd/microtasking
