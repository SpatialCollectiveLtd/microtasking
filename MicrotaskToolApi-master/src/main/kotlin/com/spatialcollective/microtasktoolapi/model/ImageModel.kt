package com.spatialcollective.microtasktoolapi.model

import com.spatialcollective.microtasktoolapi.model.entity.LinkEntity


data class ImageModel(
    var id: Long = 0,
    var url: String = "",
    var questionId: Long = 0
)

fun LinkEntity.toModel(): ImageModel {
    return ImageModel(id = id, url = url, questionId = question.id)
}