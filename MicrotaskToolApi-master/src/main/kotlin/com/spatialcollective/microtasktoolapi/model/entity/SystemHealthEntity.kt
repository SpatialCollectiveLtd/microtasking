package com.spatialcollective.microtasktoolapi.model.entity

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "system_health", indexes = [
    Index(name = "idx_timestamp", columnList = "timestamp"),
    Index(name = "idx_status", columnList = "status,alert_sent")
])
class SystemHealthEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "timestamp", nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now(),

    @Column(name = "metric_type", length = 50)
    var metricType: String? = null,

    @Column(name = "metric_value", precision = 10, scale = 2)
    var metricValue: BigDecimal = BigDecimal.ZERO,

    @Column(name = "status", length = 20)
    var status: String? = null,

    @Column(name = "alert_sent")
    var alertSent: Boolean = false,

    @Column(name = "alert_recipients", columnDefinition = "TEXT")
    var alertRecipients: String? = null
)
