package com.spatialcollective.microtasktoolapi.exception

import com.spatialcollective.microtasktoolapi.model.payload.toErrorModel
import com.spatialcollective.microtasktoolapi.model.payload.toHasMap
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.web.context.request.WebRequest

internal class AppErrorAttributes : DefaultErrorAttributes() {
    override fun getErrorAttributes(webRequest: WebRequest, options: ErrorAttributeOptions): MutableMap<String, Any> {
        val option = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)
        val defaultErrorAttributes: Map<String, Any> = super.getErrorAttributes(webRequest, option)
        println("AppErrorAttributes")
        return defaultErrorAttributes.toErrorModel().toHasMap()
    }
}


