package network.model

import ui.model.UserRole

@kotlinx.serialization.Serializable
data class UserModel(
    var id: String = "",
    var fullName: String = "",
    var email: String = "",
    var picture: String = "",
    var role: String = UserRole.Worker.name
)


fun UserModel.isEmpty(): Boolean {
    return id.isEmpty() && fullName.isEmpty() && email.isEmpty()
}
