package com.spatialcollective.microtasktoolapi.model

import com.spatialcollective.microtasktoolapi.model.entity.TaskEntity
import com.spatialcollective.microtasktoolapi.utils.extentions.stringFormat


class TaskModel(
    var id: Long = 0,
    var workerUniqueId: String = "",
    var phoneNumber: String = "",
    var startDate: String = "",
    var totalLink: Long = 0,
    var progress: Int = 0,
    var questionName: String = "",
    var questionId: Long = 0,
    var isPaused: Boolean = false
)

fun TaskEntity.toModel(totalLink: Long = 0): TaskModel {
    return TaskModel(
        id = id,
        workerUniqueId = workerUniqueId,
        phoneNumber = phoneNumber,
        startDate = startDate?.stringFormat().orEmpty(),
        totalLink = totalLink,
        progress = progress,
        questionName = question.name,
        questionId = question.id,
        isPaused = question.isPaused
    )
}

