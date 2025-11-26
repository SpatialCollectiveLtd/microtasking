package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.dto.PaymentCalculationRequest
import com.spatialcollective.microtasktoolapi.service.PaymentCalculationService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PaymentControllerTest {

    @Mock
    private lateinit var paymentService: PaymentCalculationService

    @InjectMocks
    private lateinit var paymentController: PaymentController

    @Test
    fun `calculatePayments should return response with service data`() {
        // Arrange
        val request = PaymentCalculationRequest(
            questionId = 1L,
            startDate = "2025-01-01",
            endDate = "2025-01-31"
        )

        `when`(paymentService.calculatePeriodPayments(
            LocalDate.parse("2025-01-01"),
            LocalDate.parse("2025-01-31"),
            1L
        )).thenReturn(emptyList())

        `when`(paymentService.getPaymentSummary(
            LocalDate.parse("2025-01-01"),
            LocalDate.parse("2025-01-31"),
            1L
        )).thenReturn(mapOf(
            "total_workers" to 0,
            "total_base_pay" to BigDecimal.ZERO,
            "total_bonuses" to BigDecimal.ZERO,
            "total_payment" to BigDecimal.ZERO,
            "by_tier" to emptyMap<String, Int>()
        ))

        // Act
        val response = paymentController.calculatePayments(request)

        // Assert
        assertNotNull(response)
        assertEquals(200, response.statusCodeValue)
        assertNotNull(response.body)
        assertEquals("success", response.body?.status)
    }
}
