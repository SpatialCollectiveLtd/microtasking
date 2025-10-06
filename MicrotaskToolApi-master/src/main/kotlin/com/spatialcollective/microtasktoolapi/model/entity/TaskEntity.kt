package com.spatialcollective.microtasktoolapi.model.entity

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.spatialcollective.microtasktoolapi.model.audit.DateAudit
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
import javax.persistence.*

@Entity(name = "task")
class TaskEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(columnDefinition = "varchar(30)")
    var workerUniqueId: String = "",
    @Column(columnDefinition = "varchar(20)")
    var phoneNumber: String = "",
    var startDate: Date? = null,
    var progress: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("question_id")
    var question: QuestionEntity = QuestionEntity()
) : DateAudit()