package com.spatialcollective.microtasktoolapi.model.entity

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "consensus_result", indexes = [
    Index(name = "idx_question_image", columnList = "question_id,image_id"),
    Index(name = "idx_review", columnList = "requires_review,review_status")
])
class ConsensusResultEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "question_id", nullable = false)
    var questionId: Long = 0,

    @Column(name = "image_id", nullable = false)
    var imageId: Long = 0,

    @Column(name = "ground_truth")
    var groundTruth: String? = null,

    @Column(name = "total_responses")
    var totalResponses: Int = 0,

    @Column(name = "consensus_percentage", precision = 5, scale = 2)
    var consensusPercentage: BigDecimal = BigDecimal.ZERO,

    @Column(name = "requires_review")
    var requiresReview: Boolean = false,

    @Column(name = "review_status", length = 50)
    var reviewStatus: String? = null,

    @Column(name = "reviewed_by")
    var reviewedBy: String? = null,

    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
