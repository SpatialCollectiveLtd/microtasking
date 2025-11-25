package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.entity.SystemHealthEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SystemHealthRepository : JpaRepository<SystemHealthEntity, Long> {
    
    fun findByMetricType(metricType: String): List<SystemHealthEntity>
    
    fun findByStatus(status: String): List<SystemHealthEntity>
    
    fun findByTimestampBetween(startTime: LocalDateTime, endTime: LocalDateTime): List<SystemHealthEntity>
    
    fun findByMetricTypeAndTimestampBetween(
        metricType: String, 
        startTime: LocalDateTime, 
        endTime: LocalDateTime
    ): List<SystemHealthEntity>
    
    @Query("SELECT h FROM system_health h WHERE h.status IN ('warning', 'critical') AND h.alertSent = false")
    fun findUnsentAlerts(): List<SystemHealthEntity>
    
    @Query("SELECT h FROM system_health h WHERE h.timestamp < :cutoffDate")
    fun findOldRecords(@Param("cutoffDate") cutoffDate: LocalDateTime): List<SystemHealthEntity>
}
