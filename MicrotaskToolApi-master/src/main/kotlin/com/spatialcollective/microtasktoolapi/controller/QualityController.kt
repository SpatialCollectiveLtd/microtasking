package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.dto.*
import com.spatialcollective.microtasktoolapi.service.QualityFlaggingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/quality")
@CrossOrigin(origins = ["*"])
class QualityController(
    @Autowired private val qualityService: QualityFlaggingService
) {
    
    private val logger = LoggerFactory.getLogger(QualityController::class.java)
    
    @GetMapping("/flags")
    fun getQualityFlags(
        @RequestParam(required = false) workerId: String?,
        @RequestParam(required = false) questionId: Long?,
        @RequestParam(required = false) severity: String?,
        @RequestParam(required = false) resolved: Boolean?
    ): ResponseEntity<ApiResponse<QualityFlagsResponse>> {
        logger.info("GET /api/v1/quality/flags - workerId: $workerId, questionId: $questionId, severity: $severity")
        
        return try {
            val result = qualityService.getQualityFlags(workerId, questionId, severity, resolved)
            
            @Suppress("UNCHECKED_CAST")
            val flagsList = result["flags"] as? List<Map<String, Any>> ?: emptyList()
            val stats = result["statistics"] as? Map<String, Any> ?: emptyMap()
            
            val flagDtos = flagsList.map { flag ->
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
                    flaggedAt = LocalDateTime.now(),
                    daysSinceFlagged = null,
                    impact = null,
                    resolved = flag["resolved"] as? Boolean ?: false,
                    assignedTo = flag["resolved_by"] as? String,
                    priority = null
                )
            }
            
            @Suppress("UNCHECKED_CAST")
            val severityMap = stats["by_severity"] as? Map<String, Int> ?: emptyMap()
            
            val response = QualityFlagsResponse(
                totalFlags = flagDtos.size,
                unresolved = flagDtos.count { !it.resolved },
                bySeverity = severityMap,
                flags = flagDtos
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Retrieved ${flagDtos.size} quality flags",
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
    
    @PostMapping("/resolve-flag")
    fun resolveFlag(@RequestBody request: ResolveFlagRequest): ResponseEntity<ApiResponse<ResolveFlagResponse>> {
        logger.info("POST /api/v1/quality/resolve-flag - flagId: ${request.flagId}")
        
        return try {
            val flag = qualityService.resolveFlag(
                flagId = request.flagId,
                resolvedBy = request.resolvedBy,
                resolutionNotes = request.resolutionNotes,
                actionTaken = request.actionTaken
            )
            
            if (flag != null) {
                val response = ResolveFlagResponse(
                    flagId = flag.id ?: 0L,
                    resolved = flag.resolved,
                    resolvedBy = flag.resolvedBy ?: request.resolvedBy,
                    resolvedAt = flag.resolvedAt ?: LocalDateTime.now(),
                    resolutionNotes = request.resolutionNotes,
                    actionTaken = request.actionTaken
                )
                
                ResponseEntity.ok(ApiResponse(
                    status = "success",
                    message = "Quality flag resolved successfully",
                    data = response
                ))
            } else {
                ResponseEntity.badRequest().body(ApiResponse(
                    status = "error",
                    message = "Flag not found: ${request.flagId}"
                ))
            }
        } catch (e: Exception) {
            logger.error("Error resolving quality flag", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to resolve quality flag: ${e.message}"
            ))
        }
    }
    
    @PostMapping("/create-flag")
    fun createFlag(@RequestBody request: CreateFlagRequest): ResponseEntity<ApiResponse<CreateFlagResponse>> {
        logger.info("POST /api/v1/quality/create-flag - workerId: ${request.workerId}, type: ${request.flagType}")
        
        return try {
            val flag = qualityService.createManualFlag(
                workerId = request.workerId,
                questionId = request.questionId,
                description = request.description,
                severity = request.severity,
                flaggedBy = request.flaggedBy
            )
            
            val response = CreateFlagResponse(
                flagId = flag.id ?: 0L,
                workerId = request.workerId,
                questionId = request.questionId,
                flagType = request.flagType,
                severity = request.severity,
                createdAt = LocalDateTime.now(),
                flaggedBy = request.flaggedBy,
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
    
    @GetMapping("/statistics")
    fun getStatistics(
        @RequestParam(required = false) questionId: Long?,
        @RequestParam(required = false) period: String?
    ): ResponseEntity<ApiResponse<QualityStatisticsResponse>> {
        logger.info("GET /api/v1/quality/statistics - questionId: $questionId")
        
        return try {
            val stats = qualityService.getQualityStatistics(questionId)
            
            @Suppress("UNCHECKED_CAST")
            val response = QualityStatisticsResponse(
                totalFlags = stats["total_flags"] as? Int ?: 0,
                unresolvedFlags = stats["unresolved_flags"] as? Int ?: 0,
                affectedWorkers = stats["affected_workers"] as? Int ?: 0,
                bySeverity = stats["by_severity"] as? Map<String, Int> ?: emptyMap(),
                byType = stats["by_type"] as? Map<String, Int> ?: emptyMap(),
                recentTrends = emptyMap(),
                topOffenders = emptyList()
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
