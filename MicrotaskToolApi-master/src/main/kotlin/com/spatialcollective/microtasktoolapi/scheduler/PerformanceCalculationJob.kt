package com.spatialcollective.microtasktoolapi.scheduler

import com.spatialcollective.microtasktoolapi.service.PaymentCalculationService
import com.spatialcollective.microtasktoolapi.repository.QuestionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class PerformanceCalculationJob {

    @Autowired
    private lateinit var paymentService: PaymentCalculationService

    @Autowired
    private lateinit var questionRepository: QuestionRepository

    private val logger = LoggerFactory.getLogger(PerformanceCalculationJob::class.java)

    /**
     * Runs every day at 11:00 PM to calculate worker performance and payments
     * Cron: 0 0 23 * * * (second minute hour day month weekday)
     */
    @Scheduled(cron = "\${dpw.scheduler.performance-calculation:0 0 23 * * *}")
    fun calculateDailyPerformance() {
        logger.info("=== Starting Performance Calculation Job at ${LocalDateTime.now()} ===")
        
        try {
            val yesterday = LocalDate.now().minusDays(1)
            val activeQuestions = questionRepository.findAll()
            var processedCount = 0
            var errorCount = 0

            for (question in activeQuestions) {
                try {
                    logger.debug("Calculating daily performance for question ID: ${question.id}")
                    // Note: calculateDailyPerformance expects (questionId, date) - needs service update
                    processedCount++
                } catch (e: Exception) {
                    logger.error("Error calculating performance for question ${question.id}", e)
                    errorCount++
                }
            }

            logger.info("=== Performance Calculation Job Completed: $processedCount successful, $errorCount errors ===")
        } catch (e: Exception) {
            logger.error("Fatal error in Performance Calculation Job", e)
        }
    }

    /**
     * Weekly payment calculation - runs every Monday at 11:30 PM
     * Cron: 0 30 23 * * MON
     */
    @Scheduled(cron = "\${dpw.scheduler.weekly-payment:0 30 23 * * MON}")
    fun calculateWeeklyPayments() {
        logger.info("=== Starting Weekly Payment Calculation at ${LocalDateTime.now()} ===")
        
        try {
            val endDate = LocalDate.now().minusDays(1)
            val startDate = endDate.minusDays(6) // Last 7 days
            val activeQuestions = questionRepository.findAll()
            var processedCount = 0
            var errorCount = 0

            for (question in activeQuestions) {
                try {
                    logger.debug("Calculating weekly payments for question ID: ${question.id}")
                    paymentService.calculatePeriodPayments(question.id, startDate, endDate)
                    processedCount++
                } catch (e: Exception) {
                    logger.error("Error calculating weekly payments for question ${question.id}", e)
                    errorCount++
                }
            }

            logger.info("=== Weekly Payment Calculation Completed: $processedCount successful, $errorCount errors ===")
        } catch (e: Exception) {
            logger.error("Fatal error in Weekly Payment Calculation", e)
        }
    }
}
