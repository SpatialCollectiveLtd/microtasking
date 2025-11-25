package com.spatialcollective.microtasktoolapi.model.entity

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "worker_performance", 
    uniqueConstraints = [UniqueConstraint(columnNames = ["worker_unique_id", "question_id", "date"])],
    indexes = [
        Index(name = "idx_worker", columnList = "worker_unique_id"),
        Index(name = "idx_date", columnList = "date"),
        Index(name = "idx_payment", columnList = "payment_status,date")
    ]
)
class WorkerPerformanceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "worker_unique_id", length = 30, nullable = false)
    var workerUniqueId: String = "",

    @Column(name = "question_id", nullable = false)
    var questionId: Long = 0,

    @Column(name = "date", nullable = false)
    var date: LocalDate = LocalDate.now(),

    @Column(name = "tasks_completed")
    var tasksCompleted: Int = 0,

    @Column(name = "correct_answers")
    var correctAnswers: Int = 0,

    @Column(name = "incorrect_answers")
    var incorrectAnswers: Int = 0,

    @Column(name = "consensus_score", precision = 5, scale = 2)
    var consensusScore: BigDecimal = BigDecimal.ZERO,

    @Column(name = "average_time_per_task", precision = 10, scale = 2)
    var averageTimePerTask: BigDecimal = BigDecimal.ZERO,

    @Column(name = "flagged_tasks")
    var flaggedTasks: Int = 0,

    @Column(name = "quality_tier", length = 20)
    var qualityTier: String? = null,

    @Column(name = "base_pay", precision = 10, scale = 2)
    var basePay: BigDecimal = BigDecimal.ZERO,

    @Column(name = "bonus_amount", precision = 10, scale = 2)
    var bonusAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_payment", precision = 10, scale = 2)
    var totalPayment: BigDecimal = BigDecimal.ZERO,

    @Column(name = "payment_status", length = 50)
    var paymentStatus: String = "pending",

    @Column(name = "payment_reference")
    var paymentReference: String? = null,

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
