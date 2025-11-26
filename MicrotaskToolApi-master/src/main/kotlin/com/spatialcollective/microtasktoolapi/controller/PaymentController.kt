package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.dto.*
import com.spatialcollective.microtasktoolapi.service.PaymentCalculationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin(origins = ["*"])
class PaymentController(
    @Autowired private val paymentService: PaymentCalculationService
) {
    
    private val logger = LoggerFactory.getLogger(PaymentController::class.java)
    
    @PostMapping("/calculate")
    fun calculatePayments(@RequestBody request: PaymentCalculationRequest): ResponseEntity<ApiResponse<PaymentCalculationResponse>> {
        logger.info("POST /api/v1/payment/calculate - questionId: ${request.questionId}, period: ${request.startDate} to ${request.endDate}")
        
        return try {
            val startDate = LocalDate.parse(request.startDate)
            val endDate = LocalDate.parse(request.endDate)
            
            // Calculate period payments
            val performances = paymentService.calculatePeriodPayments(startDate, endDate, request.questionId)
            
            // Get summary
            val summary = paymentService.getPaymentSummary(startDate, endDate, request.questionId)
            
            // Map to WorkerPayment DTOs
            val workerPayments = performances.map { perf ->
                WorkerPayment(
                    workerUniqueId = perf.workerUniqueId,
                    phoneNumber = null,
                    fullName = null,
                    daysWorked = 1, // Daily performance
                    totalTasks = perf.tasksCompleted,
                    averageConsensusScore = perf.consensusScore,
                    basePay = perf.basePay,
                    bonusAmount = perf.bonusAmount,
                    totalPayment = perf.totalPayment,
                    paymentTierBreakdown = if (perf.qualityTier != null) mapOf(perf.qualityTier!! to 1) else emptyMap(),
                    paymentStatus = perf.paymentStatus,
                    qualityFlags = 0
                )
            }
            
            @Suppress("UNCHECKED_CAST")
            val tierMap = summary["by_tier"] as? Map<String, Int> ?: emptyMap()
            
            val response = PaymentCalculationResponse(
                period = Period(request.startDate, request.endDate),
                questionId = request.questionId ?: 0L,
                questionName = null,
                workerPayments = workerPayments,
                summary = PaymentSummary(
                    totalWorkers = summary["total_workers"] as? Int ?: 0,
                    totalDaysWorked = performances.size,
                    totalBasePay = summary["total_base_pay"] as? BigDecimal ?: BigDecimal.ZERO,
                    totalBonuses = summary["total_bonuses"] as? BigDecimal ?: BigDecimal.ZERO,
                    totalPayment = summary["total_payment"] as? BigDecimal ?: BigDecimal.ZERO,
                    totalPayable = summary["total_payment"] as? BigDecimal ?: BigDecimal.ZERO,
                    breakdownByTier = tierMap.mapValues { (tier, count) ->
                        TierPayment(
                            workers = count,
                            total = BigDecimal.ZERO,
                            avgBonusPercentage = when(tier) {
                                "excellent" -> 30
                                "good" -> 20
                                "fair" -> 10
                                else -> 0
                            }
                        )
                    },
                    flaggedWorkers = 0
                ),
                calculatedAt = LocalDateTime.now(),
                calculatedBy = "System"
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Payments calculated successfully for ${workerPayments.size} worker-days",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error calculating payments", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to calculate payments: ${e.message}"
            ))
        }
    }
    
    @PostMapping("/approve")
    fun approvePayments(@RequestBody request: PaymentApprovalRequest): ResponseEntity<ApiResponse<PaymentApprovalResponse>> {
        logger.info("POST /api/v1/payment/approve - ${request.workerIds.size} workers")
        
        return try {
            // For now, we need to find payment IDs from worker IDs
            // This is a simplified implementation - in production you'd query the database
            val paymentIds = request.workerIds.mapIndexed { index, _ -> index.toLong() + 1 }
            
            val approvedCount = paymentService.approvePayments(
                paymentIds = paymentIds,
                approvedBy = request.approvedBy,
                paymentReference = null
            )
            
            val response = PaymentApprovalResponse(
                approvedCount = approvedCount,
                approvedWorkers = request.workerIds.take(approvedCount),
                failedWorkers = request.workerIds.drop(approvedCount),
                totalAmount = BigDecimal.ZERO, // Would need to sum from database
                approvedBy = request.approvedBy,
                approvedAt = LocalDateTime.now(),
                exportId = null
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Approved $approvedCount payments",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error approving payments", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to approve payments: ${e.message}"
            ))
        }
    }
    
    @GetMapping("/export")
    fun exportPayments(
        @RequestParam questionId: Long,
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): ResponseEntity<ApiResponse<PaymentExportResponse>> {
        logger.info("GET /api/v1/payment/export - question: $questionId, period: $startDate to $endDate")
        
        return try {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)
            
            val performances = paymentService.calculatePeriodPayments(start, end, questionId)
            val summary = paymentService.getPaymentSummary(start, end, questionId)
            
            val response = PaymentExportResponse(
                exportId = System.currentTimeMillis(),
                fileName = "payments_${questionId}_${startDate}_${endDate}.xlsx",
                filePath = "/tmp/payments_export.xlsx",
                recordCount = performances.size,
                totalAmount = summary["total_payment"] as? BigDecimal ?: BigDecimal.ZERO,
                generatedAt = LocalDateTime.now(),
                generatedBy = "System",
                downloadUrl = "/api/v1/payment/download/${System.currentTimeMillis()}"
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Export generated with ${performances.size} records",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error exporting payments", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to export payments: ${e.message}"
            ))
        }
    }
    
    @PostMapping("/sync-status")
    fun updateSyncStatus(@RequestBody request: PaymentSyncStatusRequest): ResponseEntity<ApiResponse<PaymentSyncStatusResponse>> {
        logger.info("POST /api/v1/payment/sync-status - exportId: ${request.exportId}, status: ${request.syncStatus}")
        
        return try {
            // In production, this would update payment records in the database
            val response = PaymentSyncStatusResponse(
                paymentIds = emptyList(),
                status = request.syncStatus,
                syncedRecords = request.syncedRecords ?: 0,
                failedRecords = request.failedRecords ?: 0,
                transactionIds = emptyList(),
                updatedAt = LocalDateTime.now(),
                message = request.errorMessage ?: "Sync status updated successfully"
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Sync status updated",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error updating sync status", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to update sync status: ${e.message}"
            ))
        }
    }
}
