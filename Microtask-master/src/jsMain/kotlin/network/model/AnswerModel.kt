package network.model

import kotlinx.serialization.Serializable

@Serializable
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