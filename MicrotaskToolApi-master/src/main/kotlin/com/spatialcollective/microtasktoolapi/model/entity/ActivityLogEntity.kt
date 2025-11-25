package com.spatialcollective.microtasktoolapi.model.entity

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "activity_log", indexes = [
    Index(name = "idx_timestamp", columnList = "timestamp"),
    Index(name = "idx_user", columnList = "user_id"),
    Index(name = "idx_worker", columnList = "worker_unique_id"),
    Index(name = "idx_action", columnList = "action")
])
class ActivityLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "timestamp", nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now(),

    @Column(name = "user_id")
    var userId: String? = null,

    @Column(name = "worker_unique_id", length = 30)
    var workerUniqueId: String? = null,

    @Column(name = "action", length = 100)
    var action: String? = null,

    @Column(name = "question_id")
    var questionId: Long? = null,

    @Column(name = "metadata", columnDefinition = "JSON")
    var metadata: String? = null,

    @Column(name = "ip_address", length = 50)
    var ipAddress: String? = null,

    @Column(name = "user_agent", columnDefinition = "TEXT")
    var userAgent: String? = null
)
