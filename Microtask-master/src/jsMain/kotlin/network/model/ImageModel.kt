package network.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageModel(
    var id: Long = 0,
    var name: String = "",
    var questionId: Int =0
)