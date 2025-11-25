package com.spatialcollective.microtasktoolapi.service

import com.spatialcollective.microtasktoolapi.model.entity.QualityFlagEntity
import com.spatialcollective.microtasktoolapi.repository.QualityFlagRepository
import com.spatialcollective.microtasktoolapi.repository.WorkerPerformanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class QualityFlaggingService(
    @Autowired private val qualityFlagRepository: QualityFlagRepository,
    @Autowired private val workerPerformanceRepository: WorkerPerformanceRepository
) {
    
    private val logger = LoggerFactory.getLogger(QualityFlaggingService::class.java)
    
    @Value("\${quality.consensus.fair:70.0}")
    private lateinit var fairThreshold: String
    
    @Value("\${quality.flag.low-consensus-days:3}")
    private var lowConsensusDays: Int = 3
    
    /**
     * Check and flag workers with low consensus scores
     */
    fun flagLowPerformers(questionId: Long): List<QualityFlagEntity> {
        logger.info("Checking for low performers in question $questionId")
        
        val flags = mutableListOf<QualityFlagEntity>()
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(lowConsensusDays.toLong())
        
        // Get all workers for this question
        val workerIds = workerPerformanceRepository.findDistinctWorkerIdsByQuestionId(questionId)
        
        workerIds.forEach { workerId ->
            val recentPerformance = workerPerformanceRepository.findByWorkerUniqueIdAndDateBetween(
                workerId, startDate, endDate
            ).filter { it.questionId == questionId }
            
            if (recentPerformance.size >= lowConsensusDays) {
                // Check if all recent days have low consensus
                val allLow = recentPerformance.all { 
                    it.consensusScore < BigDecimal(fairThreshold) 
                }
                
                if (allLow) {
                    val avgScore = recentPerformance
                        .map { it.consensusScore }
                        .reduce { acc, score -> acc.add(score) }
                        .divide(BigDecimal(recentPerformance.size), 2, java.math.RoundingMode.HALF_UP)
                    
                    // Check if already flagged recently
                    val existingFlags = qualityFlagRepository.findByWorkerUniqueIdAndResolved(workerId, false)
                    val alreadyFlagged = existingFlags.any { 
                        it.flagType == "low_consensus" && 
                        it.flaggedAt.isAfter(LocalDateTime.now().minusDays(7))
                    }
                    
                    if (!alreadyFlagged) {
                        val flag = QualityFlagEntity(
                            workerUniqueId = workerId,
                            questionId = questionId,
                            flagType = "low_consensus",
                            severity = determineSeverity(avgScore),
                            description = "Worker consensus score below ${fairThreshold}% for $lowConsensusDays consecutive days (avg: $avgScore%)",
                            resolved = false
                        )
                        flags.add(qualityFlagRepository.save(flag))
                        logger.warn("Flagged worker $workerId for low consensus: $avgScore%")
                    }
                }
            }
        }
        
        logger.info("Flagged ${flags.size} workers for low performance")
        return flags
    }
    
    /**
     * Flag workers with anomalous speed
     */
    fun flagAnomalousSpeed(questionId: Long): List<QualityFlagEntity> {
        logger.info("Checking for anomalous speed in question $questionId")
        
        val flags = mutableListOf<QualityFlagEntity>()
        val today = LocalDate.now()
        
        val todayPerformance = workerPerformanceRepository.findByQuestionIdAndDate(questionId, today)
        
        if (todayPerformance.isEmpty()) {
            return flags
        }
        
        // Calculate average time per task
        val avgTimes = todayPerformance.mapNotNull { it.averageTimePerTask }
        if (avgTimes.isEmpty()) {
            return flags
        }
        
        val overallAvg = avgTimes.reduce { acc, time -> acc.add(time) }
            .divide(BigDecimal(avgTimes.size), 2, java.math.RoundingMode.HALF_UP)
        
        // Flag workers who are significantly faster (potential rushing)
        val speedThreshold = overallAvg.multiply(BigDecimal("0.5")) // 50% faster than average
        
        todayPerformance.forEach { performance ->
            if (performance.averageTimePerTask != null && 
                performance.averageTimePerTask < speedThreshold &&
                performance.tasksCompleted >= 10) {
                
                val existingFlags = qualityFlagRepository.findByWorkerUniqueIdAndResolved(
                    performance.workerUniqueId, false
                )
                val alreadyFlagged = existingFlags.any { 
                    it.flagType == "high_speed" && 
                    it.flaggedAt.isAfter(LocalDateTime.now().minusDays(1))
                }
                
                if (!alreadyFlagged) {
                    val flag = QualityFlagEntity(
                        workerUniqueId = performance.workerUniqueId,
                        questionId = questionId,
                        flagType = "high_speed",
                        severity = "medium",
                        description = "Completed tasks ${overallAvg.divide(performance.averageTimePerTask, 1, java.math.RoundingMode.HALF_UP)}x faster than average (${performance.averageTimePerTask}s vs ${overallAvg}s avg)",
                        resolved = false
                    )
                    flags.add(qualityFlagRepository.save(flag))
                    logger.warn("Flagged worker ${performance.workerUniqueId} for high speed")
                }
            }
        }
        
        return flags
    }
    
    /**
     * Resolve a quality flag
     */
    fun resolveFlag(
        flagId: Long, 
        resolvedBy: String, 
        resolutionNotes: String,
        actionTaken: String?
    ): QualityFlagEntity? {
        logger.info("Resolving flag $flagId by $resolvedBy")
        
        val flag = qualityFlagRepository.findById(flagId).orElse(null) ?: return null
        
        flag.resolved = true
        flag.resolvedBy = resolvedBy
        flag.resolvedAt = LocalDateTime.now()
        flag.resolutionNotes = resolutionNotes
        
        logger.info("Resolved flag $flagId - Action: $actionTaken")
        return qualityFlagRepository.save(flag)
    }
    
    /**
     * Get unresolved flags
     */
    fun getUnresolvedFlags(questionId: Long?, severity: String?): List<QualityFlagEntity> {
        return if (questionId != null) {
            qualityFlagRepository.findByQuestionId(questionId).filter { !it.resolved }
        } else if (severity != null) {
            qualityFlagRepository.findByResolvedAndSeverity(false, severity)
        } else {
            qualityFlagRepository.findByResolved(false)
        }
    }
    
    /**
     * Get flags for a worker
     */
    fun getWorkerFlags(workerId: String, resolved: Boolean?): List<QualityFlagEntity> {
        return if (resolved != null) {
            qualityFlagRepository.findByWorkerUniqueIdAndResolved(workerId, resolved)
        } else {
            qualityFlagRepository.findByWorkerUniqueId(workerId)
        }
    }
    
    /**
     * Manually flag a worker
     */
    fun createManualFlag(
        workerId: String,
        questionId: Long,
        description: String,
        severity: String,
        flaggedBy: String
    ): QualityFlagEntity {
        logger.info("Creating manual flag for worker $workerId by $flaggedBy")
        
        val flag = QualityFlagEntity(
            workerUniqueId = workerId,
            questionId = questionId,
            flagType = "manual",
            severity = severity,
            description = "Manual flag by $flaggedBy: $description",
            resolved = false
        )
        
        return qualityFlagRepository.save(flag)
    }
    
    /**
     * Determine severity based on consensus score
     */
    private fun determineSeverity(consensusScore: BigDecimal): String {
        val score = consensusScore.toDouble()
        return when {
            score < 50.0 -> "high"
            score < 60.0 -> "medium"
            else -> "low"
        }
    }
    
    /**
     * Get flag statistics
     */
    fun getFlagStatistics(questionId: Long?): Map<String, Any> {
        val flags = if (questionId != null) {
            qualityFlagRepository.findByQuestionId(questionId)
        } else {
            qualityFlagRepository.findAll()
        }
        
        val unresolvedCount = flags.count { !it.resolved }
        val bySeverity = flags.groupBy { it.severity }
        val byType = flags.groupBy { it.flagType }
        
        return mapOf(
            "total_flags" to flags.size,
            "unresolved" to unresolvedCount,
            "resolved" to (flags.size - unresolvedCount),
            "by_severity" to mapOf(
                "high" to (bySeverity["high"]?.size ?: 0),
                "medium" to (bySeverity["medium"]?.size ?: 0),
                "low" to (bySeverity["low"]?.size ?: 0)
            ),
            "by_type" to mapOf(
                "low_consensus" to (byType["low_consensus"]?.size ?: 0),
                "high_speed" to (byType["high_speed"]?.size ?: 0),
                "manual" to (byType["manual"]?.size ?: 0)
            )
        )
    }
}
