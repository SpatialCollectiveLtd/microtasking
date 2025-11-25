package com.spatialcollective.microtasktoolapi.service

import com.spatialcollective.microtasktoolapi.model.entity.ConsensusResultEntity
import com.spatialcollective.microtasktoolapi.repository.AnswerRepository
import com.spatialcollective.microtasktoolapi.repository.ConsensusResultRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import org.slf4j.LoggerFactory

@Service
class ConsensusService(
    @Autowired private val answerRepository: AnswerRepository,
    @Autowired private val consensusResultRepository: ConsensusResultRepository
) {
    
    private val logger = LoggerFactory.getLogger(ConsensusService::class.java)
    
    @Value("\${consensus.default-threshold:60.0}")
    private lateinit var defaultThreshold: String
    
    @Value("\${consensus.minimum-responses:3}")
    private var minimumResponses: Int = 3
    
    /**
     * Calculate consensus for all images in a question
     * Returns list of consensus results
     */
    fun calculateConsensusForQuestion(questionId: Long): List<ConsensusResultEntity> {
        logger.info("Calculating consensus for question $questionId")
        
        // Get all answers for this question
        val answers = answerRepository.findByQuestionId(questionId)
        
        if (answers.isEmpty()) {
            logger.warn("No answers found for question $questionId")
            return emptyList()
        }
        
        // Group answers by imageId
        val answersByImage = answers.groupBy { it.imageId }
        
        val results = mutableListOf<ConsensusResultEntity>()
        val threshold = BigDecimal(defaultThreshold)
        
        answersByImage.forEach { (imageId, imageAnswers) ->
            if (imageAnswers.size < minimumResponses) {
                logger.debug("Image $imageId has only ${imageAnswers.size} responses, skipping consensus")
                return@forEach
            }
            
            // Count answers for each option
            val answerCounts = imageAnswers.groupingBy { it.answer }.eachCount()
            
            // Find the most common answer (ground truth)
            val groundTruth = answerCounts.maxByOrNull { it.value }?.key
            val groundTruthCount = answerCounts[groundTruth] ?: 0
            val totalResponses = imageAnswers.size
            
            // Calculate consensus percentage
            val consensusPercentage = if (totalResponses > 0) {
                BigDecimal(groundTruthCount)
                    .divide(BigDecimal(totalResponses), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
            
            // Determine if review is required
            val requiresReview = consensusPercentage < threshold
            
            // Check if consensus already exists
            val existing = consensusResultRepository.findByQuestionIdAndImageId(questionId, imageId)
            
            val consensusResult = if (existing != null) {
                // Update existing
                existing.apply {
                    this.groundTruth = groundTruth
                    this.totalResponses = totalResponses
                    this.consensusPercentage = consensusPercentage
                    this.requiresReview = requiresReview
                    if (!requiresReview) {
                        this.reviewStatus = "approved"
                    }
                }
            } else {
                // Create new
                ConsensusResultEntity(
                    questionId = questionId,
                    imageId = imageId,
                    groundTruth = groundTruth,
                    totalResponses = totalResponses,
                    consensusPercentage = consensusPercentage,
                    requiresReview = requiresReview,
                    reviewStatus = if (requiresReview) "pending" else "approved"
                )
            }
            
            results.add(consensusResultRepository.save(consensusResult))
        }
        
        logger.info("Calculated consensus for ${results.size} images in question $questionId")
        logger.info("Images requiring review: ${results.count { it.requiresReview }}")
        
        return results
    }
    
    /**
     * Get ground truth for a specific image
     */
    fun getGroundTruth(questionId: Long, imageId: Long): ConsensusResultEntity? {
        return consensusResultRepository.findByQuestionIdAndImageId(questionId, imageId)
    }
    
    /**
     * Recalculate consensus for specific images
     */
    fun recalculateConsensus(questionId: Long, imageIds: List<Long>?): List<ConsensusResultEntity> {
        logger.info("Recalculating consensus for question $questionId, imageIds: $imageIds")
        
        if (imageIds.isNullOrEmpty()) {
            // Recalculate all
            return calculateConsensusForQuestion(questionId)
        }
        
        // Get all answers for this question
        val allAnswers = answerRepository.findByQuestionId(questionId)
        val filteredAnswers = allAnswers.filter { it.imageId in imageIds }
        
        val answersByImage = filteredAnswers.groupBy { it.imageId }
        val results = mutableListOf<ConsensusResultEntity>()
        val threshold = BigDecimal(defaultThreshold)
        
        answersByImage.forEach { (imageId, imageAnswers) ->
            if (imageAnswers.size < minimumResponses) {
                return@forEach
            }
            
            val answerCounts = imageAnswers.groupingBy { it.answer }.eachCount()
            val groundTruth = answerCounts.maxByOrNull { it.value }?.key
            val groundTruthCount = answerCounts[groundTruth] ?: 0
            val totalResponses = imageAnswers.size
            
            val consensusPercentage = if (totalResponses > 0) {
                BigDecimal(groundTruthCount)
                    .divide(BigDecimal(totalResponses), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
            
            val requiresReview = consensusPercentage < threshold
            
            val existing = consensusResultRepository.findByQuestionIdAndImageId(questionId, imageId)
            
            val consensusResult = if (existing != null) {
                existing.apply {
                    this.groundTruth = groundTruth
                    this.totalResponses = totalResponses
                    this.consensusPercentage = consensusPercentage
                    this.requiresReview = requiresReview
                    if (!requiresReview) {
                        this.reviewStatus = "approved"
                    }
                }
            } else {
                ConsensusResultEntity(
                    questionId = questionId,
                    imageId = imageId,
                    groundTruth = groundTruth,
                    totalResponses = totalResponses,
                    consensusPercentage = consensusPercentage,
                    requiresReview = requiresReview,
                    reviewStatus = if (requiresReview) "pending" else "approved"
                )
            }
            
            results.add(consensusResultRepository.save(consensusResult))
        }
        
        return results
    }
    
    /**
     * Get all images requiring review for a question
     */
    fun getImagesRequiringReview(questionId: Long): List<ConsensusResultEntity> {
        return consensusResultRepository.findByQuestionIdAndRequiresReview(questionId, true)
    }
    
    /**
     * Get consensus statistics for a question
     */
    fun getConsensusStatistics(questionId: Long): Map<String, Any> {
        val allResults = consensusResultRepository.findByQuestionId(questionId)
        
        val totalImages = allResults.size
        val imagesRequiringReview = allResults.count { it.requiresReview }
        val averageConsensus = if (allResults.isNotEmpty()) {
            allResults.map { it.consensusPercentage }
                .reduce { acc, value -> acc.add(value) }
                .divide(BigDecimal(totalImages), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
        
        return mapOf(
            "total_images" to totalImages,
            "images_with_consensus" to (totalImages - imagesRequiringReview),
            "images_requiring_review" to imagesRequiringReview,
            "average_consensus_percentage" to averageConsensus,
            "consensus_threshold" to BigDecimal(defaultThreshold)
        )
    }
}
