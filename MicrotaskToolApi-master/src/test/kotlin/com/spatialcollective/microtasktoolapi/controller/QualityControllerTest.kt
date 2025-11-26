package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.dto.CreateFlagRequest
import com.spatialcollective.microtasktoolapi.model.entity.QualityFlagEntity
import com.spatialcollective.microtasktoolapi.service.QualityFlaggingService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class QualityControllerTest {

    @Mock
    private lateinit var qualityService: QualityFlaggingService

    @InjectMocks
    private lateinit var qualityController: QualityController

    @Test
    fun `getQualityFlags should return response with flags`() {
        // Arrange
        `when`(qualityService.getQualityFlags(null, 1L, null, null)).thenReturn(mapOf(
            "flags" to emptyList<Map<String, Any>>(),
            "statistics" to mapOf(
                "total_flags" to 0,
                "by_severity" to emptyMap<String, Int>()
            )
        ))

        // Act
        val response = qualityController.getQualityFlags(null, 1L, null, null)

        // Assert
        assertNotNull(response)
        assertEquals(200, response.statusCodeValue)
        assertEquals("success", response.body?.status)
    }

    @Test
    fun `createFlag should create flag and return response`() {
        // Arrange
        val request = CreateFlagRequest(
            workerId = "worker123",
            questionId = 1L,
            flagType = "manual",
            severity = "medium",
            description = "Test flag",
            flaggedBy = "admin"
        )

        val mockFlag = QualityFlagEntity(
            workerUniqueId = "worker123",
            questionId = 1L,
            flagType = "manual",
            severity = "medium",
            description = "Test flag",
            resolved = false
        )
        mockFlag.id = 1L

        `when`(qualityService.createManualFlag(
            "worker123", 1L, "Test flag", "medium", "admin"
        )).thenReturn(mockFlag)

        // Act
        val response = qualityController.createFlag(request)

        // Assert
        assertNotNull(response)
        assertEquals(200, response.statusCodeValue)
        assertEquals("success", response.body?.status)
        assertEquals(1L, response.body?.data?.flagId)
    }
}
