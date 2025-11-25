package com.spatialcollective.microtasktoolapi.service

import com.spatialcollective.microtasktoolapi.model.entity.SystemHealthEntity
import com.spatialcollective.microtasktoolapi.repository.SystemHealthRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import oshi.SystemInfo
import oshi.hardware.CentralProcessor
import oshi.hardware.GlobalMemory
import oshi.hardware.HardwareAbstractionLayer
import java.time.LocalDateTime

@Service
class ServerHealthMonitoringService {

    @Autowired
    private lateinit var healthRepository: SystemHealthRepository

    @Autowired
    private lateinit var alertService: AlertService

    private val logger = LoggerFactory.getLogger(ServerHealthMonitoringService::class.java)
    private val systemInfo = SystemInfo()
    private val hardware: HardwareAbstractionLayer = systemInfo.hardware

    /**
     * Collect current server health metrics
     */
    fun collectHealthMetrics(): Map<String, Any> {
        logger.debug("Collecting server health metrics")

        try {
            val processor: CentralProcessor = hardware.processor
            val memory: GlobalMemory = hardware.memory
            val fileStores = hardware.diskStores

            // CPU usage calculation
            val prevTicks = processor.systemCpuLoadTicks
            Thread.sleep(1000) // Wait 1 second for accurate measurement
            val cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100

            // Memory usage
            val totalMemory = memory.total
            val availableMemory = memory.available
            val usedMemory = totalMemory - availableMemory
            val memoryUsage = (usedMemory.toDouble() / totalMemory.toDouble()) * 100

            // Disk usage (primary disk)
            val totalDisk = fileStores.sumOf { it.size }
            val usableDisk = fileStores.sumOf { it.usableSpace }
            val diskUsage = if (totalDisk > 0) {
                ((totalDisk - usableDisk).toDouble() / totalDisk.toDouble()) * 100
            } else {
                0.0
            }

            // Active connections (placeholder - would need actual implementation)
            val activeConnections = 0

            // Save to database
            val healthEntity = SystemHealthEntity(
                cpuUsage = cpuLoad,
                memoryUsage = memoryUsage,
                diskUsage = diskUsage,
                activeConnections = activeConnections,
                alertSent = false,
                createdAt = LocalDateTime.now()
            )
            healthRepository.save(healthEntity)

            // Check thresholds and send alerts if needed
            checkThresholds(cpuLoad, memoryUsage, diskUsage)

            return mapOf(
                "cpu_usage" to cpuLoad,
                "memory_usage" to memoryUsage,
                "disk_usage" to diskUsage,
                "total_memory_mb" to (totalMemory / 1024 / 1024),
                "used_memory_mb" to (usedMemory / 1024 / 1024),
                "total_disk_gb" to (totalDisk / 1024 / 1024 / 1024),
                "usable_disk_gb" to (usableDisk / 1024 / 1024 / 1024),
                "active_connections" to activeConnections,
                "timestamp" to LocalDateTime.now()
            )
        } catch (e: Exception) {
            logger.error("Error collecting health metrics", e)
            throw e
        }
    }

    /**
     * Check resource thresholds and trigger alerts
     */
    private fun checkThresholds(cpuUsage: Double, memoryUsage: Double, diskUsage: Double) {
        val alerts = mutableListOf<String>()

        if (cpuUsage > 80) {
            alerts.add("CPU usage critical: ${String.format("%.2f", cpuUsage)}%")
        }
        if (memoryUsage > 85) {
            alerts.add("Memory usage critical: ${String.format("%.2f", memoryUsage)}%")
        }
        if (diskUsage > 90) {
            alerts.add("Disk usage critical: ${String.format("%.2f", diskUsage)}%")
        }

        if (alerts.isNotEmpty()) {
            val message = "Server Health Alert:\n" + alerts.joinToString("\n")
            alertService.sendAlert("Server Health Critical", message, "high")
        }
    }

    /**
     * Get current health status
     */
    fun getCurrentHealth(): Map<String, Any> {
        val latestHealth = healthRepository.findTopByOrderByCreatedAtDesc()
        
        return if (latestHealth != null) {
            mapOf(
                "cpu_usage" to latestHealth.cpuUsage,
                "memory_usage" to latestHealth.memoryUsage,
                "disk_usage" to latestHealth.diskUsage,
                "active_connections" to latestHealth.activeConnections,
                "last_check" to latestHealth.createdAt,
                "status" to determineStatus(latestHealth)
            )
        } else {
            mapOf("status" to "unknown", "message" to "No health data available")
        }
    }

    /**
     * Get health history for a time period
     */
    fun getHealthHistory(hours: Int = 24): List<SystemHealthEntity> {
        val since = LocalDateTime.now().minusHours(hours.toLong())
        return healthRepository.findByCreatedAtAfterOrderByCreatedAtDesc(since)
    }

    /**
     * Clean up old health records
     */
    fun cleanupOldRecords(keepDays: Int = 7): Int {
        val cutoffDate = LocalDateTime.now().minusDays(keepDays.toLong())
        return healthRepository.deleteByCreatedAtBefore(cutoffDate)
    }

    /**
     * Determine overall health status
     */
    private fun determineStatus(health: SystemHealthEntity): String {
        return when {
            health.cpuUsage > 90 || health.memoryUsage > 95 || health.diskUsage > 95 -> "critical"
            health.cpuUsage > 80 || health.memoryUsage > 85 || health.diskUsage > 90 -> "warning"
            else -> "healthy"
        }
    }
}
