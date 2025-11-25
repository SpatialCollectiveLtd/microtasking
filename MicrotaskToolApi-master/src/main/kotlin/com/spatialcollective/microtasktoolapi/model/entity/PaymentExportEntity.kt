package com.spatialcollective.microtasktoolapi.model.entity

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "payment_export", indexes = [
    Index(name = "idx_export_date", columnList = "export_date"),
    Index(name = "idx_sync_status", columnList = "dpw_sync_status")
])
class PaymentExportEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "export_date", nullable = false)
    var exportDate: LocalDate = LocalDate.now(),

    @Column(name = "period_start", nullable = false)
    var periodStart: LocalDate = LocalDate.now(),

    @Column(name = "period_end", nullable = false)
    var periodEnd: LocalDate = LocalDate.now(),

    @Column(name = "total_workers")
    var totalWorkers: Int = 0,

    @Column(name = "total_amount", precision = 12, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "export_format", length = 20)
    var exportFormat: String? = null,

    @Column(name = "file_path", length = 512)
    var filePath: String? = null,

    @Column(name = "exported_by")
    var exportedBy: String? = null,

    @Column(name = "exported_at", nullable = false)
    var exportedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "dpw_sync_status", length = 50)
    var dpwSyncStatus: String = "pending",

    @Column(name = "dpw_sync_at")
    var dpwSyncAt: LocalDateTime? = null,

    @Column(name = "dpw_reference")
    var dpwReference: String? = null
)
