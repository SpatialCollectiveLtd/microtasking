package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.dto.*
import com.spatialcollective.microtasktoolapi.service.PerformanceAnalyticsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1/analytics")
@CrossOrigin(origins = ["*"])
class AnalyticsController(
    @Autowired private val analyticsService: PerformanceAnalyticsService
) {
    
    private val logger = LoggerFactory.getLogger(AnalyticsController::class.java)
    
    /**
     * GET /api/v1/analytics/dashboard
     * Get real-time dashboard metrics for DPW App
     */
    @GetMapping("/dashboard")
    fun getDashboard(): ResponseEntity<ApiResponse<DashboardResponse>> {
        logger.info("GET /api/v1/analytics/dashboard")
        
        return try {
            val analytics = analyticsService.getDashboardAnalytics()
            
            val response = DashboardResponse(
                overview = DashboardOverview(
                    activeQuestions = ((analytics["overview"] as? Map<*, *>)?.get("active_questions") as? Int) ?: 0,
                    activeWorkers = ((analytics["overview"] as? Map<*, *>)?.get("active_workers") as? Int) ?: 0,
                    totalWorkersRegistered = null
                ),
                today = DailyStats(
                    tasksCompleted = (analytics["today"] as? Map<String, Any>)?.get("tasks_completed") as? Int ?: 0,
                    averageConsensusScore = (analytics["today"] as? Map<String, Any>)?.get("average_consensus_score") as? BigDecimal ?: BigDecimal.ZERO,
                    pendingPayments = (analytics["today"] as? Map<String, Any>)?.get("pending_payments") as? BigDecimal ?: BigDecimal.ZERO,
                    flaggedWorkers = (analytics["today"] as? Map<String, Any>)?.get("flagged_workers") as? Int ?: 0
                ),
                thisWeek = WeeklyStats(
                    tasksCompleted = (analytics["this_week"] as? Map<String, Any>)?.get("tasks_completed") as? Int ?: 0,
                    averageConsensusScore = (analytics["this_week"] as? Map<String, Any>)?.get("average_consensus_score") as? BigDecimal ?: BigDecimal.ZERO,
                    totalPayments = (analytics["this_week"] as? Map<String, Any>)?.get("total_payments") as? BigDecimal ?: BigDecimal.ZERO,
                    totalWorkers = (analytics["this_week"] as? Map<String, Any>)?.get("total_workers") as? Int ?: 0
                ),
                systemHealth = null,
                recentAlerts = emptyList(),
                paymentSummary = PaymentSummaryStats(
                    pendingApproval = BigDecimal.ZERO,
                    approvedNotPaid = BigDecimal.ZERO,
                    paidThisMonth = BigDecimal.ZERO
                )
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Dashboard data retrieved successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error fetching dashboard", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to fetch dashboard data: ${e.message}"
            ))
        }
    }
    
    /**
     * GET /api/v1/analytics/trends
     * Get performance trends over time
     */
    @GetMapping("/trends")
    fun getTrends(
        @RequestParam(required = false) questionId: Long?,
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<ApiResponse<TrendsResponse>> {
        logger.info("GET /api/v1/analytics/trends - questionId: $questionId, days: $days")
        
        return try {
            val trends = analyticsService.getPerformanceTrends(questionId, days)
            
            @Suppress("UNCHECKED_CAST")
            val dailyStats = (trends["daily_stats"] as? List<Map<String, Any>>)?.map { stat ->
                DailyTrendStats(
                    date = stat["date"].toString(),
                    workers = stat["workers"] as? Int ?: 0,
                    tasks = stat["tasks"] as? Int ?: 0,
                    avgConsensusScore = stat["avg_consensus_score"] as? BigDecimal ?: BigDecimal.ZERO,
                    totalPayment = stat["total_payment"] as? BigDecimal ?: BigDecimal.ZERO
                )
            } ?: emptyList()
            
            val period = trends["period"] as? Map<String, Any>
            val response = TrendsResponse(
                period = Period(
                    startDate = period?.get("start_date").toString(),
                    endDate = period?.get("end_date").toString()
                ),
                dailyStats = dailyStats
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Trends retrieved successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error fetching trends", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to fetch trends: ${e.message}"
            ))
        }
    }
}
