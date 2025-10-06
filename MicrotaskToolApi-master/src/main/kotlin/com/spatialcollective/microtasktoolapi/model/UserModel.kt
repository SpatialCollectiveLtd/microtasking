package com.spatialcollective.microtasktoolapi.model

import arrow.core.Either
import arrow.core.getOrElse
import com.spatialcollective.microtasktoolapi.model.entity.UserEntity
import io.github.nefilim.kjwt.DecodedJWT
import io.github.nefilim.kjwt.JWSAlgorithm
import io.github.nefilim.kjwt.KJWTVerificationError

data class UserModel(
    var id: String = "",
    var fullName: String = "",
    var email: String = "",
    var picture: String = "",
    var role: String = UserRole.Worker.name
)

fun Either<KJWTVerificationError, DecodedJWT<out JWSAlgorithm>>.toUserEntity(): UserEntity {
    val userEntity = UserEntity()
    tap {
        userEntity.apply {
            id = it.claimValue("sub").getOrElse { "" }
            email = it.claimValue("email").getOrElse { "" }
            fullName = it.claimValue("name").getOrElse { "" }
            picture = it.claimValue("picture").getOrElse { "" }
        }
    }
    return userEntity
}

fun UserEntity.toModel() = UserModel(id, fullName, email, picture, role)
