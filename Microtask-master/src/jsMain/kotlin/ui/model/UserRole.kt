package ui.model

enum class UserRole {
    Admin, Worker
}

fun UserRole?.isAdmin() = this == UserRole.Admin
