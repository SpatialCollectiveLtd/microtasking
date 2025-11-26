package com.spatialcollective.microtasktoolapi.service

import com.spatialcollective.microtasktoolapi.model.entity.WorkerPerformanceEntity
import com.spatialcollective.microtasktoolapi.repository.AnswerRepository
import com.spatialcollective.microtasktoolapi.repository.ConsensusResultRepository
import com.spatialcollective.microtasktoolapi.repository.WorkerPerformanceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class PaymentCalculationService(
    @Autowired private val workerPerformanceRepository: WorkerPerformanceRepository,
    @Autowired private val answerRepository: AnswerRepository,
    @Autowired private val consensusResultRepository: ConsensusResultRepository
) {
    
    private val logger = LoggerFactory.getLogger(PaymentCalculationService::class.java)
    
    @Value("\${payment.base-pay:760.00}")
    private lateinit var basePayPerDay: String
    
    @Value("\${payment.bonus.excellent:0.30}")
    private lateinit var bonusExcellent: String
    
    @Value("\${payment.bonus.good:0.20}")
    private lateinit var bonusGood: String
    
    @Value("\${payment.bonus.fair:0.10}")
    private lateinit var bonusFair: String
    
    @Value("\${quality.consensus.excellent:90.0}")
    private lateinit var excellentThreshold: String
    
    @Value("\${quality.consensus.good:80.0}")
    private lateinit var goodThreshold: String
    
    @Value("\${quality.consensus.fair:70.0}")
    private lateinit var fairThreshold: String
    
    /**
     * Calculate daily performance and payment for a worker
     */
    fun calculateDailyPerformance(workerId: String, questionId: Long, date: LocalDate): WorkerPerformanceEntity? {
        logger.info("Calculating daily performance for worker $workerId, question $questionId, date $date")
        
        // Get all answers by this worker for this question on this date
        val allAnswers = answerRepository.findByQuestionId(questionId)
        val workerAnswers = allAnswers.filter { 
            it.workerUniqueId == workerId && 
            it.createdAt?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate() == date 
        }
        
        if (workerAnswers.isEmpty()) {
            logger.debug("No answers found for worker $workerId on $date")
            return null
        }
        
        val tasksCompleted = workerAnswers.size
        var correctAnswers = 0
        var incorrectAnswers = 0
        
        // Compare each answer against consensus
        workerAnswers.forEach { answer ->
            val consensus = consensusResultRepository.findByQuestionIdAndImageId(questionId, answer.imageId)
            if (consensus != null && consensus.groundTruth != null) {
                if (answer.answer == consensus.groundTruth) {
                    correctAnswers++
                } else {
                    incorrectAnswers++
                }
            }
        }
        
        // Calculate consensus score
        val consensusScore = if (tasksCompleted > 0) {
            BigDecimal(correctAnswers)
                .divide(BigDecimal(tasksCompleted), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
        
        // Determine quality tier
        val qualityTier = getQualityTier(consensusScore)
        
        // Calculate payment
        val basePay = BigDecimal(basePayPerDay)
        val bonusPercentage = getBonusPercentage(qualityTier)
        val bonusAmount = basePay.multiply(bonusPercentage).setScale(2, RoundingMode.HALF_UP)
        val totalPayment = basePay.add(bonusAmount)
        
        // Check if record exists
        val existing = workerPerformanceRepository.findByWorkerUniqueIdAndDateBetween(
            workerId, date, date
        ).firstOrNull { it.questionId == questionId }
        
        val performance = if (existing != null) {
            existing.apply {
                this.tasksCompleted = tasksCompleted
                this.correctAnswers = correctAnswers
                this.incorrectAnswers = incorrectAnswers
                this.consensusScore = consensusScore
                this.qualityTier = qualityTier
                this.basePay = basePay
                this.bonusAmount = bonusAmount
                this.totalPayment = totalPayment
            }
        } else {
            WorkerPerformanceEntity(
                workerUniqueId = workerId,
                questionId = questionId,
                date = date,
                tasksCompleted = tasksCompleted,
                correctAnswers = correctAnswers,
                incorrectAnswers = incorrectAnswers,
                consensusScore = consensusScore,
                qualityTier = qualityTier,
                basePay = basePay,
                bonusAmount = bonusAmount,
                totalPayment = totalPayment,
                paymentStatus = "pending"
            )
        }
        
        logger.info("Worker $workerId - Tasks: $tasksCompleted, Score: $consensusScore%, Tier: $qualityTier, Payment: $totalPayment")
        
        return workerPerformanceRepository.save(performance)
    }
    
    /**
     * Calculate payments for all workers in a period
     */
    fun calculatePeriodPayments(
        startDate: LocalDate, 
        endDate: LocalDate, 
        questionId: Long?
    ): List<WorkerPerformanceEntity> {
        logger.info("Calculating period payments from $startDate to $endDate, question: $questionId")
        
        val results = mutableListOf<WorkerPerformanceEntity>()
        var currentDate = startDate
        
        while (!currentDate.isAfter(endDate)) {
            val dailyResults = if (questionId != null) {
                calculateDailyPerformanceForQuestion(questionId, currentDate)
            } else {
                // Calculate for all questions
                // This would need to iterate through all active questions
                emptyList()
            }
            results.addAll(dailyResults)
            currentDate = currentDate.plusDays(1)
        }
        
        logger.info("Calculated ${results.size} performance records for period")
        return results
    }
    
    /**
     * Calculate daily performance for all workers on a question
     */
    fun calculateDailyPerformanceForQuestion(questionId: Long, date: LocalDate): List<WorkerPerformanceEntity> {
        logger.info("Calculating daily performance for question $questionId on $date")
        
        // Get all answers for this question on this date
        val allAnswers = answerRepository.findByQuestionId(questionId)
        val dateAnswers = allAnswers.filter { it.createdAt?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate() == date }
        
        // Group by worker
        val answersByWorker = dateAnswers.groupBy { it.workerUniqueId }
        
        val results = mutableListOf<WorkerPerformanceEntity>()
        
        answersByWorker.forEach { (workerId, answers) ->
            val performance = calculateDailyPerformance(workerId, questionId, date)
            if (performance != null) {
                results.add(performance)
            }
        }
        
        logger.info("Calculated performance for ${results.size} workers")
        return results
    }
    
    /**
     * Approve payments for processing
     */
    fun approvePayments(paymentIds: List<Long>, approvedBy: String, paymentReference: String?): Int {
        logger.info("Approving ${paymentIds.size} payments by $approvedBy")
        
        var approvedCount = 0
        
        paymentIds.forEach { id ->
            val performance = workerPerformanceRepository.findById(id).orElse(null)
            if (performance != null && performance.paymentStatus == "pending") {
                performance.paymentStatus = "approved"
                performance.paymentReference = paymentReference
                workerPerformanceRepository.save(performance)
                approvedCount++
            }
        }
        
        logger.info("Approved $approvedCount payments")
        return approvedCount
    }
    
    /**
     * Update payment status after processing
     */
    fun updatePaymentStatus(
        paymentIds: List<Long>, 
        status: String, 
        transactionIds: List<String>?
    ): Int {
        logger.info("Updating payment status to $status for ${paymentIds.size} payments")
        
        var updatedCount = 0
        
        paymentIds.forEachIndexed { index, id ->
            val performance = workerPerformanceRepository.findById(id).orElse(null)
            if (performance != null) {
                performance.paymentStatus = status
                if (!transactionIds.isNullOrEmpty() && index < transactionIds.size) {
                    performance.paymentReference = transactionIds[index]
                }
                workerPerformanceRepository.save(performance)
                updatedCount++
            }
        }
        
        logger.info("Updated $updatedCount payment statuses")
        return updatedCount
    }
    
    /**
     * Get quality tier based on consensus score
     */
    private fun getQualityTier(consensusScore: BigDecimal): String {
        val score = consensusScore.toDouble()
        return when {
            score >= excellentThreshold.toDouble() -> "excellent"
            score >= goodThreshold.toDouble() -> "good"
            score >= fairThreshold.toDouble() -> "fair"
            else -> "poor"
        }
    }
    
    /**
     * Get bonus percentage based on quality tier
     */
    private fun getBonusPercentage(qualityTier: String): BigDecimal {
        return when (qualityTier) {
            "excellent" -> BigDecimal(bonusExcellent)
            "good" -> BigDecimal(bonusGood)
            "fair" -> BigDecimal(bonusFair)
            else -> BigDecimal.ZERO
        }
    }
    
    /**
     * Get payment summary for a period
     */
    fun getPaymentSummary(startDate: LocalDate, endDate: LocalDate, questionId: Long?): Map<String, Any> {
        val performances = if (questionId != null) {
            workerPerformanceRepository.findByQuestionIdAndDate(questionId, startDate)
                .filter { it.date in startDate..endDate }
        } else {
            workerPerformanceRepository.findByDateBetweenAndPaymentStatus(startDate, endDate, "pending")
        }
        
        val totalWorkers = performances.map { it.workerUniqueId }.distinct().size
        val totalBasePay = performances.sumOf { it.basePay }
        val totalBonuses = performances.sumOf { it.bonusAmount }
        val totalPayment = performances.sumOf { it.totalPayment }
        
        val byTier = performances.groupBy { it.qualityTier }
        
        return mapOf(
            "period_start" to startDate,
            "period_end" to endDate,
            "total_workers" to totalWorkers,
            "total_base_pay" to totalBasePay,
            "total_bonuses" to totalBonuses,
            "total_payment" to totalPayment,
            "by_tier" to mapOf(
                "excellent" to byTier["excellent"]?.size ?: 0,
                "good" to byTier["good"]?.size ?: 0,
                "fair" to byTier["fair"]?.size ?: 0,
                "poor" to byTier["poor"]?.size ?: 0
            )
        )
    }
}
