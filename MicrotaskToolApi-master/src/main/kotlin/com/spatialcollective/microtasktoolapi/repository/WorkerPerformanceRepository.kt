package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.entity.WorkerPerformanceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface WorkerPerformanceRepository : JpaRepository<WorkerPerformanceEntity, Long> {
    
    fun findByWorkerUniqueId(workerUniqueId: String): List<WorkerPerformanceEntity>
    
    fun findByWorkerUniqueIdAndDateBetween(
        workerUniqueId: String, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<WorkerPerformanceEntity>
    
    fun findByQuestionId(questionId: Long): List<WorkerPerformanceEntity>
    
    fun findByQuestionIdAndDate(questionId: Long, date: LocalDate): List<WorkerPerformanceEntity>
    
    fun findByDateBetweenAndPaymentStatus(
        startDate: LocalDate, 
        endDate: LocalDate, 
        paymentStatus: String
    ): List<WorkerPerformanceEntity>
    
    fun findByPaymentStatus(paymentStatus: String): List<WorkerPerformanceEntity>
    
    @Query("SELECT DISTINCT w.workerUniqueId FROM worker_performance w WHERE w.questionId = :questionId")
    fun findDistinctWorkerIdsByQuestionId(@Param("questionId") questionId: Long): List<String>
    
    @Query("SELECT w FROM worker_performance w WHERE w.date = :date ORDER BY w.consensusScore DESC")
    fun findAllByDateOrderByConsensusScoreDesc(@Param("date") date: LocalDate): List<WorkerPerformanceEntity>
    
    @Query("SELECT AVG(w.consensusScore) FROM worker_performance w WHERE w.questionId = :questionId AND w.date BETWEEN :startDate AND :endDate")
    fun getAverageConsensusScore(
        @Param("questionId") questionId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): java.math.BigDecimal?
}
