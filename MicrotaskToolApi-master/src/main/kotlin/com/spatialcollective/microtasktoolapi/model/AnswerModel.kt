package com.spatialcollective.microtasktoolapi.model

import com.spatialcollective.microtasktoolapi.model.entity.AnswerEntity
import com.spatialcollective.microtasktoolapi.model.entity.QuestionEntity
import com.spatialcollective.microtasktoolapi.utils.extentions.getName
import com.spatialcollective.microtasktoolapi.utils.extentions.stringFormat


data class AnswerModel(
    var id: Long = 0,
    var taskId: Long = 0,
    var workerProgress: Int = 0,
    var imageId: Long = 0,
    var url: String = "",
    var workerUniqueId: String = "",
    var answer: String = "",
    var date: String = "",
    var questionId: Long = 0
)

fun AnswerEntity.toModel() =
    AnswerModel(
        id = id,
        imageId = imageId,
        url = url,
        workerUniqueId = workerUniqueId,
        answer = answer,
        date = createdAt.stringFormat(),
        questionId = question.id,
    )

fun AnswerEntity.toList() = listOf(id, url.getName(), url, workerUniqueId, answer, createdAt)

fun AnswerModel.toEntity(questionEntity: QuestionEntity) =
    AnswerEntity(
        imageId = imageId,
        url = url,
        workerUniqueId = workerUniqueId,
        answer = answer,
        question = questionEntity
    )


fun AnswerModel.toEntity() =
    AnswerEntity(
        imageId = imageId,
        url = url,
        workerUniqueId = workerUniqueId,
        answer = answer, question = QuestionEntity(id = questionId)
    )