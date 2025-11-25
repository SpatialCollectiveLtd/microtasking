package network.model

import kotlinx.serialization.Serializable
import ui.screens.userFeatures.task.model.Answer

@Serializable
data class ImagePaginationModel(
    val id: Long = 0,
    val url: String = "",
    val totalImage: Int = 1,
    val currentPage: Int = 0,
    val isLast: Boolean = true
)

fun ImagePaginationModel.constructAnswerModel(answer: Answer, taskModel: TaskModel): AnswerModel {
    return AnswerModel(
        taskId = taskModel.id,
        workerProgress = currentPage,
        imageId = id,
        url = url,
        workerUniqueId = taskModel.workerUniqueId,
        answer = answer.value,
        questionId = taskModel.questionId
    )
}

fun ImagePaginationModel.finished() = currentPage >= totalImage