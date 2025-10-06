package com.spatialcollective.microtasktoolapi.model.entity

import com.spatialcollective.microtasktoolapi.model.UserRole
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "user")
class UserEntity(
    @Id
    var id: String = "",
    var fullName: String = "",
    var email: String = "",
    var picture: String = "",
    var role: String = UserRole.Worker.name
)

fun UserEntity.isAdmin() = role == UserRole.Admin.name