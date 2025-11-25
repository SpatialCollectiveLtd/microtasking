package com.spatialcollective.microtasktoolapi.exception.user

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class YourAccountIsUnauthorizedException(message: String) :
    RuntimeException(String.format("Your account '%s' is not authorized to sign-in", message))

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UserDontHavePermissionException(email: String) :
    RuntimeException(String.format("Your account '%s', doesn't have permission to sign-in", email))