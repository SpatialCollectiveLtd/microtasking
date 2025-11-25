package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.dto.*
import com.spatialcollective.microtasktoolapi.service.PaymentCalculationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin(origins = ["*"])
class PaymentController(
    @Autowired private val paymentService: PaymentCalculationService
) {
    
    private val logger = LoggerFactory.getLogger(PaymentController::class.java)
    
    /**
     * POST /api/v1/payment/calculate
     * Calculate payments for a period
     */
    @PostMapping("/calculate")
    fun calculatePayments(@RequestBody request: PaymentCalculationRequest): ResponseEntity<ApiResponse<PaymentCalculationResponse>> {
        logger.info("POST /api/v1/payment/calculate - questionId: ${request.questionId}, startDate: ${request.startDate}, endDate: ${request.endDate}")
        
        return try {
            val startDate = LocalDate.parse(request.startDate)
            val endDate = LocalDate.parse(request.endDate)
            
            val result = paymentService.calculatePeriodPayments(
                questionId = request.questionId,
                startDate = startDate,
                endDate = endDate
            )
            
            @Suppress("UNCHECKED_CAST")
            val workerPayments = (result["worker_payments"] as? List<Map<String, Any>>) ?: emptyList()
            val summary = result["summary"] as? Map<String, Any>
            
            val response = PaymentCalculationResponse(
                period = Period(
                    startDate = request.startDate,
                    endDate = request.endDate
                ),
                questionId = request.questionId,
                questionName = null,
                workerPayments = workerPayments.map { payment ->
                    WorkerPayment(
                        workerId = payment["worker_id"] as? String ?: "",
                        workerName = null,
                        phoneNumber = null,
                        daysWorked = payment["days_worked"] as? Int ?: 0,
                        totalTasks = payment["total_tasks"] as? Int ?: 0,
                        averageConsensusScore = payment["average_consensus_score"] as? java.math.BigDecimal ?: java.math.BigDecimal.ZERO,
                        basePay = payment["base_pay"] as? java.math.BigDecimal ?: java.math.BigDecimal.ZERO,
                        bonusAmount = payment["bonus_amount"] as? java.math.BigDecimal ?: java.math.BigDecimal.ZERO,
                        totalPayment = payment["total_payment"] as? java.math.BigDecimal ?: java.math.BigDecimal.ZERO,
                        paymentStatus = payment["payment_status"] as? String ?: "pending",
                        qualityFlags = payment["quality_flags"] as? Int ?: 0
                    )
                },
                summary = PaymentSummary(
                    totalWorkers = summary?.get("total_workers") as? Int ?: 0,
                    totalDaysWorked = null,
                    totalPayable = summary?.get("total_payable") as? java.math.BigDecimal ?: java.math.BigDecimal.ZERO,
                    totalBonuses = summary?.get("total_bonuses") as? java.math.BigDecimal ?: java.math.BigDecimal.ZERO,
                    flaggedWorkers = summary?.get("flagged_workers") as? Int ?: 0
                ),
                calculatedAt = java.time.LocalDateTime.now(),
                calculatedBy = "System"
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Payments calculated successfully",
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
    
    /**
     * POST /api/v1/payment/approve
     * Approve calculated payments
     */
    @PostMapping("/approve")
    fun approvePayments(@RequestBody request: PaymentApprovalRequest): ResponseEntity<ApiResponse<PaymentApprovalResponse>> {
        logger.info("POST /api/v1/payment/approve - ${request.workerIds.size} workers, approvedBy: ${request.approvedBy}")
        
        return try {
            val result = paymentService.approvePayments(
                workerIds = request.workerIds,
                questionId = request.questionId,
                startDate = LocalDate.parse(request.startDate),
                endDate = LocalDate.parse(request.endDate),
                approvedBy = request.approvedBy,
                notes = request.notes
            )
            
            @Suppress("UNCHECKED_CAST")
            val approved = (result["approved_workers"] as? List<String>) ?: emptyList()
            val failed = (result["failed_workers"] as? List<String>) ?: emptyList()
            
            val response = PaymentApprovalResponse(
                approvedCount = approved.size,
                approvedWorkers = approved,
                failedWorkers = failed,
                totalAmount = result["total_amount"] as? java.math.BigDecimal ?: java.math.BigDecimal.ZERO,
                approvedBy = request.approvedBy,
                approvedAt = java.time.LocalDateTime.now(),
                exportId = result["export_id"] as? Long
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "${approved.size} payments approved successfully",
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
    
    /**
     * GET /api/v1/payment/export
     * Export payments to Excel
     */
    @GetMapping("/export")
    fun exportPayments(
        @RequestParam questionId: Long,
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        @RequestParam(defaultValue = "approved") status: String
    ): ResponseEntity<ApiResponse<PaymentExportResponse>> {
        logger.info("GET /api/v1/payment/export - questionId: $questionId, status: $status")
        
        return try {
            // Generate export file path
            val fileName = "payment_export_${questionId}_${startDate}_${endDate}_${System.currentTimeMillis()}.xlsx"
            val filePath = "/exports/$fileName"
            
            val response = PaymentExportResponse(
                exportId = System.currentTimeMillis(),
                fileName = fileName,
                filePath = filePath,
                recordCount = 0, // Would be populated by actual export logic
                totalAmount = java.math.BigDecimal.ZERO,
                generatedAt = java.time.LocalDateTime.now(),
                generatedBy = "System",
                downloadUrl = "http://micro.spatialcollective.co.ke:8080/api/v1/payment/download/$fileName"
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Payment export generated successfully",
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
    
    /**
     * POST /api/v1/payment/sync-status
     * Update payment sync status from DPW
     */
    @PostMapping("/sync-status")
    fun updateSyncStatus(@RequestBody request: PaymentSyncStatusRequest): ResponseEntity<ApiResponse<PaymentSyncStatusResponse>> {
        logger.info("POST /api/v1/payment/sync-status - exportId: ${request.exportId}, status: ${request.syncStatus}")
        
        return try {
            val result = paymentService.updatePaymentStatus(
                exportId = request.exportId,
                syncStatus = request.syncStatus,
                syncedRecords = request.syncedRecords,
                failedRecords = request.failedRecords,
                errorMessage = request.errorMessage
            )
            
            val response = PaymentSyncStatusResponse(
                exportId = request.exportId,
                syncStatus = request.syncStatus,
                syncedRecords = request.syncedRecords ?: 0,
                failedRecords = request.failedRecords ?: 0,
                updatedAt = java.time.LocalDateTime.now(),
                message = if (request.syncStatus == "success") "Payment sync completed successfully" else "Payment sync failed"
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Sync status updated successfully",
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
