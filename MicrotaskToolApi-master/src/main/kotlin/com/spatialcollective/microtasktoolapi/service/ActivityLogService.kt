package com.spatialcollective.microtasktoolapi.service

import com.spatialcollective.microtasktoolapi.model.entity.ActivityLogEntity
import com.spatialcollective.microtasktoolapi.repository.ActivityLogRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime

@Service
class ActivityLogService {

    @Autowired
    private lateinit var activityLogRepository: ActivityLogRepository

    private val logger = LoggerFactory.getLogger(ActivityLogService::class.java)
    private val objectMapper = ObjectMapper()

    /**
     * Log an API call
     */
    fun logApiCall(
        action: String,
        userId: String?,
        details: String,
        metadata: Map<String, Any>? = null,
        ipAddress: String? = null
    ) {
        try {
            val metadataJson = metadata?.let { objectMapper.writeValueAsString(it) }
            
            val log = ActivityLogEntity(
                action = action,
                userId = userId,
                metadata = metadataJson,
                ipAddress = ipAddress
            )
            log.timestamp = LocalDateTime.now()
            
            activityLogRepository.save(log)
            logger.debug("Activity logged: $action by $userId")
        } catch (e: Exception) {
            logger.error("Failed to log activity", e)
        }
    }

    /**
     * Log consensus calculation
     */
    fun logConsensusCalculation(questionId: Long, consensusPercentage: Double, groundTruth: String, userId: String? = null) {
        logApiCall(
            action = "CONSENSUS_CALCULATED",
            userId = userId ?: "SYSTEM",
            details = "Consensus calculated for question $questionId: ${String.format("%.2f", consensusPercentage)}%",
            metadata = mapOf(
                "question_id" to questionId,
                "consensus_percentage" to consensusPercentage,
                "ground_truth" to groundTruth
            )
        )
    }

    /**
     * Log payment calculation
     */
    fun logPaymentCalculation(questionId: Long, totalWorkers: Int, totalAmount: Double, userId: String) {
        logApiCall(
            action = "PAYMENT_CALCULATED",
            userId = userId,
            details = "Payments calculated for $totalWorkers workers, total: KSH ${String.format("%.2f", totalAmount)}",
            metadata = mapOf(
                "question_id" to questionId,
                "total_workers" to totalWorkers,
                "total_amount" to totalAmount
            )
        )
    }

    /**
     * Log payment approval
     */
    fun logPaymentApproval(questionId: Long, approvedWorkers: List<String>, totalAmount: Double, approvedBy: String) {
        logApiCall(
            action = "PAYMENT_APPROVED",
            userId = approvedBy,
            details = "Payments approved for ${approvedWorkers.size} workers, total: KSH ${String.format("%.2f", totalAmount)}",
            metadata = mapOf(
                "question_id" to questionId,
                "approved_count" to approvedWorkers.size,
                "total_amount" to totalAmount,
                "worker_ids" to approvedWorkers
            )
        )
    }

    /**
     * Log quality flag creation
     */
    fun logQualityFlag(workerId: String, questionId: Long, flagType: String, severity: String, createdBy: String) {
        logApiCall(
            action = "QUALITY_FLAG_CREATED",
            userId = createdBy,
            details = "Quality flag created for worker $workerId: $flagType ($severity)",
            metadata = mapOf(
                "worker_id" to workerId,
                "question_id" to questionId,
                "flag_type" to flagType,
                "severity" to severity
            )
        )
    }

    /**
     * Log quality flag resolution
     */
    fun logQualityFlagResolution(flagId: Long, resolution: String, resolvedBy: String) {
        logApiCall(
            action = "QUALITY_FLAG_RESOLVED",
            userId = resolvedBy,
            details = "Quality flag $flagId resolved: $resolution",
            metadata = mapOf(
                "flag_id" to flagId,
                "resolution" to resolution
            )
        )
    }

    /**
     * Log payment export
     */
    fun logPaymentExport(questionId: Long, recordCount: Int, fileName: String, exportedBy: String) {
        logApiCall(
            action = "PAYMENT_EXPORTED",
            userId = exportedBy,
            details = "Payment export generated: $recordCount records to $fileName",
            metadata = mapOf(
                "question_id" to questionId,
                "record_count" to recordCount,
                "file_name" to fileName
            )
        )
    }

    /**
     * Log DPW sync event
     */
    fun logDpwSync(exportId: Long, syncStatus: String, syncedRecords: Int, failedRecords: Int) {
        logApiCall(
            action = "DPW_SYNC",
            userId = "DPW_SYSTEM",
            details = "DPW sync $syncStatus: $syncedRecords successful, $failedRecords failed",
            metadata = mapOf(
                "export_id" to exportId,
                "sync_status" to syncStatus,
                "synced_records" to syncedRecords,
                "failed_records" to failedRecords
            )
        )
    }

    /**
     * Get activity logs for a user
     */
    fun getUserActivity(userId: String, days: Int = 30): List<ActivityLogEntity> {
        val since = LocalDateTime.now().minusDays(days.toLong())
        return activityLogRepository.findByUserIdAndTimestampAfterOrderByTimestampDesc(userId, since)
    }

    /**
     * Get activity logs for an action type
     */
    fun getActionActivity(action: String, days: Int = 30): List<ActivityLogEntity> {
        val since = LocalDateTime.now().minusDays(days.toLong())
        return activityLogRepository.findByActionAndTimestampAfterOrderByTimestampDesc(action, since)
    }

    /**
     * Get recent activity logs
     */
    fun getRecentActivity(limit: Int = 100): List<ActivityLogEntity> {
        return activityLogRepository.findTop100ByOrderByTimestampDesc()
    }

    /**
     * Get activity statistics
     */
    fun getActivityStatistics(days: Int = 7): Map<String, Any> {
        val since = LocalDateTime.now().minusDays(days.toLong())
        val logs = activityLogRepository.findByTimestampAfterOrderByTimestampDesc(since)
        
        val actionCounts = logs.groupingBy { it.action ?: "UNKNOWN" }.eachCount()
        val userCounts = logs.groupingBy { it.userId ?: "UNKNOWN" }.eachCount()
        
        return mapOf(
            "total_actions" to logs.size,
            "by_action" to actionCounts,
            "by_user" to userCounts,
            "period_days" to days,
            "most_active_action" to (actionCounts.maxByOrNull { it.value }?.key ?: "N/A"),
            "most_active_user" to (userCounts.maxByOrNull { it.value }?.key ?: "N/A")
        )
    }
}
