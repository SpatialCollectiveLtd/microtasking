package com.spatialcollective.microtasktoolapi.scheduler

import com.spatialcollective.microtasktoolapi.service.ServerHealthMonitoringService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Component
class HealthMonitoringJob {

    @Autowired
    private lateinit var healthService: ServerHealthMonitoringService

    private val logger = LoggerFactory.getLogger(HealthMonitoringJob::class.java)

    /**
     * Runs every 5 minutes to monitor server health
     * Cron: 0 */5 * * * * (second minute hour day month weekday)
     */
    @Scheduled(cron = "\${dpw.scheduler.health-monitoring:0 */5 * * * *}")
    fun monitorHealth() {
        logger.debug("=== Running Health Monitoring at ${LocalDateTime.now()} ===")
        
        try {
            val metrics = healthService.collectHealthMetrics()
            
            // Log critical metrics
            val cpuUsage = metrics["cpu_usage"] as? Double ?: 0.0
            val memoryUsage = metrics["memory_usage"] as? Double ?: 0.0
            val diskUsage = metrics["disk_usage"] as? Double ?: 0.0
            
            if (cpuUsage > 80 || memoryUsage > 85 || diskUsage > 90) {
                logger.warn("High resource usage detected - CPU: ${cpuUsage}%, Memory: ${memoryUsage}%, Disk: ${diskUsage}%")
            } else {
                logger.debug("Health check OK - CPU: ${cpuUsage}%, Memory: ${memoryUsage}%, Disk: ${diskUsage}%")
            }
        } catch (e: Exception) {
            logger.error("Error in Health Monitoring Job", e)
        }
    }

    /**
     * Cleanup old health records - runs every day at 2:00 AM
     * Cron: 0 0 2 * * *
     */
    @Scheduled(cron = "\${dpw.scheduler.cleanup-health:0 0 2 * * *}")
    fun cleanupOldHealthRecords() {
        logger.info("=== Starting Health Record Cleanup at ${LocalDateTime.now()} ===")
        
        try {
            val deletedCount = healthService.cleanupOldRecords(7) // Keep last 7 days
            logger.info("=== Health Record Cleanup Completed: $deletedCount records deleted ===")
        } catch (e: Exception) {
            logger.error("Fatal error in Health Record Cleanup", e)
        }
    }
}
