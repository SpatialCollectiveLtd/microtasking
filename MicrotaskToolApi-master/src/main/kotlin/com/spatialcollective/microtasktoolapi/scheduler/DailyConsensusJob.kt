package com.spatialcollective.microtasktoolapi.scheduler

import com.spatialcollective.microtasktoolapi.service.ConsensusService
import com.spatialcollective.microtasktoolapi.repository.QuestionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Component
class DailyConsensusJob {

    @Autowired
    private lateinit var consensusService: ConsensusService

    @Autowired
    private lateinit var questionRepository: QuestionRepository

    private val logger = LoggerFactory.getLogger(DailyConsensusJob::class.java)

    /**
     * Runs every day at 10:00 PM to calculate consensus for all active questions
     * Cron: 0 0 22 * * * (second minute hour day month weekday)
     */
    @Scheduled(cron = "\${dpw.scheduler.daily-consensus:0 0 22 * * *}")
    fun calculateDailyConsensus() {
        logger.info("=== Starting Daily Consensus Calculation Job at ${LocalDateTime.now()} ===")
        
        try {
            // Get all active questions
            val activeQuestions = questionRepository.findAll()
            var processedCount = 0
            var errorCount = 0

            for (question in activeQuestions) {
                try {
                    logger.debug("Calculating consensus for question ID: ${question.id}")
                    consensusService.calculateConsensusForQuestion(question.id)
                    processedCount++
                } catch (e: Exception) {
                    logger.error("Error calculating consensus for question ${question.id}", e)
                    errorCount++
                }
            }

            logger.info("=== Daily Consensus Job Completed: $processedCount successful, $errorCount errors ===")
        } catch (e: Exception) {
            logger.error("Fatal error in Daily Consensus Job", e)
        }
    }
}
