package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.entity.AnswerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AnswerRepository : JpaRepository<AnswerEntity, Long> {

    fun findByQuestionId(questionId: Long): List<AnswerEntity>

    fun existsByWorkerUniqueIdAndImageIdAndQuestionId(
        workerUniqueId: String,
        imageId: Long,
        questionId: Long
    ): Boolean

    fun countByWorkerUniqueIdAndQuestionId(workerUniqueId: String, questionId: Long): Long
}