package com.spatialcollective.microtasktoolapi.model.dto

import java.math.BigDecimal
import java.time.LocalDateTime

// Standard API Response wrapper
data class ApiResponse<T>(
    val status: String,
    val message: String? = null,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

// Consensus DTOs
data class ConsensusCalculationResponse(
    val questionId: Long,
    val totalImages: Int,
    val imagesProcessed: Int,
    val consensusReached: Int,
    val requiresReview: Int,
    val consensusThreshold: BigDecimal,
    val results: List<ConsensusResultDto>
)

data class ConsensusResultDto(
    val imageId: Long,
    val url: String?,
    val groundTruth: String?,
    val votes: Map<String, Int>,
    val consensusPercentage: BigDecimal,
    val requiresReview: Boolean
)

data class ConsensusRecalculateRequest(
    val questionId: Long,
    val imageIds: List<Long>? = null
)

// Performance DTOs
data class WorkerPerformanceResponse(
    val workerInfo: WorkerInfo,
    val period: Period,
    val summary: PerformanceSummary,
    val dailyBreakdown: List<DailyPerformance>,
    val qualityFlags: List<QualityFlagDto>,
    val trends: PerformanceTrends?
)

data class WorkerInfo(
    val workerUniqueId: String,
    val phoneNumber: String?,
    val fullName: String?,
    val registrationDate: String?
)

data class Period(
    val startDate: String,
    val endDate: String
)

data class PerformanceSummary(
    val daysWorked: Int,
    val totalTasks: Int,
    val avgTasksPerDay: Int,
    val averageConsensusScore: BigDecimal,
    val totalEarnings: BigDecimal,
    val qualityTierDistribution: Map<String, Int>
)

data class DailyPerformance(
    val date: String,
    val tasksCompleted: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int?,
    val consensusScore: BigDecimal,
    val averageTimePerTask: BigDecimal?,
    val qualityTier: String,
    val basePay: BigDecimal,
    val bonusAmount: BigDecimal,
    val totalPayment: BigDecimal,
    val paymentStatus: String
)

data class PerformanceTrends(
    val consensusScoreTrend: String,
    val speedTrend: String,
    val earningsTrend: String
)

data class QuestionPerformanceResponse(
    val questionId: Long,
    val questionName: String,
    val totalWorkers: Int,
    val totalImages: Int?,
    val completionRate: Double?,
    val averageConsensusScore: BigDecimal,
    val workersByTier: Map<String, Int>,
    val flaggedWorkers: Int,
    val totalPayable: BigDecimal
)

data class LeaderboardResponse(
    val period: Period,
    val topPerformers: List<LeaderboardEntry>
)

data class LeaderboardEntry(
    val rank: Int,
    val workerId: String,
    val workerName: String?,
    val averageConsensusScore: BigDecimal,
    val totalTasks: Int,
    val totalEarnings: BigDecimal,
    val daysWorked: Int
)

// Payment DTOs
data class PaymentCalculationRequest(
    val startDate: String,
    val endDate: String,
    val questionId: Long? = null,
    val workerIds: List<String>? = null,
    val recalculate: Boolean = false
)

data class PaymentCalculationResponse(
    val period: Period,
    val questionId: Long,
    val questionName: String?,
    val workerPayments: List<WorkerPayment>,
    val summary: PaymentSummary,
    val calculatedAt: LocalDateTime,
    val calculatedBy: String
)

data class PaymentSummary(
    val totalWorkers: Int,
    val totalDaysWorked: Int?,
    val totalBasePay: BigDecimal,
    val totalBonuses: BigDecimal,
    val totalPayment: BigDecimal,
    val totalPayable: BigDecimal,
    val breakdownByTier: Map<String, TierPayment>,
    val flaggedWorkers: Int
)

data class TierPayment(
    val workers: Int,
    val total: BigDecimal,
    val avgBonusPercentage: Int
)

data class WorkerPayment(
    val workerUniqueId: String,
    val phoneNumber: String?,
    val fullName: String?,
    val daysWorked: Int,
    val totalTasks: Int,
    val averageConsensusScore: BigDecimal,
    val basePay: BigDecimal,
    val bonusAmount: BigDecimal,
    val totalPayment: BigDecimal,
    val paymentTierBreakdown: Map<String, Int>,
    val paymentStatus: String,
    val qualityFlags: Int
)

data class PaymentApprovalRequest(
    val workerIds: List<String>,
    val questionId: Long,
    val startDate: String,
    val endDate: String,
    val approvedBy: String,
    val notes: String?
)

data class PaymentApprovalResponse(
    val approvedCount: Int,
    val approvedWorkers: List<String>,
    val failedWorkers: List<String>,
    val totalAmount: BigDecimal,
    val approvedBy: String,
    val approvedAt: LocalDateTime,
    val exportId: Long?
)

data class PaymentSyncStatusRequest(
    val exportId: Long,
    val syncStatus: String,
    val syncedRecords: Int?,
    val failedRecords: Int?,
    val errorMessage: String?
)

// Quality DTOs
data class QualityFlagsResponse(
    val totalFlags: Int,
    val unresolved: Int,
    val bySeverity: Map<String, Int>,
    val flags: List<QualityFlagDto>
)

data class QualityFlagDto(
    val flagId: Long,
    val workerUniqueId: String,
    val workerName: String?,
    val phoneNumber: String?,
    val questionId: Long,
    val questionName: String?,
    val flagType: String,
    val severity: String,
    val description: String,
    val flaggedAt: LocalDateTime,
    val daysSinceFlagged: Long?,
    val impact: FlagImpact?,
    val resolved: Boolean,
    val assignedTo: String?,
    val priority: String?
)

data class FlagImpact(
    val tasksAffected: Int,
    val potentialRework: Int?,
    val qualityCheckNeeded: Boolean?
)

data class ResolveFlagRequest(
    val flagId: Long,
    val resolvedBy: String,
    val resolutionNotes: String,
    val actionTaken: String? = null,
    val followUpRequired: Boolean = false,
    val followUpDate: String? = null
)

// Analytics DTOs
data class DashboardResponse(
    val overview: DashboardOverview,
    val today: DailyStats,
    val thisWeek: WeeklyStats,
    val systemHealth: SystemHealthStatus?,
    val recentAlerts: List<AlertDto>,
    val paymentSummary: PaymentSummaryStats
)

data class DashboardOverview(
    val activeQuestions: Int,
    val activeWorkers: Int,
    val totalWorkersRegistered: Int?
)

data class DailyStats(
    val tasksCompleted: Int,
    val averageConsensusScore: BigDecimal,
    val pendingPayments: BigDecimal,
    val flaggedWorkers: Int
)

data class WeeklyStats(
    val tasksCompleted: Int,
    val averageConsensusScore: BigDecimal,
    val totalPayments: BigDecimal,
    val totalWorkers: Int
)

data class SystemHealthStatus(
    val status: String,
    val uptimePercentage: Double,
    val avgResponseTimeMs: Int,
    val errorRatePercentage: Double
)

data class AlertDto(
    val type: String,
    val severity: String,
    val message: String,
    val timestamp: LocalDateTime,
    val resolved: Boolean
)

data class PaymentSummaryStats(
    val pendingApproval: BigDecimal,
    val approvedNotPaid: BigDecimal,
    val paidThisMonth: BigDecimal
)

data class TrendsResponse(
    val period: Period,
    val dailyStats: List<DailyTrendStats>
)

data class DailyTrendStats(
    val date: String,
    val workers: Int,
    val tasks: Int,
    val avgConsensusScore: BigDecimal,
    val totalPayment: BigDecimal
)

// Additional Payment DTOs
data class PaymentExportResponse(
    val exportId: Long,
    val fileName: String,
    val filePath: String,
    val recordCount: Int,
    val totalAmount: BigDecimal,
    val generatedAt: LocalDateTime,
    val generatedBy: String,
    val downloadUrl: String
)

data class PaymentSyncStatusResponse(
    val paymentIds: List<Long>,
    val status: String,
    val syncedRecords: Int,
    val failedRecords: Int,
    val transactionIds: List<String>?,
    val updatedAt: LocalDateTime,
    val message: String
)

// Additional Quality DTOs
data class ResolveFlagResponse(
    val flagId: Long,
    val resolved: Boolean,
    val resolvedBy: String,
    val resolvedAt: LocalDateTime,
    val resolutionNotes: String,
    val actionTaken: String?
)

data class CreateFlagRequest(
    val workerId: String,
    val questionId: Long,
    val flagType: String,
    val severity: String,
    val description: String,
    val flaggedBy: String
)

data class CreateFlagResponse(
    val flagId: Long,
    val workerId: String,
    val questionId: Long,
    val flagType: String,
    val severity: String,
    val createdAt: LocalDateTime,
    val flaggedBy: String,
    val message: String
)

data class QualityStatisticsResponse(
    val totalFlags: Int,
    val unresolvedFlags: Int,
    val affectedWorkers: Int,
    val bySeverity: Map<String, Int>,
    val byType: Map<String, Int>,
    val recentTrends: Map<String, Any>,
    val topOffenders: List<Map<String, Any>>
)
