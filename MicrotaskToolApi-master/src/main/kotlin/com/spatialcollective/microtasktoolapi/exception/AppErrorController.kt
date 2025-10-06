package com.spatialcollective.microtasktoolapi.exception

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@ConditionalOnProperty(name = ["microtasktool.errors.controller"], havingValue = "true")
@RestController
@RequestMapping(AppErrorController.ERROR_PATH)
class AppErrorController(errorAttributes: ErrorAttributes?) : AbstractErrorController(errorAttributes, emptyList()) {
    @RequestMapping
    fun error(request: HttpServletRequest?): ResponseEntity<Map<String, Any>> {
        val body = this.getErrorAttributes(request, ErrorAttributeOptions.defaults())
        val status = getStatus(request)
        println("AppErrorController")
        return ResponseEntity(body, status)
    }

    companion object {
        const val ERROR_PATH = "/error"
    }
}