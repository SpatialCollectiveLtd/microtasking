package network.model

data class ToastModel(val title: String = "", val description: String = "", val type: ToastType = ToastType.SUCCESS)

fun ToastModel.isNotEmpty() = title.isNotEmpty() && description.isNotEmpty()

enum class ToastType(val type: String) {
    SUCCESS("bg-success"), ERROR("bg-danger")
}