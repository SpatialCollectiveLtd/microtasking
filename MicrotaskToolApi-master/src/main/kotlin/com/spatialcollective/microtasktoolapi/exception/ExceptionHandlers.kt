package com.spatialcollective.microtasktoolapi.exception

class ExceptionHandlers : RuntimeException, AppErrorCode {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)


    override fun errorCode(code: String): String {
        return code
    }
}