package com.spatialcollective.microtasktoolapi.scheduler

import com.spatialcollective.microtasktoolapi.service.QualityFlaggingService
import com.spatialcollective.microtasktoolapi.repository.QuestionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Component
class QualityCheckJob {

    @Autowired
    private lateinit var qualityService: QualityFlaggingService

    @Autowired
    private lateinit var questionRepository: QuestionRepository

    private val logger = LoggerFactory.getLogger(QualityCheckJob::class.java)

    /**
     * Runs every day at 11:30 PM to check for quality issues
     * Cron: 0 30 23 * * * (second minute hour day month weekday)
     */
    @Scheduled(cron = "\${dpw.scheduler.quality-check:0 30 23 * * *}")
    fun runQualityChecks() {
        logger.info("=== Starting Quality Check Job at ${LocalDateTime.now()} ===")
        
        try {
            val activeQuestions = questionRepository.findAll()
            var lowPerformerCount = 0
            var anomalyCount = 0
            var errorCount = 0

            for (question in activeQuestions) {
                try {
                    // Check for low performers (3 consecutive days < 70%)
                    logger.debug("Checking low performers for question ID: ${question.id}")
                    val lowPerformers = qualityService.flagLowPerformers(question.id)
                    lowPerformerCount += lowPerformers.size

                    // Check for anomalous speed patterns
                    logger.debug("Checking anomalous speed for question ID: ${question.id}")
                    val anomalies = qualityService.flagAnomalousSpeed(question.id)
                    anomalyCount += anomalies.size

                } catch (e: Exception) {
                    logger.error("Error running quality checks for question ${question.id}", e)
                    errorCount++
                }
            }

            logger.info("=== Quality Check Job Completed: $lowPerformerCount low performers, $anomalyCount anomalies, $errorCount errors ===")
        } catch (e: Exception) {
            logger.error("Fatal error in Quality Check Job", e)
        }
    }

    /**
     * Clean up resolved flags older than 90 days - runs every Sunday at midnight
     * Cron: 0 0 0 * * SUN
     */
    @Scheduled(cron = "\${dpw.scheduler.cleanup-flags:0 0 0 * * SUN}")
    fun cleanupOldFlags() {
        logger.info("=== Starting Flag Cleanup Job at ${LocalDateTime.now()} ===")
        
        try {
            // This would be implemented in QualityFlaggingService
            // qualityService.cleanupResolvedFlags(90)
            logger.info("=== Flag Cleanup Job Completed ===")
        } catch (e: Exception) {
            logger.error("Fatal error in Flag Cleanup Job", e)
        }
    }
}
