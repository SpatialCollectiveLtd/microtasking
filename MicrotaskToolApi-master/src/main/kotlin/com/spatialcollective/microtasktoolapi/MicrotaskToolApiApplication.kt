package com.spatialcollective.microtasktoolapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
class MicrotaskToolApiApplication: WebMvcConfigurer {
        override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver?>) {
                argumentResolvers.add(PageableHandlerMethodArgumentResolver())
        }
}

fun main(args: Array<String>) {
        runApplication<MicrotaskToolApiApplication>(*args)
}
