package com.spatialcollective.microtasktoolapi.service

import com.spatialcollective.microtasktoolapi.repository.WorkerPerformanceRepository
import com.spatialcollective.microtasktoolapi.repository.QualityFlagRepository
import com.spatialcollective.microtasktoolapi.repository.QuestionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class PerformanceAnalyticsService(
    @Autowired private val workerPerformanceRepository: WorkerPerformanceRepository,
    @Autowired private val qualityFlagRepository: QualityFlagRepository,
    @Autowired private val questionRepository: QuestionRepository
) {
    
    private val logger = LoggerFactory.getLogger(PerformanceAnalyticsService::class.java)
    
    /**
     * Get comprehensive worker performance metrics
     */
    fun getWorkerPerformance(
        workerId: String, 
        startDate: LocalDate, 
        endDate: LocalDate,
        questionId: Long?
    ): Map<String, Any> {
        logger.info("Getting performance for worker $workerId from $startDate to $endDate")
        
        var performances = workerPerformanceRepository.findByWorkerUniqueIdAndDateBetween(
            workerId, startDate, endDate
        )
        
        if (questionId != null) {
            performances = performances.filter { it.questionId == questionId }
        }
        
        if (performances.isEmpty()) {
            return emptyMap()
        }
        
        val daysWorked = performances.size
        val totalTasks = performances.sumOf { it.tasksCompleted }
        val avgTasksPerDay = totalTasks / daysWorked
        
        val avgConsensusScore = performances
            .map { it.consensusScore }
            .reduce { acc, score -> acc.add(score) }
            .divide(BigDecimal(daysWorked), 2, RoundingMode.HALF_UP)
        
        val totalEarnings = performances.sumOf { it.totalPayment }
        
        val tierDistribution = performances.groupBy { it.qualityTier }
        
        val flags = qualityFlagRepository.findByWorkerUniqueId(workerId)
            .filter { it.flaggedAt.toLocalDate() in startDate..endDate }
        
        return mapOf(
            "worker_unique_id" to workerId,
            "period" to mapOf(
                "start_date" to startDate,
                "end_date" to endDate
            ),
            "summary" to mapOf(
                "days_worked" to daysWorked,
                "total_tasks" to totalTasks,
                "avg_tasks_per_day" to avgTasksPerDay,
                "average_consensus_score" to avgConsensusScore,
                "total_earnings" to totalEarnings
            ),
            "quality_tier_distribution" to mapOf(
                "excellent" to (tierDistribution["excellent"]?.size ?: 0),
                "good" to (tierDistribution["good"]?.size ?: 0),
                "fair" to (tierDistribution["fair"]?.size ?: 0),
                "poor" to (tierDistribution["poor"]?.size ?: 0)
            ),
            "daily_breakdown" to performances.sortedByDescending { it.date }.map { perf ->
                mapOf(
                    "date" to perf.date,
                    "tasks_completed" to perf.tasksCompleted,
                    "correct_answers" to perf.correctAnswers,
                    "consensus_score" to perf.consensusScore,
                    "quality_tier" to perf.qualityTier,
                    "base_pay" to perf.basePay,
                    "bonus_amount" to perf.bonusAmount,
                    "total_payment" to perf.totalPayment,
                    "payment_status" to perf.paymentStatus
                )
            },
            "quality_flags" to flags.map { flag ->
                mapOf(
                    "flag_id" to flag.id,
                    "flag_type" to flag.flagType,
                    "severity" to flag.severity,
                    "description" to flag.description,
                    "flagged_at" to flag.flaggedAt,
                    "resolved" to flag.resolved
                )
            }
        )
    }
    
    /**
     * Get question-wide performance statistics
     */
    fun getQuestionPerformance(questionId: Long): Map<String, Any> {
        logger.info("Getting performance statistics for question $questionId")
        
        val performances = workerPerformanceRepository.findByQuestionId(questionId)
        
        if (performances.isEmpty()) {
            return emptyMap()
        }
        
        val totalWorkers = performances.map { it.workerUniqueId }.distinct().size
        val totalTasks = performances.sumOf { it.tasksCompleted }
        
        val avgConsensusScore = performances
            .map { it.consensusScore }
            .reduce { acc, score -> acc.add(score) }
            .divide(BigDecimal(performances.size), 2, RoundingMode.HALF_UP)
        
        val totalPayable = performances.sumOf { it.totalPayment }
        
        val workersByTier = performances.groupBy { it.qualityTier }
        
        val flags = qualityFlagRepository.findByQuestionId(questionId)
        val flaggedWorkers = flags.map { it.workerUniqueId }.distinct().size
        
        return mapOf(
            "question_id" to questionId,
            "total_workers" to totalWorkers,
            "total_tasks" to totalTasks,
            "average_consensus_score" to avgConsensusScore,
            "total_payable" to totalPayable,
            "workers_by_tier" to mapOf(
                "excellent" to (workersByTier["excellent"]?.map { it.workerUniqueId }?.distinct()?.size ?: 0),
                "good" to (workersByTier["good"]?.map { it.workerUniqueId }?.distinct()?.size ?: 0),
                "fair" to (workersByTier["fair"]?.map { it.workerUniqueId }?.distinct()?.size ?: 0),
                "poor" to (workersByTier["poor"]?.map { it.workerUniqueId }?.distinct()?.size ?: 0)
            ),
            "flagged_workers" to flaggedWorkers,
            "total_flags" to flags.size,
            "unresolved_flags" to flags.count { !it.resolved }
        )
    }
    
    /**
     * Get leaderboard (top performers)
     */
    fun getLeaderboard(questionId: Long?, limit: Int = 10): List<Map<String, Any>> {
        logger.info("Getting leaderboard for question $questionId, limit $limit")
        
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(7) // Last 7 days
        
        var performances = workerPerformanceRepository.findByDateBetweenAndPaymentStatus(
            startDate, endDate, "pending"
        )
        
        if (questionId != null) {
            performances = performances.filter { it.questionId == questionId }
        }
        
        // Group by worker and calculate averages
        val workerStats = performances.groupBy { it.workerUniqueId }
            .map { (workerId, perfs) ->
                val avgScore = perfs
                    .map { it.consensusScore }
                    .reduce { acc, score -> acc.add(score) }
                    .divide(BigDecimal(perfs.size), 2, RoundingMode.HALF_UP)
                
                val totalTasks = perfs.sumOf { it.tasksCompleted }
                val totalEarnings = perfs.sumOf { it.totalPayment }
                
                mapOf(
                    "worker_id" to workerId,
                    "average_consensus_score" to avgScore,
                    "total_tasks" to totalTasks,
                    "total_earnings" to totalEarnings,
                    "days_worked" to perfs.size
                )
            }
            .sortedByDescending { (it["average_consensus_score"] as BigDecimal).toDouble() }
            .take(limit)
        
        return workerStats
    }
    
    /**
     * Get dashboard analytics for DPW App
     */
    fun getDashboardAnalytics(): Map<String, Any> {
        logger.info("Getting dashboard analytics")
        
        val today = LocalDate.now()
        val weekStart = today.minusDays(7)
        
        // Get active questions
        val activeQuestions = questionRepository.findAll().filter { !it.isPaused }
        
        // Today's stats
        val todayPerformances = workerPerformanceRepository.findAllByDateOrderByConsensusScoreDesc(today)
        val todayWorkers = todayPerformances.map { it.workerUniqueId }.distinct().size
        val todayTasks = todayPerformances.sumOf { it.tasksCompleted }
        val todayAvgQuality = if (todayPerformances.isNotEmpty()) {
            todayPerformances.map { it.consensusScore }
                .reduce { acc, score -> acc.add(score) }
                .divide(BigDecimal(todayPerformances.size), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
        val todayPendingPayments = todayPerformances
            .filter { it.paymentStatus == "pending" }
            .sumOf { it.totalPayment }
        
        // This week's stats
        val weekPerformances = workerPerformanceRepository.findByDateBetweenAndPaymentStatus(
            weekStart, today, "pending"
        )
        val weekTasks = weekPerformances.sumOf { it.tasksCompleted }
        val weekAvgQuality = if (weekPerformances.isNotEmpty()) {
            weekPerformances.map { it.consensusScore }
                .reduce { acc, score -> acc.add(score) }
                .divide(BigDecimal(weekPerformances.size), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
        val weekTotalPayments = weekPerformances.sumOf { it.totalPayment }
        
        // Flags
        val unresolvedFlags = qualityFlagRepository.findByResolved(false)
        
        return mapOf(
            "overview" to mapOf(
                "active_questions" to activeQuestions.size,
                "active_workers" to todayWorkers
            ),
            "today" to mapOf(
                "tasks_completed" to todayTasks,
                "average_consensus_score" to todayAvgQuality,
                "pending_payments" to todayPendingPayments,
                "flagged_workers" to unresolvedFlags.map { it.workerUniqueId }.distinct().size
            ),
            "this_week" to mapOf(
                "tasks_completed" to weekTasks,
                "average_consensus_score" to weekAvgQuality,
                "total_payments" to weekTotalPayments,
                "total_workers" to weekPerformances.map { it.workerUniqueId }.distinct().size
            ),
            "recent_alerts" to unresolvedFlags.take(5).map { flag ->
                mapOf(
                    "type" to "quality",
                    "severity" to flag.severity,
                    "message" to flag.description,
                    "timestamp" to flag.flaggedAt,
                    "resolved" to flag.resolved
                )
            }
        )
    }
    
    /**
     * Get performance trends over time
     */
    fun getPerformanceTrends(questionId: Long?, days: Int = 30): Map<String, Any> {
        logger.info("Getting performance trends for $days days")
        
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        
        var performances = workerPerformanceRepository.findByDateBetweenAndPaymentStatus(
            startDate, endDate, "pending"
        )
        
        if (questionId != null) {
            performances = performances.filter { it.questionId == questionId }
        }
        
        // Group by date
        val dailyStats = performances.groupBy { it.date }
            .map { (date, perfs) ->
                val avgScore = perfs
                    .map { it.consensusScore }
                    .reduce { acc, score -> acc.add(score) }
                    .divide(BigDecimal(perfs.size), 2, RoundingMode.HALF_UP)
                
                mapOf(
                    "date" to date,
                    "workers" to perfs.map { it.workerUniqueId }.distinct().size,
                    "tasks" to perfs.sumOf { it.tasksCompleted },
                    "avg_consensus_score" to avgScore,
                    "total_payment" to perfs.sumOf { it.totalPayment }
                )
            }
            .sortedBy { it["date"] as LocalDate }
        
        return mapOf(
            "period" to mapOf(
                "start_date" to startDate,
                "end_date" to endDate,
                "days" to days
            ),
            "daily_stats" to dailyStats
        )
    }
}
