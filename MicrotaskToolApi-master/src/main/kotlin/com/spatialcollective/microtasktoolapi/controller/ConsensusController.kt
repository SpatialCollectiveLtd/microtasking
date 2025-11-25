package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.dto.*
import com.spatialcollective.microtasktoolapi.service.ConsensusService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/v1/consensus")
@CrossOrigin(origins = ["*"])
class ConsensusController(
    @Autowired private val consensusService: ConsensusService
) {
    
    private val logger = LoggerFactory.getLogger(ConsensusController::class.java)
    
    /**
     * GET /api/v1/consensus/calculate/{questionId}
     * Calculate consensus for all images in a question
     */
    @GetMapping("/calculate/{questionId}")
    fun calculateConsensus(@PathVariable questionId: Long): ResponseEntity<ApiResponse<ConsensusCalculationResponse>> {
        logger.info("GET /api/v1/consensus/calculate/$questionId")
        
        return try {
            val results = consensusService.calculateConsensusForQuestion(questionId)
            val stats = consensusService.getConsensusStatistics(questionId)
            
            val response = ConsensusCalculationResponse(
                questionId = questionId,
                totalImages = stats["total_images"] as? Int ?: 0,
                imagesProcessed = results.size,
                consensusReached = stats["images_with_consensus"] as? Int ?: 0,
                requiresReview = stats["images_requiring_review"] as? Int ?: 0,
                consensusThreshold = stats["consensus_threshold"] as? java.math.BigDecimal ?: java.math.BigDecimal.ZERO,
                results = results.map { result ->
                    ConsensusResultDto(
                        imageId = result.imageId,
                        url = null,
                        groundTruth = result.groundTruth,
                        votes = emptyMap(),
                        consensusPercentage = result.consensusPercentage,
                        requiresReview = result.requiresReview
                    )
                }
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Consensus calculated successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error calculating consensus for question $questionId", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to calculate consensus: ${e.message}"
            ))
        }
    }
    
    /**
     * POST /api/v1/consensus/recalculate
     * Force recalculation of consensus
     */
    @PostMapping("/recalculate")
    fun recalculateConsensus(@RequestBody request: ConsensusRecalculateRequest): ResponseEntity<ApiResponse<ConsensusCalculationResponse>> {
        logger.info("POST /api/v1/consensus/recalculate - questionId: ${request.questionId}, imageIds: ${request.imageIds}")
        
        return try {
            val results = consensusService.recalculateConsensus(request.questionId, request.imageIds)
            
            val response = ConsensusCalculationResponse(
                questionId = request.questionId,
                totalImages = results.size,
                imagesProcessed = results.size,
                consensusReached = results.count { !it.requiresReview },
                requiresReview = results.count { it.requiresReview },
                consensusThreshold = java.math.BigDecimal("60.0"),
                results = results.map { result ->
                    ConsensusResultDto(
                        imageId = result.imageId,
                        url = null,
                        groundTruth = result.groundTruth,
                        votes = emptyMap(),
                        consensusPercentage = result.consensusPercentage,
                        requiresReview = result.requiresReview
                    )
                }
            )
            
            ResponseEntity.ok(ApiResponse(
                status = "success",
                message = "Consensus recalculated successfully",
                data = response
            ))
        } catch (e: Exception) {
            logger.error("Error recalculating consensus", e)
            ResponseEntity.internalServerError().body(ApiResponse(
                status = "error",
                message = "Failed to recalculate consensus: ${e.message}"
            ))
        }
    }
}
