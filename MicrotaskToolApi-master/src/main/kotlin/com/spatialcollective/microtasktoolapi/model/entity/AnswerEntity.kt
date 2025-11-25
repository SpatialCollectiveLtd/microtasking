package com.spatialcollective.microtasktoolapi.model.entity

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.spatialcollective.microtasktoolapi.model.audit.DateAudit
import org.hibernate.Hibernate
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable
import javax.persistence.*

@Entity(name = "answer")
@Table(
    indexes = [Index(
        name = "answered_index",
        columnList = "workerUniqueId, imageId, question_id", unique = true
    ), Index(
        name = "question_index",
        columnList = "question_id"
    ), Index(
        name = "done_index",
        columnList = "workerUniqueId, question_id"
    )]
)

data class AnswerEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint(255)")
    var id: Long = 0,
    var imageId: Long = 0,
    var url: String = "",
    var workerUniqueId: String = "",
    var answer: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("question_id")
    var question: QuestionEntity = QuestionEntity()
) : DateAudit(), Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as AnswerEntity

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , imageId = $imageId , url = $url , workerUniqueId = $workerUniqueId , answer = $answer )"
    }
}