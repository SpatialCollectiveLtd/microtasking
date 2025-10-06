package network.model

@kotlinx.serialization.Serializable
data class ImageCountModel(
    var id: Int = 0,
    var url: String = "",
    var createdAt: String = "",
    var totalImage: Long = 0
)