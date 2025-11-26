package com.spatialcollective.microtasktoolapi.service

import com.spatialcollective.microtasktoolapi.model.entity.SystemHealthEntity
import com.spatialcollective.microtasktoolapi.repository.SystemHealthRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.math.BigDecimal
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
            val diskStores = hardware.diskStores
            val totalDisk = diskStores.sumOf { it.size }
            // Note: diskStores don't have usableSpace, using approximate calculation
            val diskUsage = 0.0 // Placeholder - would need OS-specific implementation

            // Active connections (placeholder - would need actual implementation)
            val activeConnections = 0

            // Save to database (save 3 separate records for CPU, memory, disk)
            val cpuEntity = SystemHealthEntity(
                metricType = "cpu_usage",
                metricValue = BigDecimal.valueOf(cpuLoad),
                status = if (cpuLoad > 80) "critical" else if (cpuLoad > 60) "warning" else "healthy",
                alertSent = false
            )
            val memoryEntity = SystemHealthEntity(
                metricType = "memory_usage",
                metricValue = BigDecimal.valueOf(memoryUsage),
                status = if (memoryUsage > 85) "critical" else if (memoryUsage > 70) "warning" else "healthy",
                alertSent = false
            )
            val diskEntity = SystemHealthEntity(
                metricType = "disk_usage",
                metricValue = BigDecimal.valueOf(diskUsage),
                status = if (diskUsage > 90) "critical" else if (diskUsage > 75) "warning" else "healthy",
                alertSent = false
            )
            healthRepository.save(cpuEntity)
            healthRepository.save(memoryEntity)
            healthRepository.save(diskEntity)

            // Check thresholds and send alerts if needed
            checkThresholds(cpuLoad, memoryUsage, diskUsage)

            return mapOf(
                "cpu_usage" to cpuLoad,
                "memory_usage" to memoryUsage,
                "disk_usage" to diskUsage,
                "total_memory_mb" to (totalMemory / 1024 / 1024),
                "used_memory_mb" to (usedMemory / 1024 / 1024),
                "total_disk_gb" to (totalDisk / 1024 / 1024 / 1024),
                "usable_disk_gb" to 0,
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
        val latestHealth = healthRepository.findTopByOrderByTimestampDesc()
        
        return if (latestHealth != null) {
            mapOf(
                "metric_type" to (latestHealth.metricType ?: "unknown"),
                "metric_value" to latestHealth.metricValue,
                "status" to (latestHealth.status ?: "unknown"),
                "last_check" to latestHealth.timestamp
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
        return healthRepository.findByTimestampAfterOrderByTimestampDesc(since)
    }

    /**
     * Clean up old health records
     */
    fun cleanupOldRecords(keepDays: Int = 7): Int {
        val cutoffDate = LocalDateTime.now().minusDays(keepDays.toLong())
        return healthRepository.deleteByTimestampBefore(cutoffDate)
    }

}
