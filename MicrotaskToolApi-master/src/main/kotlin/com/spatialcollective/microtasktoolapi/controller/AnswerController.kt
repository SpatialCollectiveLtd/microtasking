package com.spatialcollective.microtasktoolapi.controller

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.spatialcollective.microtasktoolapi.exception.ResourceNotFoundException
import com.spatialcollective.microtasktoolapi.model.AnswerModel
import com.spatialcollective.microtasktoolapi.model.entity.AnswerEntity
import com.spatialcollective.microtasktoolapi.model.toEntity
import com.spatialcollective.microtasktoolapi.model.toList
import com.spatialcollective.microtasktoolapi.repository.AnswerRepository
import com.spatialcollective.microtasktoolapi.repository.QuestionRepository
import com.spatialcollective.microtasktoolapi.repository.TaskRepository
import com.spatialcollective.microtasktoolapi.utils.extentions.stringFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.validation.Valid

@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RestController
@RequestMapping("/")
class AnswerController(
    @Autowired private val answerRepository: AnswerRepository,
    @Autowired private val questionRepository: QuestionRepository,
    @Autowired private val taskRepository: TaskRepository
) {

    @GetMapping("/answers/{questionId}")
    fun getAnswersByQuestionId(@PathVariable questionId: Long): ResponseEntity<Any> {
        val answers = answerRepository.findByQuestionId(questionId).map { it.toList() }
        val reportFile = File("${Date().stringFormat()}_$questionId.csv")
        val csvHeader = listOf("id", "ImageName", "Url","WorkerUniqueId", "Answer", "TimesTemp")

        csvWriter().open(reportFile, append = true) {
            writeRow(csvHeader)
            writeRows(answers)
        }

        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${reportFile.name}")
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv")

        val resource = InputStreamResource(FileInputStream(reportFile))
        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(reportFile.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }

    @PostMapping("/answer", consumes = ["application/json"])
    fun createAnswer(@Valid @RequestBody answer: AnswerModel): Int {
        return questionRepository.findById(answer.questionId).map {question->
            if(question.isPaused){ throw RuntimeException("Sorry, this question has been paused") }
            val answerEntity = answer.toEntity(question)
            val foundAnswer = answerRepository.existsByWorkerUniqueIdAndImageIdAndQuestionId(
                answer.workerUniqueId,
                answer.imageId,
                answer.questionId
            )
            if (foundAnswer) {
                val totalAnswer = recalculateAnsweredQuestions(answerEntity).toInt()
                taskRepository.updateProgressById(totalAnswer, answer.taskId)
                totalAnswer.plus(1)
            } else {
                answerRepository.save(answerEntity)
                val workerProgress = answer.workerProgress.plus(1)
                taskRepository.updateProgressById(workerProgress, answer.taskId)
                workerProgress
            }
        }.orElseThrow { ResourceNotFoundException("Question with id ${answer.questionId} not found") }
    }

    private fun recalculateAnsweredQuestions(answer: AnswerEntity): Long {
        return answerRepository.countByWorkerUniqueIdAndQuestionId(answer.workerUniqueId, answer.question.id)
    }
}