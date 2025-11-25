package com.spatialcollective.microtasktoolapi.model.payload

data class ApiResponse(
        var timestamp:String= "",
        var status: String = "",
        var error: String = "",
        var message: String = "",
        var path: String = "",
        var isSuccess: Boolean = false
        //val cause: String = ""
)

fun ApiResponse.toHasMap(): HashMap<String, Any> {
        return hashMapOf(
                "timestamp" to timestamp,
                "status" to status,
                "error" to error,
                "message" to message,
                "path" to path
        )
}

fun Map<String, Any>.toErrorModel(): ApiResponse {
        return ApiResponse(
                this["timestamp"].toString(),
                this["status"].toString(),
                this.getOrDefault("error", "no reason available") as String,
                this.getOrDefault("message", "no message available") as String,
                this.getOrDefault("path", "no domain available") as String
        )
}