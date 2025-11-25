# DPW App Integration Guide
## Microtasking Platform API Integration

**Version:** 1.0  
**Date:** November 25, 2025  
**Platform:** micro.spatialcollective.co.ke  
**Management Dashboard:** app.spatialcollective.com  

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Architecture Overview](#architecture-overview)
3. [Getting Started](#getting-started)
4. [Authentication & Security](#authentication-security)
5. [API Reference](#api-reference)
6. [Integration Workflows](#integration-workflows)
7. [Webhook Implementation](#webhook-implementation)
8. [Data Schemas](#data-schemas)
9. [Testing Guide](#testing-guide)
10. [Deployment Checklist](#deployment-checklist)
11. [Troubleshooting](#troubleshooting)
12. [Support & Contact](#support-contact)

---

## 1. Executive Summary

### What is This Document?

This guide provides everything the DPW App development team needs to integrate the Microtasking Platform with your management dashboard at `app.spatialcollective.com`.

### Integration Capabilities

Once integrated, the DPW App will be able to:

✅ **Monitor Worker Performance** - Real-time access to consensus scores, task completion rates, and quality metrics  
✅ **Process Payments** - Calculate bonuses, approve payments, and sync payment statuses  
✅ **Manage Quality** - Review flagged workers, resolve quality issues, and track improvements  
✅ **View Analytics** - Access dashboard metrics, performance trends, and system health  
✅ **Receive Alerts** - Get notified about quality issues, system problems, and payment approvals  
✅ **Export Data** - Download payment reports in CSV/Excel/JSON formats  

### Current Implementation Status

**✅ Completed:**
- Database schema with 6 new tables for DPW integration
- Database migration ready (Flyway V2)
- Basic API infrastructure (Spring Boot + Kotlin)
- OAuth authentication for workers/admins

**⏳ In Progress:**
- API endpoints for consensus, performance, and payments
- Background jobs for quality calculations
- Alert system (email/SMS)
- Health monitoring service

**📋 Pending:**
- Full API documentation (Swagger/OpenAPI)
- Webhook implementations
- Integration testing
- Production deployment

---

## 2. Architecture Overview

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                       DPW App                                │
│              (app.spatialcollective.com)                     │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Dashboard   │  │   Payment    │  │   Quality    │     │
│  │   Widget     │  │  Processor   │  │   Manager    │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
│         │                  │                  │              │
└─────────┼──────────────────┼──────────────────┼──────────────┘
          │                  │                  │
          │ REST API         │ Webhook          │ Alerts
          │                  │                  │
┌─────────▼──────────────────▼──────────────────▼──────────────┐
│              Microtasking Platform API                        │
│          (micro.spatialcollective.co.ke:8080)                │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Consensus   │  │  Payment     │  │   Health     │     │
│  │   Service    │  │  Calculator  │  │  Monitoring  │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
│         │                  │                  │              │
│         └──────────────────┼──────────────────┘              │
│                            │                                  │
│                   ┌────────▼────────┐                        │
│                   │   MySQL DB      │                        │
│                   │  (Production)   │                        │
│                   └─────────────────┘                        │
└───────────────────────────────────────────────────────────────┘
```

### Database Tables (DPW Integration)

The following tables are available for API operations:

1. **`consensus_result`** - Final answers with agreement percentages
2. **`worker_performance`** - Daily scores, payments, and quality tiers
3. **`quality_flags`** - Issues requiring manual review
4. **`system_health`** - Server metrics and alerts
5. **`activity_log`** - Audit trail of all actions
6. **`payment_export`** - Batch payment records for DPW sync

### Technology Stack

**Backend:**
- Kotlin + Spring Boot 2.7+
- MySQL 8.0
- Flyway (database migrations)
- JPA/Hibernate (ORM)

**Frontend (Microtasking):**
- Kotlin/JS + React
- Google OAuth

**Integration:**
- RESTful APIs (JSON)
- JWT/API Key authentication
- Webhooks for bidirectional sync

---

## 3. Getting Started

### Prerequisites

Before integrating with the Microtasking Platform, ensure you have:

- [ ] API key provided by Spatial Collective technical team
- [ ] Access to `app.spatialcollective.com` development/staging environment
- [ ] Ability to make HTTPS requests to `micro.spatialcollective.co.ke`
- [ ] Database or storage for caching API responses
- [ ] Email/SMS service for receiving alerts (optional)

### Quick Start Guide

**Step 1: Get API Credentials**

Contact: tech@spatialcollective.com

Request:
- Production API key
- Staging API key (for testing)
- Webhook authentication token

**Step 2: Test Connection**

```bash
curl -X GET https://micro.spatialcollective.co.ke:8080/api/v1/health \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

Expected response:
```json
{
  "status": "success",
  "message": "Microtasking API is operational",
  "timestamp": "2025-11-25T14:30:00Z",
  "version": "1.0.0"
}
```

**Step 3: Fetch Sample Data**

```bash
curl -X GET "https://micro.spatialcollective.co.ke:8080/api/v1/analytics/dashboard" \
  -H "X-API-Key: YOUR_API_KEY_HERE" \
  -H "Content-Type: application/json"
```

**Step 4: Implement Webhook Endpoint**

Your app must expose this endpoint:
```
POST https://app.spatialcollective.com/api/webhooks/microtasking
```

See [Webhook Implementation](#webhook-implementation) for details.

---

## 4. Authentication & Security

### API Key Authentication

All API requests require an API key in the header:

```http
GET /api/v1/performance/worker/W12345 HTTP/1.1
Host: micro.spatialcollective.co.ke:8080
X-API-Key: dpw_prod_a1b2c3d4e5f6g7h8i9j0
Content-Type: application/json
```

### API Key Types

| Environment | Key Prefix | Rate Limit | Purpose |
|-------------|-----------|------------|---------|
| Production | `dpw_prod_` | 1000/hour | Live data access |
| Staging | `dpw_stage_` | 500/hour | Testing/development |
| Development | `dpw_dev_` | 100/hour | Local development |

### Security Best Practices

**DO:**
✅ Store API keys in environment variables or secret managers  
✅ Use HTTPS for all API requests  
✅ Implement request signing for webhooks  
✅ Log all API interactions for audit trails  
✅ Rotate API keys every 90 days  

**DON'T:**
❌ Hardcode API keys in source code  
❌ Commit API keys to version control  
❌ Share API keys across multiple environments  
❌ Use production keys for testing  
❌ Expose API keys in client-side code  

### Rate Limiting

Current limits:
- **60 requests per minute** per API key
- **1000 requests per hour** per API key
- **10,000 requests per day** per API key

Rate limit headers in responses:
```http
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1700924400
```

If exceeded:
```json
{
  "status": "error",
  "message": "Rate limit exceeded. Try again in 30 seconds.",
  "error_code": "RATE_LIMIT_EXCEEDED",
  "retry_after": 30
}
```

### Webhook Authentication

Webhooks from Microtasking Platform include a signature:

```http
POST /api/webhooks/microtasking HTTP/1.1
Host: app.spatialcollective.com
Content-Type: application/json
X-Webhook-Signature: sha256=abc123...
X-Webhook-ID: wh_xyz789
X-Webhook-Timestamp: 1700924400

{
  "event": "payment.calculated",
  "data": { ... }
}
```

**Verify signature:**

```javascript
const crypto = require('crypto');

function verifyWebhook(payload, signature, secret) {
  const hmac = crypto.createHmac('sha256', secret);
  const digest = 'sha256=' + hmac.update(payload).digest('hex');
  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(digest)
  );
}
```

---

## 5. API Reference

### Base URL

```
https://micro.spatialcollective.co.ke:8080/api/v1
```

### Standard Response Format

All API endpoints return JSON responses with this structure:

**Success:**
```json
{
  "status": "success",
  "message": "Operation completed successfully",
  "data": { /* endpoint-specific data */ },
  "timestamp": "2025-11-25T14:30:00Z"
}
```

**Error:**
```json
{
  "status": "error",
  "message": "Human-readable error description",
  "error_code": "ERROR_CODE_HERE",
  "details": { /* additional error context */ },
  "timestamp": "2025-11-25T14:30:00Z"
}
```

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `AUTH_INVALID_KEY` | 401 | Invalid or missing API key |
| `AUTH_EXPIRED_KEY` | 401 | API key has expired |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `RESOURCE_NOT_FOUND` | 404 | Resource does not exist |
| `VALIDATION_ERROR` | 400 | Invalid request parameters |
| `INTERNAL_ERROR` | 500 | Server-side error |

### Endpoints Overview

#### Analytics & Dashboard
- `GET /analytics/dashboard` - Real-time dashboard metrics
- `GET /analytics/trends` - Performance trends over time

#### Worker Performance
- `GET /performance/worker/{workerId}` - Individual worker metrics
- `GET /performance/question/{questionId}` - Question-wide performance
- `GET /performance/leaderboard` - Top performers

#### Consensus & Quality
- `GET /consensus/calculate/{questionId}` - Calculate consensus
- `POST /consensus/recalculate` - Force recalculation
- `GET /quality/flags` - Get quality flags
- `POST /quality/resolve-flag` - Resolve a flag

#### Payment Processing
- `POST /payments/calculate` - Calculate period payments
- `POST /payments/approve` - Approve payments
- `GET /payments/export` - Export payment data
- `POST /payments/sync-status` - Update payment status

#### System Health
- `GET /health` - System health check
- `GET /health/metrics` - Detailed metrics
- `GET /health/alerts` - Active alerts

---

## 6. API Endpoints (Detailed)

### 6.1 Dashboard Analytics

#### GET `/api/v1/analytics/dashboard`

Get real-time dashboard metrics for the DPW management interface.

**Request:**
```http
GET /api/v1/analytics/dashboard HTTP/1.1
Host: micro.spatialcollective.co.ke:8080
X-API-Key: dpw_prod_xxxxx
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "overview": {
      "active_questions": 3,
      "active_workers": 45,
      "total_workers_registered": 127
    },
    "today": {
      "tasks_completed": 2250,
      "average_consensus_score": 86.3,
      "pending_payments": 34200.00,
      "flagged_workers": 3
    },
    "this_week": {
      "tasks_completed": 15750,
      "average_consensus_score": 85.1,
      "total_payments": 239400.00,
      "total_workers": 52
    },
    "system_health": {
      "status": "healthy",
      "uptime_percentage": 99.8,
      "avg_response_time_ms": 125,
      "error_rate_percentage": 0.02
    },
    "recent_alerts": [
      {
        "type": "quality",
        "severity": "medium",
        "message": "3 workers flagged for low consensus scores",
        "timestamp": "2025-11-25T12:00:00Z",
        "resolved": false
      }
    ],
    "payment_summary": {
      "pending_approval": 34200.00,
      "approved_not_paid": 52800.00,
      "paid_this_month": 425600.00
    }
  },
  "timestamp": "2025-11-25T14:30:00Z"
}
```

**Use Case:** Display on DPW App homepage dashboard widget

---

### 6.2 Worker Performance

#### GET `/api/v1/performance/worker/{workerId}`

Get detailed performance metrics for a specific worker.

**Request:**
```http
GET /api/v1/performance/worker/W12345?start_date=2025-11-18&end_date=2025-11-25 HTTP/1.1
Host: micro.spatialcollective.co.ke:8080
X-API-Key: dpw_prod_xxxxx
```

**Query Parameters:**
- `start_date` (optional): YYYY-MM-DD format, default: 7 days ago
- `end_date` (optional): YYYY-MM-DD format, default: today
- `question_id` (optional): Filter by specific question

**Response:**
```json
{
  "status": "success",
  "data": {
    "worker_info": {
      "worker_unique_id": "W12345",
      "phone_number": "+254712345678",
      "full_name": "John Doe",
      "registration_date": "2025-10-15"
    },
    "period": {
      "start_date": "2025-11-18",
      "end_date": "2025-11-25"
    },
    "summary": {
      "days_worked": 7,
      "total_tasks": 350,
      "avg_tasks_per_day": 50,
      "average_consensus_score": 87.5,
      "total_earnings": 6916.00,
      "quality_tier_distribution": {
        "excellent": 4,
        "good": 2,
        "fair": 1,
        "poor": 0
      }
    },
    "daily_breakdown": [
      {
        "date": "2025-11-25",
        "tasks_completed": 50,
        "correct_answers": 45,
        "incorrect_answers": 5,
        "consensus_score": 90.0,
        "average_time_per_task_seconds": 45.5,
        "quality_tier": "excellent",
        "base_pay": 760.00,
        "bonus_amount": 228.00,
        "total_payment": 988.00,
        "payment_status": "pending"
      },
      {
        "date": "2025-11-24",
        "tasks_completed": 50,
        "correct_answers": 42,
        "consensus_score": 84.0,
        "average_time_per_task_seconds": 48.2,
        "quality_tier": "good",
        "base_pay": 760.00,
        "bonus_amount": 152.00,
        "total_payment": 912.00,
        "payment_status": "approved"
      }
      // ... more days
    ],
    "quality_flags": [
      {
        "flag_id": 45,
        "flag_type": "high_speed",
        "severity": "low",
        "description": "Completed tasks 20% faster than average on 2025-11-22",
        "flagged_at": "2025-11-22T18:00:00Z",
        "resolved": true,
        "resolution_notes": "Reviewed - worker efficiency improved, no quality issues"
      }
    ],
    "trends": {
      "consensus_score_trend": "improving",
      "speed_trend": "stable",
      "earnings_trend": "increasing"
    }
  },
  "timestamp": "2025-11-25T14:30:00Z"
}
```

**Use Case:** Worker detail page in DPW App, performance reviews

---

### 6.3 Payment Calculation

#### POST `/api/v1/payments/calculate`

Calculate payments for a specific period. This does NOT approve payments.

**Request:**
```http
POST /api/v1/payments/calculate HTTP/1.1
Host: micro.spatialcollective.co.ke:8080
X-API-Key: dpw_prod_xxxxx
Content-Type: application/json

{
  "period_start": "2025-11-18",
  "period_end": "2025-11-25",
  "question_id": 123,
  "worker_ids": ["W12345", "W67890"],
  "recalculate": false
}
```

**Body Parameters:**
- `period_start` (required): Start date (YYYY-MM-DD)
- `period_end` (required): End date (YYYY-MM-DD)
- `question_id` (optional): Filter by question
- `worker_ids` (optional): Specific workers only
- `recalculate` (optional): Force recalculation (default: false)

**Response:**
```json
{
  "status": "success",
  "data": {
    "calculation_id": "CALC-20251125-001",
    "period": "2025-11-18 to 2025-11-25",
    "total_workers": 25,
    "summary": {
      "total_base_pay": 133000.00,
      "total_bonuses": 25400.00,
      "total_payment": 158400.00,
      "breakdown_by_tier": {
        "excellent": {
          "workers": 12,
          "total": 78000.00,
          "avg_bonus_percentage": 30
        },
        "good": {
          "workers": 8,
          "total": 52800.00,
          "avg_bonus_percentage": 20
        },
        "fair": {
          "workers": 4,
          "total": 21200.00,
          "avg_bonus_percentage": 10
        },
        "poor": {
          "workers": 1,
          "total": 6400.00,
          "avg_bonus_percentage": 0
        }
      }
    },
    "worker_payments": [
      {
        "worker_unique_id": "W12345",
        "phone_number": "+254712345678",
        "full_name": "John Doe",
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
        },
        "payment_status": "calculated"
      }
      // ... more workers
    ]
  },
  "timestamp": "2025-11-25T14:30:00Z"
}
```

**Use Case:** Weekly payment calculation for admin review

---

### 6.4 Payment Approval

#### POST `/api/v1/payments/approve`

Approve calculated payments for processing.

**Request:**
```http
POST /api/v1/payments/approve HTTP/1.1
Host: micro.spatialcollective.co.ke:8080
X-API-Key: dpw_prod_xxxxx
Content-Type: application/json

{
  "calculation_id": "CALC-20251125-001",
  "worker_payment_ids": [1, 2, 3, 4, 5],
  "approved_by": "admin@dpw.com",
  "payment_reference": "DPW-PAY-20251125-001",
  "notes": "Weekly payment batch - approved for M-Pesa processing"
}
```

**Body Parameters:**
- `calculation_id` (required): ID from calculate endpoint
- `worker_payment_ids` (required): Array of `worker_performance` record IDs
- `approved_by` (required): Email of approver
- `payment_reference` (optional): Internal reference number
- `notes` (optional): Approval notes

**Response:**
```json
{
  "status": "success",
  "data": {
    "approval_id": "APPR-20251125-001",
    "total_approved": 5,
    "total_amount": 31920.00,
    "approved_at": "2025-11-25T14:35:00Z",
    "approved_by": "admin@dpw.com",
    "next_steps": [
      "Export payment data using /api/v1/payments/export",
      "Process M-Pesa payments in DPW App",
      "Update payment status using /api/v1/payments/sync-status"
    ]
  },
  "timestamp": "2025-11-25T14:35:00Z"
}
```

**Use Case:** Admin approves payments in DPW App before processing

---

### 6.5 Payment Export

#### GET `/api/v1/payments/export`

Export approved payments for M-Pesa bulk upload or processing.

**Request:**
```http
GET /api/v1/payments/export?period_start=2025-11-18&period_end=2025-11-25&format=csv&payment_status=approved HTTP/1.1
Host: micro.spatialcollective.co.ke:8080
X-API-Key: dpw_prod_xxxxx
```

**Query Parameters:**
- `period_start` (required): YYYY-MM-DD
- `period_end` (required): YYYY-MM-DD
- `format` (required): csv | excel | json
- `payment_status` (optional): pending | approved | paid
- `question_id` (optional): Filter by question

**Response (CSV):**
```csv
Worker ID,Phone Number,Full Name,Days Worked,Tasks Completed,Consensus Score,Base Pay,Bonus,Total Payment,Payment Status,Payment Reference
W12345,+254712345678,John Doe,7,350,87.5,5320.00,1064.00,6384.00,approved,DPW-PAY-20251125-001
W67890,+254723456789,Jane Smith,6,300,92.3,4560.00,1368.00,5928.00,approved,DPW-PAY-20251125-001
```

**Response (JSON):**
```json
{
  "status": "success",
  "data": {
    "export_id": "EXP-20251125-001",
    "period": "2025-11-18 to 2025-11-25",
    "total_workers": 25,
    "total_amount": 158400.00,
    "format": "json",
    "payments": [
      {
        "worker_id": "W12345",
        "phone_number": "+254712345678",
        "full_name": "John Doe",
        "mpesa_format": {
          "phone": "254712345678",
          "amount": 6384.00,
          "account_ref": "W12345",
          "remarks": "Microtask payment Nov 18-25"
        },
        "payment_details": {
          "days_worked": 7,
          "tasks_completed": 350,
          "consensus_score": 87.5,
          "base_pay": 5320.00,
          "bonus": 1064.00,
          "total": 6384.00
        }
      }
      // ... more workers
    ]
  },
  "timestamp": "2025-11-25T14:40:00Z"
}
```

**Use Case:** Download payment file for M-Pesa bulk upload

---

### 6.6 Quality Flags

#### GET `/api/v1/quality/flags`

Retrieve quality flags for review.

**Request:**
```http
GET /api/v1/quality/flags?resolved=false&severity=medium,high&start_date=2025-11-18 HTTP/1.1
Host: micro.spatialcollective.co.ke:8080
X-API-Key: dpw_prod_xxxxx
```

**Query Parameters:**
- `worker_id` (optional): Filter by worker
- `resolved` (optional): true | false
- `severity` (optional): low | medium | high (comma-separated)
- `start_date` (optional): YYYY-MM-DD
- `end_date` (optional): YYYY-MM-DD
- `flag_type` (optional): low_consensus | high_speed | low_quality | manual

**Response:**
```json
{
  "status": "success",
  "data": {
    "total_flags": 15,
    "unresolved": 8,
    "by_severity": {
      "high": 2,
      "medium": 6,
      "low": 7
    },
    "flags": [
      {
        "flag_id": 45,
        "worker_unique_id": "W34567",
        "worker_name": "Alice Johnson",
        "phone_number": "+254734567890",
        "question_id": 123,
        "question_name": "GoPro 360 Amenity Tagging",
        "flag_type": "low_consensus",
        "severity": "high",
        "description": "Worker consensus score below 60% for 3 consecutive days",
        "flagged_at": "2025-11-23T10:00:00Z",
        "days_since_flagged": 2,
        "impact": {
          "tasks_affected": 150,
          "potential_rework": 90
        },
        "resolved": false,
        "assigned_to": null,
        "priority": "urgent"
      },
      {
        "flag_id": 46,
        "worker_unique_id": "W45678",
        "worker_name": "Bob Wilson",
        "phone_number": "+254745678901",
        "question_id": 123,
        "flag_type": "high_speed",
        "severity": "medium",
        "description": "Average task completion time 50% faster than team average",
        "flagged_at": "2025-11-24T14:30:00Z",
        "days_since_flagged": 1,
        "impact": {
          "tasks_affected": 75,
          "quality_check_needed": true
        },
        "resolved": false,
        "assigned_to": "supervisor@dpw.com",
        "priority": "medium"
      }
    ],
    "recommended_actions": [
      {
        "flag_id": 45,
        "action": "Schedule retraining session",
        "urgency": "high"
      },
      {
        "flag_id": 46,
        "action": "Manual quality review of recent tasks",
        "urgency": "medium"
      }
    ]
  },
  "timestamp": "2025-11-25T14:45:00Z"
}
```

**Use Case:** Quality management dashboard in DPW App

---

### 6.7 Resolve Quality Flag

#### POST `/api/v1/quality/resolve-flag`

Mark a quality flag as resolved after investigation.

**Request:**
```http
POST /api/v1/quality/resolve-flag HTTP/1.1
Host: micro.spatialcollective.co.ke:8080
X-API-Key: dpw_prod_xxxxx
Content-Type: application/json

{
  "flag_id": 45,
  "resolved_by": "supervisor@dpw.com",
  "resolution_notes": "Worker retrained on tagging guidelines. Quality improved to 85% consensus. Monitoring will continue for 7 days.",
  "action_taken": "retraining",
  "follow_up_required": true,
  "follow_up_date": "2025-12-02"
}
```

**Body Parameters:**
- `flag_id` (required): ID of the flag to resolve
- `resolved_by` (required): Email of resolver
- `resolution_notes` (required): Explanation of resolution
- `action_taken` (optional): retraining | warning | removal | false_positive
- `follow_up_required` (optional): boolean
- `follow_up_date` (optional): YYYY-MM-DD

**Response:**
```json
{
  "status": "success",
  "data": {
    "flag_id": 45,
    "worker_id": "W34567",
    "resolved": true,
    "resolved_by": "supervisor@dpw.com",
    "resolved_at": "2025-11-25T14:50:00Z",
    "resolution_summary": {
      "action_taken": "retraining",
      "duration_days": 2,
      "follow_up_required": true,
      "follow_up_date": "2025-12-02"
    },
    "worker_status": {
      "active": true,
      "on_probation": true,
      "probation_until": "2025-12-02"
    }
  },
  "timestamp": "2025-11-25T14:50:00Z"
}
```

**Use Case:** Supervisor resolves quality issue in DPW App

---

## 7. Integration Workflows

### Workflow 1: Daily Dashboard Update

**Objective:** Display real-time metrics on DPW App homepage

**Steps:**

1. **Every 5 minutes**, DPW App calls:
   ```
   GET /api/v1/analytics/dashboard
   ```

2. **Parse response** and update dashboard widgets:
   - Active workers count
   - Today's tasks completed
   - Pending payments
   - Active alerts

3. **Display visual indicators:**
   - Green: System healthy
   - Yellow: Warnings exist
   - Red: Critical alerts

**Implementation:**

```javascript
// DPW App - Dashboard Service
async function updateDashboard() {
  try {
    const response = await fetch(
      'https://micro.spatialcollective.co.ke:8080/api/v1/analytics/dashboard',
      {
        headers: {
          'X-API-Key': process.env.MICROTASKING_API_KEY,
          'Content-Type': 'application/json'
        }
      }
    );
    
    const { data } = await response.json();
    
    // Update UI
    updateWidget('active-workers', data.overview.active_workers);
    updateWidget('tasks-today', data.today.tasks_completed);
    updateWidget('pending-payments', formatCurrency(data.today.pending_payments));
    updateWidget('system-status', data.system_health.status);
    
    // Show alerts if any
    if (data.recent_alerts.length > 0) {
      showAlertBadge(data.recent_alerts.length);
    }
    
  } catch (error) {
    console.error('Dashboard update failed:', error);
    showErrorState();
  }
}

// Run every 5 minutes
setInterval(updateDashboard, 5 * 60 * 1000);
```

---

### Workflow 2: Weekly Payment Processing

**Objective:** Calculate, approve, and process weekly payments

**Steps:**

1. **Monday 9 AM**: Admin triggers payment calculation in DPW App

2. **DPW App calls:**
   ```
   POST /api/v1/payments/calculate
   {
     "period_start": "2025-11-18",
     "period_end": "2025-11-25"
   }
   ```

3. **Review calculation results** in DPW App interface

4. **Admin approves** payments:
   ```
   POST /api/v1/payments/approve
   {
     "calculation_id": "CALC-20251125-001",
     "worker_payment_ids": [1, 2, 3, ...],
     "approved_by": "admin@dpw.com"
   }
   ```

5. **Export payment file:**
   ```
   GET /api/v1/payments/export?format=csv&payment_status=approved
   ```

6. **Process M-Pesa payments** in DPW App's payment system

7. **Update payment status** after successful processing:
   ```
   POST /api/v1/payments/sync-status
   {
     "payment_ids": [1, 2, 3, ...],
     "status": "paid",
     "payment_date": "2025-11-25",
     "mpesa_transaction_ids": ["ABC123", "DEF456", ...]
   }
   ```

**Implementation:**

```javascript
// DPW App - Payment Processing Flow
class PaymentProcessor {
  
  async calculateWeeklyPayments(startDate, endDate) {
    const response = await this.apiClient.post('/payments/calculate', {
      period_start: startDate,
      period_end: endDate
    });
    
    // Store calculation ID for approval
    const { calculation_id, worker_payments } = response.data;
    await this.db.saveCalculation(calculation_id, worker_payments);
    
    return { calculation_id, worker_payments };
  }
  
  async approvePayments(calculationId, workerIds, approverEmail) {
    const response = await this.apiClient.post('/payments/approve', {
      calculation_id: calculationId,
      worker_payment_ids: workerIds,
      approved_by: approverEmail,
      payment_reference: this.generateReference()
    });
    
    return response.data.approval_id;
  }
  
  async exportForMpesa(startDate, endDate) {
    const response = await this.apiClient.get('/payments/export', {
      params: {
        period_start: startDate,
        period_end: endDate,
        format: 'json',
        payment_status: 'approved'
      }
    });
    
    // Transform to M-Pesa bulk format
    return response.data.payments.map(p => p.mpesa_format);
  }
  
  async syncPaymentStatus(paymentIds, mpesaResults) {
    await this.apiClient.post('/payments/sync-status', {
      payment_ids: paymentIds,
      status: 'paid',
      payment_date: new Date().toISOString().split('T')[0],
      mpesa_transaction_ids: mpesaResults.map(r => r.transaction_id),
      processing_notes: 'Bulk payment processed successfully'
    });
  }
}
```

---

### Workflow 3: Quality Issue Management

**Objective:** Monitor and resolve worker quality issues

**Steps:**

1. **Daily 6 PM**: Microtasking Platform automatically calculates consensus and flags low performers

2. **DPW App receives webhook** (or polls):
   ```
   POST https://app.spatialcollective.com/api/webhooks/microtasking
   {
     "event": "quality.flag_raised",
     "data": {
       "flag_id": 45,
       "worker_id": "W34567",
       "severity": "high"
     }
   }
   ```

3. **DPW App displays alert** to supervisor

4. **Supervisor reviews flag:**
   ```
   GET /api/v1/quality/flags?flag_id=45
   ```

5. **Supervisor takes action:**
   - Reviews worker's recent tasks
   - Contacts worker for retraining
   - Documents resolution

6. **Supervisor marks as resolved:**
   ```
   POST /api/v1/quality/resolve-flag
   {
     "flag_id": 45,
     "resolved_by": "supervisor@dpw.com",
     "resolution_notes": "...",
     "action_taken": "retraining"
   }
   ```

7. **Worker continues** under monitoring

**Implementation:**

```javascript
// DPW App - Quality Management
class QualityManager {
  
  async handleQualityFlagWebhook(payload) {
    const { flag_id, worker_id, severity } = payload.data;
    
    // Send notification to supervisor
    if (severity === 'high' || severity === 'critical') {
      await this.notificationService.sendUrgent({
        to: ['supervisor@dpw.com'],
        subject: `URGENT: Quality issue for worker ${worker_id}`,
        message: `Flag ${flag_id} raised - immediate action required`
      });
    }
    
    // Log in quality dashboard
    await this.db.createQualityAlert(payload);
  }
  
  async getFlagDetails(flagId) {
    const response = await this.apiClient.get('/quality/flags', {
      params: { flag_id: flagId }
    });
    
    return response.data.flags[0];
  }
  
  async resolveFlag(flagId, resolverEmail, notes, actionTaken) {
    await this.apiClient.post('/quality/resolve-flag', {
      flag_id: flagId,
      resolved_by: resolverEmail,
      resolution_notes: notes,
      action_taken: actionTaken,
      follow_up_required: actionTaken === 'retraining',
      follow_up_date: this.getFollowUpDate(7) // 7 days from now
    });
    
    // Update local database
    await this.db.updateFlagStatus(flagId, 'resolved');
  }
}
```

---

## 8. Webhook Implementation

### Webhooks from Microtasking Platform to DPW App

The Microtasking Platform will send webhooks to DPW App for real-time updates.

### Webhook Endpoint Requirements

**DPW App must implement:**

```
POST https://app.spatialcollective.com/api/webhooks/microtasking
```

### Webhook Events

| Event | Trigger | Payload |
|-------|---------|---------|
| `quality.flag_raised` | Worker flagged for quality issues | `{ flag_id, worker_id, severity }` |
| `payment.calculated` | Payment calculation completed | `{ calculation_id, total_amount }` |
| `payment.approved` | Payments approved for processing | `{ approval_id, worker_count }` |
| `system.health_alert` | System health critical/warning | `{ metric_type, status, value }` |
| `consensus.completed` | Consensus calculation finished | `{ question_id, images_processed }` |

### Webhook Payload Structure

```json
{
  "event": "quality.flag_raised",
  "event_id": "evt_abc123xyz",
  "timestamp": "2025-11-25T14:55:00Z",
  "data": {
    "flag_id": 45,
    "worker_id": "W34567",
    "worker_name": "Alice Johnson",
    "phone_number": "+254734567890",
    "question_id": 123,
    "flag_type": "low_consensus",
    "severity": "high",
    "description": "Worker consensus score below 60% for 3 consecutive days",
    "flagged_at": "2025-11-23T10:00:00Z"
  }
}
```

### Webhook Implementation Example

```javascript
// DPW App - Webhook Handler
const express = require('express');
const crypto = require('crypto');

const app = express();
app.use(express.json());

// Webhook endpoint
app.post('/api/webhooks/microtasking', async (req, res) => {
  try {
    // 1. Verify signature
    const signature = req.headers['x-webhook-signature'];
    const timestamp = req.headers['x-webhook-timestamp'];
    
    if (!verifyWebhookSignature(req.body, signature, timestamp)) {
      return res.status(401).json({ error: 'Invalid signature' });
    }
    
    // 2. Prevent replay attacks (check timestamp)
    const requestTime = parseInt(timestamp);
    const now = Math.floor(Date.now() / 1000);
    if (Math.abs(now - requestTime) > 300) { // 5 minutes
      return res.status(401).json({ error: 'Request too old' });
    }
    
    // 3. Handle event
    const { event, data } = req.body;
    
    switch (event) {
      case 'quality.flag_raised':
        await handleQualityFlag(data);
        break;
      
      case 'payment.calculated':
        await handlePaymentCalculated(data);
        break;
      
      case 'payment.approved':
        await handlePaymentApproved(data);
        break;
      
      case 'system.health_alert':
        await handleHealthAlert(data);
        break;
      
      case 'consensus.completed':
        await handleConsensusCompleted(data);
        break;
      
      default:
        console.warn(`Unknown webhook event: ${event}`);
    }
    
    // 4. Respond quickly (200 OK)
    res.status(200).json({ received: true });
    
  } catch (error) {
    console.error('Webhook processing error:', error);
    res.status(500).json({ error: 'Processing failed' });
  }
});

function verifyWebhookSignature(payload, signature, timestamp) {
  const secret = process.env.MICROTASKING_WEBHOOK_SECRET;
  const payloadString = JSON.stringify(payload) + timestamp;
  const hmac = crypto.createHmac('sha256', secret);
  const digest = 'sha256=' + hmac.update(payloadString).digest('hex');
  
  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(digest)
  );
}

async function handleQualityFlag(data) {
  // Send email/SMS to supervisor
  await notificationService.sendAlert({
    type: 'quality_issue',
    severity: data.severity,
    worker_id: data.worker_id,
    message: data.description
  });
  
  // Create task in DPW App
  await taskService.create({
    title: `Review quality flag for ${data.worker_name}`,
    assignee: 'supervisor@dpw.com',
    priority: data.severity,
    due_date: new Date(Date.now() + 24 * 60 * 60 * 1000) // 24 hours
  });
  
  // Log in database
  await db.qualityFlags.create(data);
}

async function handlePaymentCalculated(data) {
  // Notify admin that payments are ready for review
  await notificationService.sendEmail({
    to: ['admin@dpw.com'],
    subject: 'Weekly payments ready for approval',
    body: `Payment calculation ${data.calculation_id} is complete. Total: KES ${data.total_amount}`
  });
}

async function handleHealthAlert(data) {
  // Critical system alert
  await notificationService.sendUrgent({
    to: ['tech@spatialcollective.com'],
    subject: `CRITICAL: ${data.metric_type} alert`,
    message: `${data.metric_type} is at ${data.value}% - Status: ${data.status}`
  });
}
```

### Webhook Retry Logic

If DPW App is unavailable, Microtasking Platform will retry:

- **Attempt 1:** Immediate
- **Attempt 2:** After 1 minute
- **Attempt 3:** After 5 minutes
- **Attempt 4:** After 30 minutes
- **Attempt 5:** After 1 hour

After 5 failed attempts, alert will be sent to tech team.

---

## 9. Data Schemas

### Worker Performance Schema

```typescript
interface WorkerPerformance {
  id: number;
  worker_unique_id: string;
  question_id: number;
  date: string; // YYYY-MM-DD
  tasks_completed: number;
  correct_answers: number;
  incorrect_answers: number;
  consensus_score: number; // 0-100
  average_time_per_task: number; // seconds
  flagged_tasks: number;
  quality_tier: 'excellent' | 'good' | 'fair' | 'poor';
  base_pay: number;
  bonus_amount: number;
  total_payment: number;
  payment_status: 'pending' | 'approved' | 'paid';
  payment_reference?: string;
  created_at: string;
  updated_at: string;
}
```

### Consensus Result Schema

```typescript
interface ConsensusResult {
  id: number;
  question_id: number;
  image_id: number;
  ground_truth: string; // Final consensus answer
  total_responses: number;
  consensus_percentage: number; // 0-100
  requires_review: boolean;
  review_status?: 'pending' | 'approved' | 'rejected';
  reviewed_by?: string;
  reviewed_at?: string;
  created_at: string;
  updated_at: string;
}
```

### Quality Flag Schema

```typescript
interface QualityFlag {
  id: number;
  worker_unique_id: string;
  question_id: number;
  flag_type: 'low_consensus' | 'high_speed' | 'low_quality' | 'manual';
  severity: 'low' | 'medium' | 'high';
  description: string;
  flagged_at: string;
  resolved: boolean;
  resolved_by?: string;
  resolved_at?: string;
  resolution_notes?: string;
}
```

### Payment Export Schema

```typescript
interface PaymentExport {
  id: number;
  export_date: string;
  period_start: string;
  period_end: string;
  total_workers: number;
  total_amount: number;
  export_format: 'csv' | 'excel' | 'json';
  file_path: string;
  exported_by: string;
  exported_at: string;
  dpw_sync_status: 'pending' | 'synced' | 'failed';
  dpw_sync_at?: string;
  dpw_reference?: string;
}
```

---

## 10. Testing Guide

### Test Environment

**Base URL:** `https://staging.micro.spatialcollective.co.ke:8080/api/v1`  
**API Key:** `dpw_stage_test_key_12345` (request from tech team)

### Test Data Available

- **Test Workers:** W99901 - W99910 (10 test workers)
- **Test Question:** Question ID 999 ("Test Tagging Project")
- **Test Images:** 100 sample images
- **Test Answers:** Pre-populated with various consensus levels

### Test Scenarios

#### Scenario 1: Dashboard Integration

```bash
# Test dashboard endpoint
curl -X GET "https://staging.micro.spatialcollective.co.ke:8080/api/v1/analytics/dashboard" \
  -H "X-API-Key: dpw_stage_test_key_12345"

# Expected: 200 OK with dashboard data
```

#### Scenario 2: Worker Performance

```bash
# Get test worker performance
curl -X GET "https://staging.micro.spatialcollective.co.ke:8080/api/v1/performance/worker/W99901" \
  -H "X-API-Key: dpw_stage_test_key_12345"

# Expected: Worker performance data with daily breakdown
```

#### Scenario 3: Payment Calculation

```bash
# Calculate test payments
curl -X POST "https://staging.micro.spatialcollective.co.ke:8080/api/v1/payments/calculate" \
  -H "X-API-Key: dpw_stage_test_key_12345" \
  -H "Content-Type: application/json" \
  -d '{
    "period_start": "2025-11-18",
    "period_end": "2025-11-25",
    "question_id": 999
  }'

# Expected: Payment calculation with test worker data
```

#### Scenario 4: Webhook Testing

```bash
# Trigger test webhook to your endpoint
curl -X POST "https://staging.micro.spatialcollective.co.ke:8080/api/v1/test/webhook" \
  -H "X-API-Key: dpw_stage_test_key_12345" \
  -H "Content-Type: application/json" \
  -d '{
    "target_url": "https://your-staging-dpw-app.com/api/webhooks/microtasking",
    "event": "quality.flag_raised"
  }'

# This will send a test webhook to your endpoint
```

### Postman Collection

Download test collection:
```
https://micro.spatialcollective.co.ke/api-docs/postman-collection.json
```

Import into Postman and set environment variables:
- `base_url`: `https://staging.micro.spatialcollective.co.ke:8080`
- `api_key`: `dpw_stage_test_key_12345`

---

## 11. Deployment Checklist

### Pre-Deployment

- [ ] Obtain production API key from tech team
- [ ] Configure webhook endpoint in production
- [ ] Set up environment variables (API keys, secrets)
- [ ] Test all API endpoints in staging environment
- [ ] Verify webhook signature validation
- [ ] Set up monitoring/logging for API calls
- [ ] Document API integration in DPW App codebase

### Production Configuration

```env
# DPW App .env file
MICROTASKING_API_URL=https://micro.spatialcollective.co.ke:8080/api/v1
MICROTASKING_API_KEY=dpw_prod_xxxxx_xxxxx_xxxxx
MICROTASKING_WEBHOOK_SECRET=webhook_secret_xxxxx
MICROTASKING_WEBHOOK_URL=https://app.spatialcollective.com/api/webhooks/microtasking
```

### Health Checks

Set up monitoring for:
- API endpoint availability (every 5 minutes)
- Webhook delivery success rate
- API response times
- Error rates

### Rollout Plan

1. **Week 1:** Deploy webhook endpoint (receive only, no processing)
2. **Week 2:** Deploy dashboard integration (read-only)
3. **Week 3:** Deploy worker performance views
4. **Week 4:** Deploy payment processing (test with 5 workers)
5. **Week 5:** Full rollout to all workers

---

## 12. Troubleshooting

### Common Issues

#### Issue: 401 Unauthorized

**Cause:** Invalid or missing API key

**Solution:**
```bash
# Verify API key is correct
echo $MICROTASKING_API_KEY

# Test with curl
curl -v -H "X-API-Key: $MICROTASKING_API_KEY" \
  https://micro.spatialcollective.co.ke:8080/api/v1/health
```

#### Issue: 429 Rate Limit Exceeded

**Cause:** Too many requests

**Solution:**
- Implement request caching
- Add exponential backoff retry logic
- Check rate limit headers in responses
- Contact tech team to increase limits if needed

```javascript
async function apiCallWithRetry(url, options, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      const response = await fetch(url, options);
      
      if (response.status === 429) {
        const retryAfter = response.headers.get('X-RateLimit-Reset');
        const waitTime = (retryAfter - Date.now() / 1000) * 1000;
        await sleep(waitTime);
        continue;
      }
      
      return response;
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await sleep(1000 * Math.pow(2, i)); // Exponential backoff
    }
  }
}
```

#### Issue: Webhook Not Received

**Possible Causes:**
1. Firewall blocking incoming requests
2. Invalid webhook URL
3. Signature verification failing

**Debugging:**
```javascript
// Log all webhook attempts
app.post('/api/webhooks/microtasking', (req, res) => {
  console.log('Webhook received:', {
    headers: req.headers,
    body: req.body,
    timestamp: new Date().toISOString()
  });
  
  // Always respond 200 even if processing fails
  res.status(200).json({ received: true });
});
```

#### Issue: Empty Data Returned

**Cause:** No data for specified parameters

**Solution:**
```javascript
// Always check if data exists before processing
const response = await api.get('/performance/worker/W12345');

if (!response.data.daily_breakdown || response.data.daily_breakdown.length === 0) {
  console.warn('No performance data found for worker W12345');
  // Handle gracefully - show "No data" message
  return;
}
```

### Getting Help

**Technical Support:**
- Email: tech@spatialcollective.com
- Slack: #microtasking-integration
- Phone: +254 XXX XXX XXX (emergencies only)

**Response Times:**
- Critical issues: 2 hours
- High priority: 24 hours
- Medium/Low: 3-5 business days

---

## 13. Support & Contact

### Technical Contacts

**Project Lead:**  
Name: [To be assigned]  
Email: tech@spatialcollective.com  

**API Integration Support:**  
Email: api-support@spatialcollective.com  
Response Time: 24-48 hours  

**Emergency Contact (Critical Issues):**  
Phone: [To be provided]  
Available: 24/7 for production outages  

### Documentation & Resources

- **API Documentation:** https://micro.spatialcollective.co.ke/api-docs
- **GitHub Repository:** https://github.com/SpatialCollectiveLtd/microtasking
- **Technical Specification:** `DPW-INTEGRATION-TECHNICAL-SPECIFICATION.md`
- **Changelog:** https://micro.spatialcollective.co.ke/changelog

### Feedback & Improvements

We welcome feedback on this integration:
- Submit issues: https://github.com/SpatialCollectiveLtd/microtasking/issues
- Feature requests: integration-feedback@spatialcollective.com

---

**Document Version:** 1.0  
**Last Updated:** November 25, 2025  
**Next Review:** December 25, 2025  

---

**End of Integration Guide**
