package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.entity.TaskEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Date

@Repository
interface TaskRepository : JpaRepository<TaskEntity, Long> {

    fun findByQuestionId(questionId: Long): List<TaskEntity>

    fun findTaskByPhoneNumber(phoneNumber: String): List<TaskEntity>

    fun countByQuestionId(questionId: Long): Long

    @Transactional
    @Modifying
    @Query(
        value = "UPDATE task set progress=:progress,  start_date=IFNULL(start_date,:start_date) where id =:id",
        nativeQuery = true
    )
    fun updateProgressById(
        @Param("progress") progress: Int,
        @Param("id") id: Long,
        @Param("start_date") startDate: Date = Date()
    ): Int
}