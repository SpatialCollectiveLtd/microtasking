package com.spatialcollective.microtasktoolapi.model.entity

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "quality_flags", indexes = [
    Index(name = "idx_worker", columnList = "worker_unique_id"),
    Index(name = "idx_resolved", columnList = "resolved")
])
class QualityFlagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "worker_unique_id", length = 30, nullable = false)
    var workerUniqueId: String = "",

    @Column(name = "question_id", nullable = false)
    var questionId: Long = 0,

    @Column(name = "flag_type", length = 50)
    var flagType: String? = null,

    @Column(name = "severity", length = 20)
    var severity: String? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "flagged_at", nullable = false)
    var flaggedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "resolved")
    var resolved: Boolean = false,

    @Column(name = "resolved_by")
    var resolvedBy: String? = null,

    @Column(name = "resolved_at")
    var resolvedAt: LocalDateTime? = null,

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    var resolutionNotes: String? = null
)
