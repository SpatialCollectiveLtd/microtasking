package com.spatialcollective.microtasktoolapi.model

data class ImageCountDto(
    var url: String = "",
    var createdAt: String = "",
    var totalImage: Long = 0
)

fun ImageCountDto.toModel(id: Int) = ImageCountModel(id, url, createdAt, totalImage)


data class ImageCountModel(
    var id: Int = 0,
    var url: String = "",
    var createdAt: String = "",
    var totalImage: Long = 0
)