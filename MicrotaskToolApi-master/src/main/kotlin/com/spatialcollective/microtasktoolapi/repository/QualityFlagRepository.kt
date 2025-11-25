package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.entity.QualityFlagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface QualityFlagRepository : JpaRepository<QualityFlagEntity, Long> {
    
    fun findByWorkerUniqueId(workerUniqueId: String): List<QualityFlagEntity>
    
    fun findByWorkerUniqueIdAndResolved(workerUniqueId: String, resolved: Boolean): List<QualityFlagEntity>
    
    fun findByResolved(resolved: Boolean): List<QualityFlagEntity>
    
    fun findByResolvedAndSeverity(resolved: Boolean, severity: String): List<QualityFlagEntity>
    
    fun findByQuestionId(questionId: Long): List<QualityFlagEntity>
    
    fun findByFlaggedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<QualityFlagEntity>
    
    @Query("SELECT COUNT(f) FROM quality_flags f WHERE f.workerUniqueId = :workerId AND f.resolved = false")
    fun countUnresolvedFlagsByWorker(@Param("workerId") workerId: String): Long
    
    @Query("SELECT f FROM quality_flags f WHERE f.resolved = false AND f.severity IN :severities")
    fun findUnresolvedBySeverities(@Param("severities") severities: List<String>): List<QualityFlagEntity>
}
