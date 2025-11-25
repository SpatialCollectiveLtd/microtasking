package ui.screens.adminFeatures.questionDetails.model

import network.model.ToastModel
import network.model.ToastType

data class QuestionErrorModel(val title: String = "", var message: String = "")

fun QuestionErrorModel.isNotEmpty() = title.isNotEmpty() && message.isNotEmpty()

fun QuestionErrorModel.toToastModel(): ToastModel? {
    return if (isNotEmpty()) {
        ToastModel(title, message, ToastType.ERROR)
    } else null
}

