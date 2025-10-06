package com.spatialcollective.microtasktoolapi.model.entity

import com.spatialcollective.microtasktoolapi.model.audit.DateAudit
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity(name = "question")
data class QuestionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var name: String = "",
    var isPaused: Boolean = false
) : DateAudit()