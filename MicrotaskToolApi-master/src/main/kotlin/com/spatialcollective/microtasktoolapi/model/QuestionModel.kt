package com.spatialcollective.microtasktoolapi.model

import com.spatialcollective.microtasktoolapi.model.entity.QuestionEntity
import com.spatialcollective.microtasktoolapi.utils.extentions.stringFormat
import java.util.*

data class QuestionModel(
    var id: Long = 0,
    var name: String = "",
    var totalImage: Long = 0,
    var totalWorker: Long = 0,
    var createdAt: String = Date().stringFormat(),
    var isPaused: Boolean = false
)

fun QuestionEntity.toModel(imageCount: Long, taskCount: Long): QuestionModel {
    return QuestionModel(
        id = id,
        name = name,
        createdAt = createdAt.stringFormat(),
        totalImage = imageCount,
        totalWorker = taskCount,
        isPaused = isPaused
    )
}