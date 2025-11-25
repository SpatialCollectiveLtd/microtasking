package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.entity.ConsensusResultEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ConsensusResultRepository : JpaRepository<ConsensusResultEntity, Long> {
    
    fun findByQuestionId(questionId: Long): List<ConsensusResultEntity>
    
    fun findByQuestionIdAndImageId(questionId: Long, imageId: Long): ConsensusResultEntity?
    
    fun findByQuestionIdAndRequiresReview(questionId: Long, requiresReview: Boolean): List<ConsensusResultEntity>
    
    fun findByRequiresReviewAndReviewStatus(requiresReview: Boolean, reviewStatus: String?): List<ConsensusResultEntity>
    
    @Query("SELECT COUNT(c) FROM consensus_result c WHERE c.questionId = :questionId AND c.requiresReview = true")
    fun countByQuestionIdAndRequiresReview(@Param("questionId") questionId: Long): Long
    
    @Query("SELECT c FROM consensus_result c WHERE c.questionId = :questionId AND c.consensusPercentage >= :threshold")
    fun findByQuestionIdAndConsensusPercentageGreaterThanEqual(
        @Param("questionId") questionId: Long, 
        @Param("threshold") threshold: java.math.BigDecimal
    ): List<ConsensusResultEntity>
}
