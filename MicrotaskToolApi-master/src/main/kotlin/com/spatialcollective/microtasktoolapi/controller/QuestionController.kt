package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.exception.ResourceNotFoundException
import com.spatialcollective.microtasktoolapi.model.QuestionModel
import com.spatialcollective.microtasktoolapi.model.entity.QuestionEntity
import com.spatialcollective.microtasktoolapi.model.toModel
import com.spatialcollective.microtasktoolapi.repository.LinkRepository
import com.spatialcollective.microtasktoolapi.repository.QueryRepository
import com.spatialcollective.microtasktoolapi.repository.QuestionRepository
import com.spatialcollective.microtasktoolapi.repository.TaskRepository
import com.spatialcollective.microtasktoolapi.utils.extentions.toLinkValues
import com.spatialcollective.microtasktoolapi.utils.extentions.toPhoneValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RestController
@RequestMapping("/")
class QuestionController(
    @Autowired private val questionRepository: QuestionRepository,
    @Autowired private val linkRepository: LinkRepository,
    @Autowired private val taskRepository: TaskRepository,
    @Autowired private val queryRepository: QueryRepository
) {
    @GetMapping("/questions")
    fun getAllQuestions(): ResponseEntity<List<Any>> {
        val questions = questionRepository.findAllCountImageAndLink()
        return ResponseEntity.ok(questions)
    }

    @PostMapping(path = ["/question"])
    fun createQuestion(
        @RequestPart questionName: String,
        @RequestPart imageFile: MultipartFile,
        @RequestPart phoneNumberFile: MultipartFile
    ): Any {
        val questionEntity = questionRepository.saveAndFlush(QuestionEntity(name = questionName))
        val linkList = imageFile.toLinkValues(questionEntity.id)

        val phoneList = phoneNumberFile.toPhoneValue(questionEntity.id)
        val phoneValues = phoneList.toString().replace("[", "").replace("]", "")

        queryRepository.insertAllWithList("image", PHONE_COLUMN, linkList)
        queryRepository.insertAllWithQuery("task", TASK_COLUMN, phoneValues)
        return questionEntity.toModel(linkList.size.toLong(), phoneList.size.toLong())
    }


    @PutMapping(path = ["/question/{questionId}"], consumes = ["application/json"])
    fun updateQuestion(@PathVariable questionId: Long, @Valid @RequestBody question: QuestionModel): Any {
        if (!questionRepository.existsById(questionId)) {
            return ResponseEntity.notFound()
        }
        val questionModel = questionRepository.findById(questionId).map {
            it.name = question.name
            it.isPaused = question.isPaused
            questionRepository.save(it).toModel(question.totalImage, question.totalWorker)
        }
        return ResponseEntity.ok(questionModel)
    }

    @DeleteMapping("/question/{questionId}")
    fun deleteQuestion(
        @PathVariable questionId: Long
    ): ResponseEntity<Void> =
        questionRepository.findById(questionId).map {
            questionRepository.delete(it)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElseThrow { ResourceNotFoundException("Question with id $questionId not found") }

    private fun constructQuestionModel(question: QuestionEntity): QuestionModel {
        val totalImages = linkRepository.countByQuestionId(question.id)
        val totalTask = taskRepository.countByQuestionId(question.id)
        return question.toModel(totalImages, totalTask)
    }
}