package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.dto.*
import com.spatialcollective.microtasktoolapi.service.QualityFlaggingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/v1/quality")
@CrossOrigin(origins = ["*"])
class QualityController(
    @Autowired private val qualityService: QualityFlaggingService
) {
    
    private val logger = LoggerFactory.getLogger(QualityController::class.java)
    
    /**
     * GET /api/v1/quality/flags
     * Get quality flags with filtering
     */
    @GetMapping("/flags")
    fun getQualityFlags(
        @RequestParam(required = false) workerId: String?,
        @RequestParam(required = false) questionId: Long?,
        @RequestParam(required = false) severity: String?,
        @RequestParam(required = false) resolved: Boolean?
    ): ResponseEntity<ApiResponse<QualityFlagsResponse>> {
        logger.info("GET /api/v1/quality/flags - workerId: $workerId, questionId: $questionId, severity: $severity, resolved: $resolved")
        
        return try {
            val flags = qualityService.getQualityFlags(
                workerId = workerId,
                questionId = questionId,
                severity = severity,
                resolved = resolved
            )
            
            @Suppress("UNCHECKED_CAST")
            val flagDtos = (flags["flags"] as? List<Map<String, Any>>) ?: emptyList()
            val stats = flags["statistics"] as? Map<String, Any>
            
            val response = QualityFlagsResponse(
                flags = flagDtos.map { flag ->
                    QualityFlagDto(
                        flagId = (flag["flag_id"] as? Number)?.toLong() ?: 0L,
                        workerUniqueId = flag["worker_unique_id"] as? String ?: "",
                        workerName = null,
                        phoneNumber = null,
                        questionId = (flag["question_id"] as? Number)?.toLong() ?: 0L,
                        questionName = null,
                        flagType = flag["flag_type"] as? String ?: "",
                        severity = flag["severity"] as? String ?: "",
                        description = flag["description"] as? String ?: "",
                        flaggedAt = java.time.LocalDateTime.now(),
                        daysSinceFlagged = null,
                        impact = FlagImpact(
                            tasksAffected = 0,
                            potentialLoss = java.math.BigDecimal.ZERO
                        ),
                        resolved = flag["resolved"] as? Boolean ?: false,
                        assignedTo = flag["assigned_to"] as? String,
                        priority = null
                    )
                },
                totalFlags = flagDtos.size,
                unresolvedFlags = flagDtos.count { !(it["resolved"] as? Boolean ?: false) },
                bySeverity = (stats?.get("by_severity") as? Map<String, Int>) ?: emptyMap(),
                byType = (stats?.get("by_type") as? Map<String, Int>) ?: emptyMap(),
                affectedWorkers = stats?.get("affected_workers") as? Int ?: 0
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Quality flags retrieved successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error fetching quality flags", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to fetch quality flags: ${e.message}"
            ))
        }
    }
    
    /**
     * POST /api/v1/quality/resolve-flag
     * Resolve a quality flag
     */
    @PostMapping("/resolve-flag")
    fun resolveFlag(@RequestBody request: ResolveFlagRequest): ResponseEntity<ApiResponse<ResolveFlagResponse>> {
        logger.info("POST /api/v1/quality/resolve-flag - flagId: ${request.flagId}, resolvedBy: ${request.resolvedBy}")
        
        return try {
            val result = qualityService.resolveFlag(
                flagId = request.flagId,
                resolution = request.resolution,
                resolvedBy = request.resolvedBy,
                actionTaken = request.actionTaken
            )
            
            val response = ResolveFlagResponse(
                flagId = request.flagId,
                resolved = result["resolved"] as? Boolean ?: true,
                resolution = request.resolution,
                resolvedBy = request.resolvedBy,
                resolvedAt = java.time.LocalDateTime.now(),
                actionTaken = request.actionTaken
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Quality flag resolved successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error resolving quality flag", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to resolve quality flag: ${e.message}"
            ))
        }
    }
    
    /**
     * POST /api/v1/quality/create-flag
     * Manually create a quality flag
     */
    @PostMapping("/create-flag")
    fun createFlag(@RequestBody request: CreateFlagRequest): ResponseEntity<ApiResponse<CreateFlagResponse>> {
        logger.info("POST /api/v1/quality/create-flag - workerId: ${request.workerId}, type: ${request.flagType}")
        
        return try {
            val result = qualityService.createManualFlag(
                workerId = request.workerId,
                questionId = request.questionId,
                flagType = request.flagType,
                severity = request.severity,
                description = request.description,
                createdBy = request.createdBy
            )
            
            val response = CreateFlagResponse(
                flagId = result["flag_id"] as? Long ?: 0L,
                workerId = request.workerId,
                questionId = request.questionId,
                flagType = request.flagType,
                severity = request.severity,
                createdAt = java.time.LocalDateTime.now(),
                createdBy = request.createdBy,
                message = "Quality flag created successfully"
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Quality flag created successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error creating quality flag", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to create quality flag: ${e.message}"
            ))
        }
    }
    
    /**
     * GET /api/v1/quality/statistics
     * Get quality statistics overview
     */
    @GetMapping("/statistics")
    fun getQualityStatistics(@RequestParam(required = false) questionId: Long?): ResponseEntity<ApiResponse<QualityStatisticsResponse>> {
        logger.info("GET /api/v1/quality/statistics - questionId: $questionId")
        
        return try {
            val stats = qualityService.getQualityStatistics(questionId)
            
            @Suppress("UNCHECKED_CAST")
            val response = QualityStatisticsResponse(
                totalFlags = stats["total_flags"] as? Int ?: 0,
                unresolvedFlags = stats["unresolved_flags"] as? Int ?: 0,
                affectedWorkers = stats["affected_workers"] as? Int ?: 0,
                bySeverity = (stats["by_severity"] as? Map<String, Int>) ?: emptyMap(),
                byType = (stats["by_type"] as? Map<String, Int>) ?: emptyMap(),
                recentTrends = (stats["recent_trends"] as? Map<String, Any>) ?: emptyMap(),
                topOffenders = (stats["top_offenders"] as? List<Map<String, Any>>) ?: emptyList()
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Quality statistics retrieved successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error fetching quality statistics", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to fetch quality statistics: ${e.message}"
            ))
        }
    }
}
