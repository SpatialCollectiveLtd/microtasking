package com.spatialcollective.microtasktoolapi.exception

interface AppErrorCode {
    /**
     * Provides an app-specific error code to help find out exactly what happened.
     * It's a human-friendly identifier for a given exception.
     */
    fun errorCode(code:String):String
}