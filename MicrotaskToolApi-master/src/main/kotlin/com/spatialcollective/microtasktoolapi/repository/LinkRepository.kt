package com.spatialcollective.microtasktoolapi.repository

import com.spatialcollective.microtasktoolapi.model.ImageCountDto
import com.spatialcollective.microtasktoolapi.model.entity.LinkEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface LinkRepository : JpaRepository<LinkEntity, Long> {

    fun findByQuestionId(questionId: Long, pageable: Pageable): Page<LinkEntity>

    fun countByQuestionId(questionId: Long): Long

    @Query(value = "SELECT new com.spatialcollective.microtasktoolapi.model.ImageCountDto(url, createdAt, COUNT(createdAt)) from image where question_id =:question_id  GROUP by createdAt")
    fun findByQuestionIdAndGroupByCreatedAt(@Param("question_id") questionId: Long): List<ImageCountDto>

    @Transactional
    fun deleteByQuestionIdAndCreatedAt(question_id: Long, createdAt: String): Long
}