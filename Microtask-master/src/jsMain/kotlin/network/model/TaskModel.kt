package network.model

import kotlinx.serialization.Serializable


@Serializable
data class TaskModel(
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

fun TaskModel.getProgressStatus(): String {
    return when {
        isPaused -> "Paused"
        progress == 0 -> "Not started"
        progress < totalLink -> "In progress"
        else -> "Finished"
    }
}

fun TaskModel.isFinished() = progress.toLong() >= totalLink