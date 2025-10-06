package com.spatialcollective.microtasktoolapi.exception

/**
 * This exception is thrown when the Resources can't be found in the application if searching by ID.
 */
class ResourceNotFoundException : RuntimeException, AppErrorCode {
    private val serialVersionUID = 1L
    constructor(message: String) : super(message)
    constructor(code: String,message: String, cause: Throwable) : super(message, cause)


    override fun errorCode(code: String): String {
        return code
    }
}