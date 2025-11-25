package com.spatialcollective.microtasktoolapi.exception

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConditionalOnProperty(name = ["microtasktoolapi.errors.attributes"], havingValue = "true")
@Configuration
class WebErrorConfiguration {

    /**
     * We override the default ]
     *
     * @return A custom implementation of ErrorAttributes
     */
    @Bean
    fun errorAttributes(): ErrorAttributes {
        return AppErrorAttributes()
    }
}