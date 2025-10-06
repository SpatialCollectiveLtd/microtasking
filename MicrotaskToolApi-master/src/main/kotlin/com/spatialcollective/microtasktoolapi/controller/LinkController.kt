package com.spatialcollective.microtasktoolapi.controller

import com.spatialcollective.microtasktoolapi.model.ImageCountModel
import com.spatialcollective.microtasktoolapi.model.ImagePaginationModel
import com.spatialcollective.microtasktoolapi.model.toImagePaginationModel
import com.spatialcollective.microtasktoolapi.model.toModel
import com.spatialcollective.microtasktoolapi.repository.LinkRepository
import com.spatialcollective.microtasktoolapi.repository.QueryRepository
import com.spatialcollective.microtasktoolapi.repository.QuestionRepository
import com.spatialcollective.microtasktoolapi.utils.extentions.toLinkValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

const val TASK_COLUMN =
    "(id, created_at, updated_at, phone_number, progress, start_date, worker_unique_id, question_id)"

@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RestController
@RequestMapping("/")
class LinkController(
    @Autowired private val linkRepository: LinkRepository,
    @Autowired private val questionRepository: QuestionRepository,
    @Autowired private val queryRepository: QueryRepository
) {

    @GetMapping("/image/{questionId}")
    fun getPageableImage(
        @PathVariable(value = "questionId") questionId: Long, @RequestParam("page") page: Int
    ): ImagePaginationModel {
        val pageable = PageRequest.of(page, 1)
        val result = linkRepository.findByQuestionId(questionId, pageable)
        return result.content.map { it.toImagePaginationModel(result.totalPages, result.number, result.isLast) }
            .firstOrNull() ?: ImagePaginationModel()
    }

    @GetMapping("/images/{questionId}")
    fun getAllImages(@PathVariable(value = "questionId") questionId: Long): List<ImageCountModel> {
        return linkRepository.findByQuestionIdAndGroupByCreatedAt(questionId)
            .mapIndexed { index, model -> model.toModel(index) }
    }

    @DeleteMapping("/images")
    fun deleteImages(
        @RequestParam("questionId") questionId: Long, @RequestParam("createdAt") createdAt: String
    ): Long {
        return linkRepository.deleteByQuestionIdAndCreatedAt(questionId, createdAt)
    }

    @PostMapping(path = ["/images"])
    fun createImages(
        @RequestPart questionId: String, @RequestPart imageFile: MultipartFile
    ): ResponseEntity<Void> {
        questionRepository.findById(questionId.toLong()).map { questionEntity ->
            val linkList = imageFile.toLinkValues(questionEntity.id)
            queryRepository.insertAllWithList("image", PHONE_COLUMN, linkList)
        }
        return ResponseEntity<Void>(OK)
    }
}