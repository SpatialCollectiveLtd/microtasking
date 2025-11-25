package com.spatialcollective.microtasktoolapi.model

import com.spatialcollective.microtasktoolapi.model.entity.LinkEntity

data class ImagePaginationModel(
    val id: Long = 0,
    val url: String = "",
    val totalImage: Int = 0,
    val currentPage: Int = 0,
    val isLast: Boolean = true
)

fun LinkEntity.toImagePaginationModel(totalImage: Int, currentPage: Int, isLast: Boolean): ImagePaginationModel {
    return ImagePaginationModel(
        id = id,
        url = url,
        totalImage = totalImage,
        currentPage = currentPage,
        isLast = isLast
    )
}