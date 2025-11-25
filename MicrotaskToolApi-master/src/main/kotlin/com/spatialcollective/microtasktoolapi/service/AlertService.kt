package com.spatialcollective.microtasktoolapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class AlertService {

    @Autowired
    private lateinit var mailSender: JavaMailSender

    @Value("\${dpw.alerts.email.from}")
    private lateinit var fromEmail: String

    @Value("\${dpw.alerts.email.to}")
    private lateinit var toEmails: List<String>

    @Value("\${dpw.alerts.email.enabled:true}")
    private var emailEnabled: Boolean = true

    @Value("\${dpw.alerts.cooldown-minutes:30}")
    private var cooldownMinutes: Int = 30

    private val logger = LoggerFactory.getLogger(AlertService::class.java)
    
    // Track last alert time for each alert type to prevent spam
    private val lastAlertTime = ConcurrentHashMap<String, LocalDateTime>()

    /**
     * Send an alert via email
     */
    fun sendAlert(subject: String, message: String, priority: String = "medium") {
        logger.info("Alert triggered: $subject (Priority: $priority)")

        // Check cooldown period
        val alertKey = "$subject:$priority"
        val lastAlert = lastAlertTime[alertKey]
        if (lastAlert != null) {
            val minutesSinceLastAlert = java.time.Duration.between(lastAlert, LocalDateTime.now()).toMinutes()
            if (minutesSinceLastAlert < cooldownMinutes) {
                logger.debug("Alert suppressed due to cooldown: $subject (${cooldownMinutes - minutesSinceLastAlert} minutes remaining)")
                return
            }
        }

        // Send email if enabled
        if (emailEnabled) {
            try {
                val mailMessage = SimpleMailMessage()
                mailMessage.setFrom(fromEmail)
                mailMessage.setTo(*toEmails.toTypedArray())
                mailMessage.subject = "[$priority] Microtasking Platform Alert: $subject"
                mailMessage.text = """
                    |Alert Details:
                    |-------------
                    |Subject: $subject
                    |Priority: $priority
                    |Time: ${LocalDateTime.now()}
                    |
                    |Message:
                    |$message
                    |
                    |---
                    |Microtasking Platform
                    |http://micro.spatialcollective.co.ke:8080
                """.trimMargin()

                mailSender.send(mailMessage)
                logger.info("Alert email sent successfully to: ${toEmails.joinToString()}")
                
                // Update last alert time
                lastAlertTime[alertKey] = LocalDateTime.now()
            } catch (e: Exception) {
                logger.error("Failed to send alert email", e)
            }
        } else {
            logger.warn("Email alerts disabled. Alert would have been sent: $subject")
        }
    }

    /**
     * Send resource threshold alert
     */
    fun sendResourceAlert(resourceType: String, usage: Double, threshold: Double) {
        val subject = "$resourceType Usage Alert"
        val message = """
            |$resourceType usage has exceeded threshold:
            |Current: ${String.format("%.2f", usage)}%
            |Threshold: ${String.format("%.2f", threshold)}%
            |
            |Immediate attention may be required.
        """.trimMargin()

        val priority = if (usage > threshold * 1.2) "high" else "medium"
        sendAlert(subject, message, priority)
    }

    /**
     * Send quality issue alert
     */
    fun sendQualityAlert(workerId: String, questionId: Long, issueType: String, details: String) {
        val subject = "Quality Issue Detected"
        val message = """
            |Worker Quality Issue:
            |Worker ID: $workerId
            |Question ID: $questionId
            |Issue Type: $issueType
            |
            |Details:
            |$details
        """.trimMargin()

        sendAlert(subject, message, "medium")
    }

    /**
     * Send payment alert
     */
    fun sendPaymentAlert(eventType: String, details: String) {
        val subject = "Payment Event: $eventType"
        val message = """
            |Payment Event Notification:
            |Event: $eventType
            |
            |Details:
            |$details
        """.trimMargin()

        sendAlert(subject, message, "low")
    }

    /**
     * Send consensus alert
     */
    fun sendConsensusAlert(questionId: Long, consensusPercentage: Double, threshold: Double) {
        val subject = "Low Consensus Alert"
        val message = """
            |Low consensus detected for question:
            |Question ID: $questionId
            |Consensus: ${String.format("%.2f", consensusPercentage)}%
            |Threshold: ${String.format("%.2f", threshold)}%
            |
            |This may indicate data quality issues or unclear question.
        """.trimMargin()

        sendAlert(subject, message, "medium")
    }

    /**
     * Send system error alert
     */
    fun sendErrorAlert(errorType: String, errorMessage: String, stackTrace: String? = null) {
        val subject = "System Error: $errorType"
        val message = """
            |System Error Occurred:
            |Error Type: $errorType
            |Error Message: $errorMessage
            |
            |${if (stackTrace != null) "Stack Trace:\n$stackTrace" else ""}
        """.trimMargin()

        sendAlert(subject, message, "high")
    }
}
