package com.spatialcollective.microtasktoolapi.controller


import com.spatialcollective.microtasktoolapi.exception.user.UserDontHavePermissionException
import com.spatialcollective.microtasktoolapi.exception.user.YourAccountIsUnauthorizedException
import com.spatialcollective.microtasktoolapi.model.entity.UserEntity
import com.spatialcollective.microtasktoolapi.model.entity.isAdmin
import com.spatialcollective.microtasktoolapi.model.toUserEntity
import com.spatialcollective.microtasktoolapi.repository.UserRepository
import com.spatialcollective.microtasktoolapi.utils.extentions.isNotNull
import io.github.nefilim.kjwt.JWT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RestController
@RequestMapping("/")
class UserController(@Autowired val userRepository: UserRepository) {

    @PostMapping("user/token")
    fun createUser(@RequestParam("token") token: String): UserEntity {
        val decodedEntity = JWT.decode(token).toUserEntity()
        return if (decodedEntity.id.isNotEmpty()) {
            if (userRepository.existsById(decodedEntity.id)) {
                decodedEntity
            } else {
                userRepository.save(decodedEntity)
            }
        } else {
            throw RuntimeException("Invalid token (${token})")
        }
    }

    @PostMapping(path = ["/user/sign-in"])
    fun signIn(@RequestParam("token") token: String?): UserEntity? {
        if (token.isNotNull()) {
            val userEntity = JWT.decode(token!!).toUserEntity()
            return if (userEntity.id.isNotEmpty()) {
                userRepository.findById(userEntity.id)
                    .map {
                        if (it.isAdmin()) {
                            it
                        } else {
                            throw UserDontHavePermissionException(userEntity.email)
                        }
                    }
                    .orElseThrow { YourAccountIsUnauthorizedException(userEntity.email) }
            } else {
                throw YourAccountIsUnauthorizedException(userEntity.email)
            }
        } else {
            throw RuntimeException("Invalid token")
        }
    }
}


