package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.entity.ActivityLogEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ActivityLogRepository : JpaRepository<ActivityLogEntity, Long> {
    
    fun findByUserId(userId: String): List<ActivityLogEntity>
    
    fun findByWorkerUniqueId(workerUniqueId: String): List<ActivityLogEntity>
    
    fun findByAction(action: String): List<ActivityLogEntity>
    
    fun findByTimestampBetween(startTime: LocalDateTime, endTime: LocalDateTime): List<ActivityLogEntity>
    
    fun findByUserIdAndTimestampBetween(
        userId: String, 
        startTime: LocalDateTime, 
        endTime: LocalDateTime
    ): List<ActivityLogEntity>
    
    @Query("SELECT a FROM activity_log a WHERE a.timestamp < :cutoffDate")
    fun findOldLogs(@Param("cutoffDate") cutoffDate: LocalDateTime): List<ActivityLogEntity>
    
    @Query("SELECT COUNT(a) FROM activity_log a WHERE a.action = :action AND a.timestamp >= :since")
    fun countByActionSince(@Param("action") action: String, @Param("since") since: LocalDateTime): Long
    
    fun findByUserIdAndTimestampAfterOrderByTimestampDesc(userId: String, since: LocalDateTime): List<ActivityLogEntity>
    
    fun findByActionAndTimestampAfterOrderByTimestampDesc(action: String, since: LocalDateTime): List<ActivityLogEntity>
    
    fun findTop100ByOrderByTimestampDesc(): List<ActivityLogEntity>
    
    fun findByTimestampAfterOrderByTimestampDesc(since: LocalDateTime): List<ActivityLogEntity>
}
