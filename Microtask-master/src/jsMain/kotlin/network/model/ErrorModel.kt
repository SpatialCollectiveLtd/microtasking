package network.model

@kotlinx.serialization.Serializable
data class ErrorModel(
    val timestamp: String = "",
    val status: Int = 0,
    val error: String = "",
    val trace: String = "",
    var message: String = "",
    val path: String = ""
)