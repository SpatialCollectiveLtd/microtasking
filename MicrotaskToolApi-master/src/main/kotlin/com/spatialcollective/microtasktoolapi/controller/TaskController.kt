package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.TaskModel
import com.spatialcollective.microtasktoolapi.model.toModel
import com.spatialcollective.microtasktoolapi.repository.LinkRepository
import com.spatialcollective.microtasktoolapi.repository.QueryRepository
import com.spatialcollective.microtasktoolapi.repository.QuestionRepository
import com.spatialcollective.microtasktoolapi.repository.TaskRepository
import com.spatialcollective.microtasktoolapi.utils.extentions.toPhoneValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

const val PHONE_COLUMN = "(id, url,created_at, question_id)"

@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RestController
@RequestMapping("/")
class TaskController(
    @Autowired private val taskRepository: TaskRepository,
    @Autowired private val questionRepository: QuestionRepository,
    @Autowired private val linkRepository: LinkRepository,
    @Autowired private val queryRepository: QueryRepository
) {

    @GetMapping("/workers")
    fun getWorkersByQuestionId(@RequestParam questionId: Long): List<TaskModel> {
        return taskRepository.findByQuestionId(questionId).map { it.toModel(0) }
    }

    @GetMapping("/tasks/{phoneNumber}")
    fun findUserTaskByPhoneNumber(@PathVariable phoneNumber: String): ResponseEntity<List<TaskModel>> {
        return ResponseEntity.ok(taskRepository.findTaskByPhoneNumber(phoneNumber).map {
            val totalImage = linkRepository.countByQuestionId(it.question.id)
            it.toModel(totalImage)
        })
    }

    @PostMapping(path = ["/tasks"])
    fun addMorePhoneNumbers(
        @RequestPart questionId: String,
        @RequestPart phoneNumberFile: MultipartFile
    ): List<TaskModel> {
        questionRepository.findById(questionId.toLong()).map { questionEntity ->
            val phoneList = phoneNumberFile.toPhoneValue(questionEntity.id)
            val phoneValues = phoneList.toString().replace("[", "").replace("]", "")
            queryRepository.insertAllWithQuery("task", TASK_COLUMN, phoneValues)
        }
        return taskRepository.findByQuestionId(questionId.toLong()).map { it.toModel() }
    }

    @DeleteMapping("/worker")
    fun deleteWorkerById(@RequestParam workerId: Long) {
        taskRepository.deleteById(workerId)
    }
}