package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.entity.QuestionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface QuestionRepository : JpaRepository<QuestionEntity, Long> {
    @Query(
        value = "SELECT question.id, question.name, question.is_paused, COUNT(Distinct image.id) AS total_image, " +
                "COUNT(Distinct task.id) AS total_worker,question.created_at FROM question " +
                "LEFT JOIN task ON question.id =task.question_id " +
                "LEFT JOIN image ON question.id = image.question_id " +
                "GROUP BY question.id ", nativeQuery = true
    )
    fun findAllCountImageAndLink(): List<Any>
}