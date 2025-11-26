# DPW App - Microtasking Platform Integration Guide

**Version:** 1.0  
**Last Updated:** November 26, 2025  
**API Base URL:** `http://micro.spatialcollective.co.ke:8080`

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
4. [Data Models](#data-models)
5. [Integration Flows](#integration-flows)
6. [Error Handling](#error-handling)
7. [Rate Limiting](#rate-limiting)
8. [Testing](#testing)

---

## Overview

The Microtasking Platform API provides real-time monitoring, payment calculation, and quality control for microtask workers. This integration enables the DPW App to:

- **Monitor worker performance** - Track consensus scores, task completion, and quality metrics
- **Calculate payments** - Automated payment calculation with tiered bonuses
- **Manage quality flags** - Create, review, and resolve quality issues
- **View analytics** - Dashboard data and performance trends
- **Export payment data** - Generate Excel exports for payment processing

### Architecture

```
DPW App (Flutter) → API Gateway → Microtasking Platform (Spring Boot)
                                            ↓
                                      MySQL Database
```

---

## Authentication

### API Key Authentication

All API requests require an API key in the request header.

**Header:**
```http
X-API-Key: dpw_prod_microtask_2025_key1
```

**Available Keys:**
- **Production:** `dpw_prod_microtask_2025_key1`
- **Staging:** `dpw_stage_microtask_2025_key2`

**Example Request:**
```bash
curl -X GET "http://micro.spatialcollective.co.ke:8080/api/v1/analytics/dashboard" \
  -H "X-API-Key: dpw_prod_microtask_2025_key1" \
  -H "Content-Type: application/json"
```

---

## API Endpoints

### 1. Analytics & Dashboard

#### GET `/api/v1/analytics/dashboard`
Get overview dashboard metrics.

**Parameters:**
- `questionId` (optional): Filter by specific question
- `period` (optional): Time period (daily, weekly, monthly)

**Response:**
```json
{
  "status": "success",
  "message": "Dashboard data retrieved",
  "data": {
    "overview": {
      "active_questions": 15,
      "total_workers": 234,
      "tasks_today": 1250,
      "consensus_rate": 87.5
    },
    "recent_activity": [...],
    "alerts": [...]
  }
}
```

**UI Components:**
- Summary cards (active questions, workers, tasks)
- Consensus rate chart
- Recent activity feed
- Alert notifications

---

#### GET `/api/v1/analytics/trends`
Get performance trends over time.

**Parameters:**
- `questionId` (optional): Filter by question
- `startDate` (required): Start date (YYYY-MM-DD)
- `endDate` (required): End date (YYYY-MM-DD)
- `metric` (optional): Specific metric (consensus, speed, quality)

**Response:**
```json
{
  "status": "success",
  "data": {
    "trends": {
      "consensus": [
        {"date": "2025-01-01", "value": 85.2},
        {"date": "2025-01-02", "value": 87.1}
      ],
      "task_completion": [...],
      "quality_score": [...]
    }
  }
}
```

**UI Components:**
- Line charts for trends
- Date range picker
- Metric selector dropdown

---

### 2. Payment Management

#### POST `/api/v1/payment/calculate`
Calculate payments for a period.

**Request Body:**
```json
{
  "questionId": 1,
  "startDate": "2025-01-01",
  "endDate": "2025-01-31"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Payments calculated successfully for 450 worker-days",
  "data": {
    "period": {
      "startDate": "2025-01-01",
      "endDate": "2025-01-31"
    },
    "questionId": 1,
    "questionName": "Land Classification Task",
    "workerPayments": [
      {
        "workerUniqueId": "254712345678",
        "phoneNumber": "+254712345678",
        "fullName": "John Doe",
        "daysWorked": 20,
        "totalTasks": 450,
        "averageConsensusScore": 92.5,
        "basePay": 4000.00,
        "bonusAmount": 1200.00,
        "totalPayment": 5200.00,
        "paymentTierBreakdown": {
          "excellent": 15,
          "good": 5
        },
        "paymentStatus": "pending",
        "qualityFlags": 0
      }
    ],
    "summary": {
      "totalWorkers": 45,
      "totalDaysWorked": 450,
      "totalBasePay": 90000.00,
      "totalBonuses": 27000.00,
      "totalPayment": 117000.00,
      "totalPayable": 117000.00,
      "breakdownByTier": {
        "excellent": {
          "workers": 15,
          "total": 78000.00,
          "avgBonusPercentage": 30
        },
        "good": {
          "workers": 20,
          "total": 32000.00,
          "avgBonusPercentage": 20
        },
        "fair": {
          "workers": 8,
          "total": 6400.00,
          "avgBonusPercentage": 10
        },
        "poor": {
          "workers": 2,
          "total": 600.00,
          "avgBonusPercentage": 0
        }
      },
      "flaggedWorkers": 2
    },
    "calculatedAt": "2025-01-31T10:30:00",
    "calculatedBy": "System"
  }
}
```

**UI Components:**
- Date range selector
- Question dropdown
- Worker payment list (table/cards)
- Summary section with totals
- Tier breakdown chart (pie/bar chart)
- Export button

---

#### POST `/api/v1/payment/approve`
Approve calculated payments.

**Request Body:**
```json
{
  "workerIds": ["254712345678", "254787654321"],
  "questionId": 1,
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "approvedBy": "admin@dpw.com",
  "notes": "January payments approved"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Approved 45 payments",
  "data": {
    "approvedCount": 45,
    "approvedWorkers": ["254712345678", ...],
    "failedWorkers": [],
    "totalAmount": 117000.00,
    "approvedBy": "admin@dpw.com",
    "approvedAt": "2025-01-31T11:00:00",
    "exportId": 1234567890
  }
}
```

**UI Components:**
- Multi-select worker list
- Approval button
- Confirmation dialog
- Notes input field
- Success notification

---

#### GET `/api/v1/payment/export`
Export payments to Excel.

**Parameters:**
- `questionId` (required): Question ID
- `startDate` (required): Start date (YYYY-MM-DD)
- `endDate` (required): End date (YYYY-MM-DD)

**Response:**
```json
{
  "status": "success",
  "message": "Export generated with 450 records",
  "data": {
    "exportId": 1234567890,
    "fileName": "payments_1_2025-01-01_2025-01-31.xlsx",
    "filePath": "/tmp/payments_export.xlsx",
    "recordCount": 450,
    "totalAmount": 117000.00,
    "generatedAt": "2025-01-31T11:05:00",
    "generatedBy": "System",
    "downloadUrl": "/api/v1/payment/download/1234567890"
  }
}
```

**UI Components:**
- Export button
- Download progress indicator
- File download link
- Export history list

---

#### POST `/api/v1/payment/sync-status`
Update sync status after payment processing.

**Request Body:**
```json
{
  "exportId": 1234567890,
  "syncStatus": "completed",
  "syncedRecords": 450,
  "failedRecords": 0,
  "errorMessage": null
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Sync status updated",
  "data": {
    "paymentIds": [1, 2, 3],
    "status": "completed",
    "syncedRecords": 450,
    "failedRecords": 0,
    "transactionIds": [],
    "updatedAt": "2025-01-31T12:00:00",
    "message": "Sync status updated successfully"
  }
}
```

**UI Components:**
- Sync status indicator
- Progress bar
- Error list (if any)
- Retry button

---

### 3. Quality Management

#### GET `/api/v1/quality/flags`
Get quality flags with filtering.

**Parameters:**
- `workerId` (optional): Filter by worker ID
- `questionId` (optional): Filter by question
- `severity` (optional): Filter by severity (low, medium, high)
- `resolved` (optional): Filter by resolution status (true/false)

**Response:**
```json
{
  "status": "success",
  "message": "Retrieved 12 quality flags",
  "data": {
    "totalFlags": 12,
    "unresolved": 5,
    "bySeverity": {
      "high": 2,
      "medium": 6,
      "low": 4
    },
    "flags": [
      {
        "flagId": 101,
        "workerUniqueId": "254712345678",
        "workerName": "John Doe",
        "phoneNumber": "+254712345678",
        "questionId": 1,
        "questionName": "Land Classification",
        "flagType": "low_performance",
        "severity": "high",
        "description": "Consensus score below 50% for 3 consecutive days",
        "flaggedAt": "2025-01-25T09:00:00",
        "daysSinceFlagged": 6,
        "impact": {
          "tasksAffected": 45,
          "potentialRework": 23,
          "qualityCheckNeeded": true
        },
        "resolved": false,
        "assignedTo": "supervisor@dpw.com",
        "priority": "high"
      }
    ]
  }
}
```

**UI Components:**
- Flag list (cards/table)
- Filter controls (severity, status, worker)
- Search by worker ID
- Flag details modal
- Resolution form

---

#### POST `/api/v1/quality/resolve-flag`
Resolve a quality flag.

**Request Body:**
```json
{
  "flagId": 101,
  "resolvedBy": "supervisor@dpw.com",
  "resolutionNotes": "Provided additional training on classification criteria",
  "actionTaken": "training",
  "followUpRequired": false,
  "followUpDate": null
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Quality flag resolved successfully",
  "data": {
    "flagId": 101,
    "resolved": true,
    "resolvedBy": "supervisor@dpw.com",
    "resolvedAt": "2025-01-31T14:00:00",
    "resolutionNotes": "Provided additional training on classification criteria",
    "actionTaken": "training"
  }
}
```

**UI Components:**
- Resolution form
- Action taken dropdown
- Notes textarea
- Follow-up date picker
- Submit button

---

#### POST `/api/v1/quality/create-flag`
Create a manual quality flag.

**Request Body:**
```json
{
  "workerId": "254712345678",
  "questionId": 1,
  "flagType": "manual",
  "severity": "medium",
  "description": "Inconsistent responses on boundary classifications",
  "flaggedBy": "supervisor@dpw.com"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Quality flag created successfully",
  "data": {
    "flagId": 102,
    "workerId": "254712345678",
    "questionId": 1,
    "flagType": "manual",
    "severity": "medium",
    "createdAt": "2025-01-31T15:00:00",
    "flaggedBy": "supervisor@dpw.com",
    "message": "Quality flag created successfully"
  }
}
```

**UI Components:**
- Create flag form
- Worker ID input/search
- Question selector
- Severity dropdown
- Description textarea

---

#### GET `/api/v1/quality/statistics`
Get quality statistics.

**Parameters:**
- `questionId` (optional): Filter by question
- `period` (optional): Time period

**Response:**
```json
{
  "status": "success",
  "message": "Quality statistics retrieved successfully",
  "data": {
    "totalFlags": 45,
    "unresolvedFlags": 12,
    "affectedWorkers": 23,
    "bySeverity": {
      "high": 8,
      "medium": 22,
      "low": 15
    },
    "byType": {
      "low_performance": 20,
      "anomalous_speed": 10,
      "manual": 15
    },
    "recentTrends": {},
    "topOffenders": []
  }
}
```

**UI Components:**
- Statistics cards
- Severity distribution chart
- Type distribution chart
- Top offenders list

---

### 4. Performance Monitoring

#### GET `/api/v1/performance/consensus`
Get consensus calculation results.

**Parameters:**
- `questionId` (required): Question ID
- `date` (optional): Specific date (YYYY-MM-DD)
- `workerId` (optional): Filter by worker

**Response:**
```json
{
  "status": "success",
  "data": {
    "question": {
      "questionId": 1,
      "questionName": "Land Classification"
    },
    "date": "2025-01-31",
    "results": [
      {
        "taskId": 1001,
        "consensusAnswer": "forest",
        "agreementScore": 87.5,
        "workerResponses": [
          {
            "workerId": "254712345678",
            "answer": "forest",
            "timestamp": "2025-01-31T10:00:00"
          }
        ],
        "status": "consensus_reached"
      }
    ],
    "summary": {
      "totalTasks": 150,
      "consensusReached": 135,
      "noConsensus": 15,
      "averageAgreement": 85.3
    }
  }
}
```

**UI Components:**
- Consensus results table
- Agreement score chart
- Task details modal
- Worker response comparison

---

#### GET `/api/v1/performance/workers`
Get worker performance metrics.

**Parameters:**
- `questionId` (optional): Filter by question
- `startDate` (optional): Start date
- `endDate` (optional): End date
- `sortBy` (optional): Sort field (consensus, speed, tasks)

**Response:**
```json
{
  "status": "success",
  "data": {
    "workers": [
      {
        "workerId": "254712345678",
        "workerName": "John Doe",
        "phoneNumber": "+254712345678",
        "totalTasks": 450,
        "correctAnswers": 416,
        "incorrectAnswers": 34,
        "consensusScore": 92.4,
        "averageSpeed": 45.2,
        "qualityTier": "excellent",
        "flagCount": 0,
        "lastActive": "2025-01-31T16:00:00"
      }
    ],
    "summary": {
      "totalWorkers": 45,
      "averageConsensus": 85.2,
      "topPerformers": 15,
      "needsImprovement": 5
    }
  }
}
```

**UI Components:**
- Worker leaderboard/table
- Performance cards
- Tier distribution chart
- Worker detail page

---

### 5. Consensus Management

#### GET `/api/v1/consensus/results`
Get consensus results.

**Parameters:**
- `questionId` (required): Question ID
- `date` (optional): Specific date
- `status` (optional): Filter by status

**Response:**
```json
{
  "status": "success",
  "data": {
    "results": [...],
    "summary": {
      "totalTasks": 150,
      "consensusRate": 90.0,
      "pendingTasks": 15
    }
  }
}
```

---

## Data Models

### Worker Performance
```typescript
interface WorkerPerformance {
  workerId: string;
  workerName?: string;
  phoneNumber?: string;
  totalTasks: number;
  correctAnswers: number;
  incorrectAnswers: number;
  consensusScore: number;
  averageSpeed: number;
  qualityTier: 'excellent' | 'good' | 'fair' | 'poor';
  flagCount: number;
  lastActive: string;
}
```

### Payment Calculation
```typescript
interface PaymentCalculation {
  period: {
    startDate: string;
    endDate: string;
  };
  questionId: number;
  questionName?: string;
  workerPayments: WorkerPayment[];
  summary: PaymentSummary;
  calculatedAt: string;
  calculatedBy: string;
}

interface WorkerPayment {
  workerUniqueId: string;
  phoneNumber?: string;
  fullName?: string;
  daysWorked: number;
  totalTasks: number;
  averageConsensusScore: number;
  basePay: number;
  bonusAmount: number;
  totalPayment: number;
  paymentTierBreakdown: Record<string, number>;
  paymentStatus: string;
  qualityFlags: number;
}
```

### Quality Flag
```typescript
interface QualityFlag {
  flagId: number;
  workerUniqueId: string;
  workerName?: string;
  phoneNumber?: string;
  questionId: number;
  questionName?: string;
  flagType: string;
  severity: 'low' | 'medium' | 'high';
  description: string;
  flaggedAt: string;
  daysSinceFlagged?: number;
  impact?: {
    tasksAffected: number;
    potentialRework?: number;
    qualityCheckNeeded?: boolean;
  };
  resolved: boolean;
  assignedTo?: string;
  priority?: string;
}
```

---

## Integration Flows

### 1. Daily Payment Calculation Flow

```
DPW App                          Microtasking API
   |                                    |
   |-- POST /payment/calculate -------->|
   |    (questionId, date range)        |
   |                                    |
   |<-- Payment calculation result -----|
   |    (worker payments, summary)      |
   |                                    |
   |-- Display payments to admin ------>|
   |                                    |
   |-- POST /payment/approve ---------->|
   |    (selected worker IDs)           |
   |                                    |
   |<-- Approval confirmation ----------|
   |    (exportId, approved count)      |
   |                                    |
   |-- GET /payment/export ------------>|
   |                                    |
   |<-- Excel file download URL --------|
   |                                    |
   |-- Download Excel file ------------->|
   |                                    |
   |-- Process payments in DPW -------->|
   |                                    |
   |-- POST /payment/sync-status ------>|
   |    (success/failure status)        |
   |                                    |
   |<-- Confirmation -------------------|
```

### 2. Quality Monitoring Flow

```
DPW App                          Microtasking API
   |                                    |
   |-- GET /quality/flags ------------->|
   |    (questionId, severity)          |
   |                                    |
   |<-- Quality flags list -------------|
   |                                    |
   |-- Display flags to supervisor ---->|
   |                                    |
   |-- POST /quality/resolve-flag ----->|
   |    (flagId, resolution notes)      |
   |                                    |
   |<-- Resolution confirmation --------|
```

### 3. Dashboard Flow

```
DPW App                          Microtasking API
   |                                    |
   |-- GET /analytics/dashboard ------->|
   |                                    |
   |<-- Dashboard metrics --------------|
   |    (active workers, tasks, etc)    |
   |                                    |
   |-- Display on home screen --------->|
   |                                    |
   |-- GET /analytics/trends ---------->|
   |    (date range, metric)            |
   |                                    |
   |<-- Trend data --------------------|
   |                                    |
   |-- Display charts ---------------->|
```

---

## Error Handling

### Standard Error Response

```json
{
  "status": "error",
  "message": "Detailed error message",
  "timestamp": "2025-01-31T10:00:00"
}
```

### HTTP Status Codes

- `200` - Success
- `400` - Bad Request (invalid parameters)
- `401` - Unauthorized (missing/invalid API key)
- `404` - Not Found (resource doesn't exist)
- `429` - Too Many Requests (rate limit exceeded)
- `500` - Internal Server Error

### Error Handling in DPW App

```dart
try {
  final response = await http.post(
    Uri.parse('$baseUrl/api/v1/payment/calculate'),
    headers: {
      'X-API-Key': apiKey,
      'Content-Type': 'application/json',
    },
    body: jsonEncode(requestBody),
  );

  if (response.statusCode == 200) {
    final data = jsonDecode(response.body);
    // Handle success
  } else if (response.statusCode == 401) {
    // Handle authentication error
    showError('Authentication failed. Please check API key.');
  } else if (response.statusCode == 429) {
    // Handle rate limit
    showError('Too many requests. Please try again later.');
  } else {
    // Handle other errors
    final error = jsonDecode(response.body);
    showError(error['message']);
  }
} catch (e) {
  // Handle network errors
  showError('Network error: ${e.toString()}');
}
```

---

## Rate Limiting

**Limits:**
- **Per Minute:** 60 requests
- **Per Hour:** 1000 requests

**Headers:**
```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1643645400
```

**Best Practices:**
- Cache dashboard data (refresh every 30 seconds)
- Batch payment approvals
- Implement exponential backoff on errors
- Use polling intervals > 1 second

---

## Testing

### Test Credentials

**API Base URL (Staging):** `http://micro.spatialcollective.co.ke:8080`  
**API Key:** `dpw_stage_microtask_2025_key2`

### Sample Test Data

**Test Question ID:** `1` (Land Classification Task)  
**Test Worker IDs:** `254712345678`, `254787654321`  
**Test Date Range:** `2025-01-01` to `2025-01-31`

### Postman Collection

A Postman collection is available for testing. Contact the backend team for access.

### Test Checklist

- [ ] Authenticate with API key
- [ ] Fetch dashboard metrics
- [ ] Calculate payments for test period
- [ ] Approve test payments
- [ ] Export payment Excel file
- [ ] Create quality flag
- [ ] Resolve quality flag
- [ ] View worker performance
- [ ] Check consensus results
- [ ] Handle error scenarios

---

## UI/UX Recommendations

### 1. Dashboard Screen
**Components:**
- Overview cards (workers, tasks, consensus rate)
- Today's activity chart
- Recent alerts list
- Quick actions (calculate payments, view flags)

### 2. Payment Management Screen
**Components:**
- Period selector
- Question dropdown
- Calculate button
- Payment table/list with:
  - Worker info (name, phone, ID)
  - Days worked
  - Tasks completed
  - Consensus score
  - Payment amount (base + bonus)
  - Status badge
- Summary section
- Tier breakdown chart
- Approve/Export buttons

### 3. Quality Flags Screen
**Components:**
- Filter panel (severity, status, worker)
- Flag cards/list with:
  - Worker info
  - Flag type and severity
  - Description
  - Days since flagged
  - Impact metrics
  - Action buttons
- Flag detail modal
- Resolution form
- Statistics panel

### 4. Worker Performance Screen
**Components:**
- Search/filter bar
- Worker leaderboard
- Performance cards with:
  - Consensus score
  - Tasks completed
  - Quality tier
  - Flags count
- Detail view with charts
- Export worker report

### 5. Analytics Screen
**Components:**
- Metric selector
- Date range picker
- Trend charts (line/area)
- Comparison views
- Export chart data

---

## Support & Contact

**Backend Team:**
- **Email:** tech@spatialcollective.com
- **Slack:** #microtasking-api

**Documentation:**
- **API Docs:** http://micro.spatialcollective.co.ke:8080/swagger-ui.html
- **GitHub:** https://github.com/SpatialCollectiveLtd/microtasking

**Issue Reporting:**
- Create issues in GitHub repository
- Tag with `dpw-integration` label

---

## Changelog

**Version 1.0** (November 26, 2025)
- Initial release
- All core endpoints implemented
- Payment calculation and approval
- Quality flag management
- Analytics and dashboard
- Performance monitoring

---

## Next Steps

1. **Review this document** with your development team
2. **Set up staging environment** access
3. **Test API endpoints** using provided credentials
4. **Design UI screens** based on data models
5. **Implement authentication** and error handling
6. **Build payment flow** (highest priority)
7. **Implement quality monitoring** (medium priority)
8. **Add analytics dashboards** (lower priority)
9. **Schedule integration testing** with backend team
10. **Plan production deployment**

---

**Questions?** Contact the backend team for clarification or additional endpoints.
