package com.spatialcollective.microtasktoolapi.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class ApiKeyAuthFilter : OncePerRequestFilter() {

    @Value("\${dpw.api.keys}")
    private lateinit var apiKeys: List<String>

    @Value("\${dpw.api.rate-limit.requests-per-minute:60}")
    private val requestsPerMinute: Int = 60

    @Value("\${dpw.api.rate-limit.requests-per-hour:1000}")
    private val requestsPerHour: Int = 1000

    private val logger = LoggerFactory.getLogger(ApiKeyAuthFilter::class.java)
    
    // Rate limiting storage: API Key -> Timestamp -> Request Count
    private val minuteRequestCounts = ConcurrentHashMap<String, ConcurrentHashMap<Long, Int>>()
    private val hourRequestCounts = ConcurrentHashMap<String, ConcurrentHashMap<Long, Int>>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestUri = request.requestURI
        
        // Skip authentication for health check and public endpoints
        if (requestUri.startsWith("/actuator") || 
            requestUri.startsWith("/swagger-ui") || 
            requestUri.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response)
            return
        }

        val apiKey = request.getHeader("X-API-Key")

        if (apiKey.isNullOrBlank()) {
            logger.warn("API request without API key: ${request.remoteAddr} -> $requestUri")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write("""{"status":"error","message":"Missing API key. Include X-API-Key header."}""")
            return
        }

        if (!apiKeys.contains(apiKey)) {
            logger.warn("Invalid API key attempt: ${request.remoteAddr} -> $requestUri")
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.contentType = "application/json"
            response.writer.write("""{"status":"error","message":"Invalid API key"}""")
            return
        }

        // Check rate limits
        if (!checkRateLimit(apiKey)) {
            logger.warn("Rate limit exceeded for API key: $apiKey from ${request.remoteAddr}")
            response.status = 429 // Too Many Requests
            response.contentType = "application/json"
            response.writer.write("""{"status":"error","message":"Rate limit exceeded. Max $requestsPerMinute requests/minute and $requestsPerHour requests/hour."}""")
            return
        }

        // Log successful authentication
        logger.debug("API request authenticated: $requestUri with key ${apiKey.take(10)}...")

        filterChain.doFilter(request, response)
    }

    private fun checkRateLimit(apiKey: String): Boolean {
        val now = System.currentTimeMillis()
        val currentMinute = TimeUnit.MILLISECONDS.toMinutes(now)
        val currentHour = TimeUnit.MILLISECONDS.toHours(now)

        // Initialize maps if not present
        minuteRequestCounts.putIfAbsent(apiKey, ConcurrentHashMap())
        hourRequestCounts.putIfAbsent(apiKey, ConcurrentHashMap())

        val minuteMap = minuteRequestCounts[apiKey]!!
        val hourMap = hourRequestCounts[apiKey]!!

        // Clean up old entries (keep only last 2 minutes and 2 hours)
        minuteMap.keys.removeIf { it < currentMinute - 2 }
        hourMap.keys.removeIf { it < currentHour - 2 }

        // Get current counts
        val minuteCount = minuteMap.getOrDefault(currentMinute, 0)
        val hourCount = hourMap.getOrDefault(currentHour, 0)

        // Check limits
        if (minuteCount >= requestsPerMinute) {
            return false
        }
        if (hourCount >= requestsPerHour) {
            return false
        }

        // Increment counts
        minuteMap[currentMinute] = minuteCount + 1
        hourMap[currentHour] = hourCount + 1

        return true
    }
}
