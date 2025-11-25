package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.entity.PaymentExportEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface PaymentExportRepository : JpaRepository<PaymentExportEntity, Long> {
    
    fun findByExportDate(exportDate: LocalDate): List<PaymentExportEntity>
    
    fun findByPeriodStartAndPeriodEnd(periodStart: LocalDate, periodEnd: LocalDate): PaymentExportEntity?
    
    fun findByDpwSyncStatus(dpwSyncStatus: String): List<PaymentExportEntity>
    
    fun findByExportedBy(exportedBy: String): List<PaymentExportEntity>
    
    @Query("SELECT p FROM payment_export p WHERE p.exportDate BETWEEN :startDate AND :endDate ORDER BY p.exportDate DESC")
    fun findByExportDateRange(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<PaymentExportEntity>
    
    @Query("SELECT p FROM payment_export p WHERE p.dpwSyncStatus = 'pending' ORDER BY p.exportedAt ASC")
    fun findPendingSyncOrderByExportedAt(): List<PaymentExportEntity>
}
