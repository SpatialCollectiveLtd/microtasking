package network.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionModel(
    var id: Long = 0,
    var name: String = "",
    var isPaused: Boolean = false,
    var totalImage: Long = 0,
    var totalWorker: Long = 0,
    var createdAt: String = String()
)