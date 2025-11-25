package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.dto.*
import com.spatialcollective.microtasktoolapi.service.PerformanceAnalyticsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/performance")
@CrossOrigin(origins = ["*"])
class PerformanceController(
    @Autowired private val analyticsService: PerformanceAnalyticsService
) {
    
    private val logger = LoggerFactory.getLogger(PerformanceController::class.java)
    
    /**
     * GET /api/v1/performance/worker/{workerId}
     * Get detailed performance metrics for a worker
     */
    @GetMapping("/worker/{workerId}")
    fun getWorkerPerformance(
        @PathVariable workerId: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @RequestParam(required = false) questionId: Long?
    ): ResponseEntity<ApiResponse<WorkerPerformanceResponse>> {
        logger.info("GET /api/v1/performance/worker/$workerId")
        
        return try {
            val start = startDate?.let { LocalDate.parse(it) } ?: LocalDate.now().minusDays(7)
            val end = endDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
            
            val performance = analyticsService.getWorkerPerformance(workerId, start, end, questionId)
            
            if (performance.isEmpty()) {
                return ResponseEntity.ok(ApiResponse(
                    status = "success",
                    message = "No performance data found for worker",
                    data = null
                ))
            }
            
            @Suppress("UNCHECKED_CAST")
            val summary = performance["summary"] as? Map<String, Any>
            val dailyBreakdown = (performance["daily_breakdown"] as? List<Map<String, Any>>) ?: emptyList()
            val qualityFlags = (performance["quality_flags"] as? List<Map<String, Any>>) ?: emptyList()
            
            val response = WorkerPerformanceResponse(
                workerInfo = WorkerInfo(
                    workerUniqueId = workerId,
                    phoneNumber = null,
                    fullName = null,
                    registrationDate = null
                ),
                period = Period(
                    startDate = start.toString(),
                    endDate = end.toString()
                ),
                summary = PerformanceSummary(
                    daysWorked = summary?.get("days_worked") as? Int ?: 0,
                    totalTasks = summary?.get("total_tasks") as? Int ?: 0,
                    avgTasksPerDay = summary?.get("avg_tasks_per_day") as? Int ?: 0,
                    averageConsensusScore = summary?.get("average_consensus_score") as? BigDecimal ?: BigDecimal.ZERO,
                    totalEarnings = summary?.get("total_earnings") as? BigDecimal ?: BigDecimal.ZERO,
                    qualityTierDistribution = (performance["quality_tier_distribution"] as? Map<String, Int>) ?: emptyMap()
                ),
                dailyBreakdown = dailyBreakdown.map { day ->
                    DailyPerformance(
                        date = day["date"].toString(),
                        tasksCompleted = day["tasks_completed"] as? Int ?: 0,
                        correctAnswers = day["correct_answers"] as? Int ?: 0,
                        incorrectAnswers = null,
                        consensusScore = day["consensus_score"] as? BigDecimal ?: BigDecimal.ZERO,
                        averageTimePerTask = null,
                        qualityTier = day["quality_tier"] as? String ?: "poor",
                        basePay = day["base_pay"] as? BigDecimal ?: BigDecimal.ZERO,
                        bonusAmount = day["bonus_amount"] as? BigDecimal ?: BigDecimal.ZERO,
                        totalPayment = day["total_payment"] as? BigDecimal ?: BigDecimal.ZERO,
                        paymentStatus = day["payment_status"] as? String ?: "pending"
                    )
                },
                qualityFlags = qualityFlags.map { flag ->
                    QualityFlagDto(
                        flagId = (flag["flag_id"] as? Number)?.toLong() ?: 0L,
                        workerUniqueId = workerId,
                        workerName = null,
                        phoneNumber = null,
                        questionId = 0L,
                        questionName = null,
                        flagType = flag["flag_type"] as? String ?: "",
                        severity = flag["severity"] as? String ?: "",
                        description = flag["description"] as? String ?: "",
                        flaggedAt = java.time.LocalDateTime.now(),
                        daysSinceFlagged = null,
                        impact = null,
                        resolved = flag["resolved"] as? Boolean ?: false,
                        assignedTo = null,
                        priority = null
                    )
                },
                trends = null
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Worker performance retrieved successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error fetching worker performance", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to fetch worker performance: ${e.message}"
            ))
        }
    }
    
    /**
     * GET /api/v1/performance/question/{questionId}
     * Get aggregate performance for a question
     */
    @GetMapping("/question/{questionId}")
    fun getQuestionPerformance(@PathVariable questionId: Long): ResponseEntity<ApiResponse<QuestionPerformanceResponse>> {
        logger.info("GET /api/v1/performance/question/$questionId")
        
        return try {
            val performance = analyticsService.getQuestionPerformance(questionId)
            
            if (performance.isEmpty()) {
                return ResponseEntity.ok(ApiResponse(
                    status = "success",
                    message = "No performance data found for question",
                    data = null
                ))
            }
            
            @Suppress("UNCHECKED_CAST")
            val response = QuestionPerformanceResponse(
                questionId = questionId,
                questionName = "Question $questionId",
                totalWorkers = performance["total_workers"] as? Int ?: 0,
                totalImages = null,
                completionRate = null,
                averageConsensusScore = performance["average_consensus_score"] as? BigDecimal ?: BigDecimal.ZERO,
                workersByTier = (performance["workers_by_tier"] as? Map<String, Int>) ?: emptyMap(),
                flaggedWorkers = performance["flagged_workers"] as? Int ?: 0,
                totalPayable = performance["total_payable"] as? BigDecimal ?: BigDecimal.ZERO
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Question performance retrieved successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error fetching question performance", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to fetch question performance: ${e.message}"
            ))
        }
    }
    
    /**
     * GET /api/v1/performance/leaderboard
     * Get top performers leaderboard
     */
    @GetMapping("/leaderboard")
    fun getLeaderboard(
        @RequestParam(required = false) questionId: Long?,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<ApiResponse<LeaderboardResponse>> {
        logger.info("GET /api/v1/performance/leaderboard - questionId: $questionId, limit: $limit")
        
        return try {
            val leaderboard = analyticsService.getLeaderboard(questionId, limit)
            
            val entries = leaderboard.mapIndexed { index, entry ->
                @Suppress("UNCHECKED_CAST")
                LeaderboardEntry(
                    rank = index + 1,
                    workerId = entry["worker_id"] as? String ?: "",
                    workerName = null,
                    averageConsensusScore = entry["average_consensus_score"] as? BigDecimal ?: BigDecimal.ZERO,
                    totalTasks = entry["total_tasks"] as? Int ?: 0,
                    totalEarnings = entry["total_earnings"] as? BigDecimal ?: BigDecimal.ZERO,
                    daysWorked = entry["days_worked"] as? Int ?: 0
                )
            }
            
            val response = LeaderboardResponse(
                period = Period(
                    startDate = LocalDate.now().minusDays(7).toString(),
                    endDate = LocalDate.now().toString()
                ),
                topPerformers = entries
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Leaderboard retrieved successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error fetching leaderboard", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to fetch leaderboard: ${e.message}"
            ))
        }
    }
}
