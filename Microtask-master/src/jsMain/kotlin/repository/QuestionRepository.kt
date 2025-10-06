package repository

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json.Default.decodeFromString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import network.API_URL
import network.client
import network.model.ErrorModel
import network.model.QuestionModel
import org.w3c.dom.get
import org.w3c.fetch.RequestInit
import org.w3c.files.File
import org.w3c.xhr.FormData
import ui.screens.adminFeatures.questionDetails.model.QuestionErrorModel
import ui.string.fileUploadedSuccessfully
import util.getBody
import util.orDefault
import util.toErrorModel
import util.tryAndReturnResource

const val ERROR_TITLE_KEY = "errorTitleKey"
const val ERROR_MESSAGE_KEY = "errorMessageKey"

const val QUESTION_ID_KEY = "questionIdKey"
const val QUESTION_NAME_KEY = "questionNameKey"
const val QUESTION_TOTAL_LINKS_KEY = "questionTotalLinkKey"
const val QUESTION_STATE_KEY = "questionStateKey"

class QuestionRepository {
    private val storage = localStorage

    private var questions = mutableListOf<QuestionModel>()
    val resource = MutableStateFlow<Resource>(Resource.Loading)

    suspend fun getQuestions() {
        resource.emit(tryAndReturnResource {
            questions = client.get("/questions").getBody<JsonArray>().map {
                console.log(it.jsonArray[1])
                QuestionModel(
                    it.jsonArray[0].toString().toLong(),
                    it.jsonArray[1].removeQuotation(),
                    it.jsonArray[2].toString().toBoolean(),
                    it.jsonArray[3].toString().toLong(),
                    it.jsonArray[4].toString().toLong(),
                    it.jsonArray[5].removeQuotation()
                )
            }.toMutableList()
            Resource.Success(data = questions)
        })
    }

    fun getSendingQuestionError(): QuestionErrorModel {
        return QuestionErrorModel(storage[ERROR_TITLE_KEY].orEmpty(), storage[ERROR_MESSAGE_KEY].orEmpty())
    }

    fun deleteQuestionError() {
        storage.removeItem(ERROR_TITLE_KEY)
        storage.removeItem(ERROR_MESSAGE_KEY)
    }

    fun saveQuestion(question: QuestionModel) {
        storage.setItem(QUESTION_ID_KEY, question.id.toString())
        storage.setItem(QUESTION_NAME_KEY, question.name)
        storage.setItem(QUESTION_TOTAL_LINKS_KEY, question.totalImage.toString())
        storage.setItem(QUESTION_STATE_KEY, question.isPaused.toString())
    }

    fun getQuestionFromStorage(): QuestionModel {
        return QuestionModel(
            storage[QUESTION_ID_KEY].orDefault(0),
            storage[QUESTION_NAME_KEY].orEmpty(),
            totalImage = storage[QUESTION_TOTAL_LINKS_KEY].orDefault(0),
            isPaused = storage[QUESTION_STATE_KEY].toBoolean(),
        )
    }

    suspend fun deleteQuestion(question: QuestionModel): Resource {
        return tryAndReturnResource {
            client.delete("/question/" + question.id).getBody<Unit>()
            removeQuestion(question)
            Resource.Success(data = "Question (${question.name}) deleted successfully")
        }
    }

    private suspend fun removeQuestion(questionModel: QuestionModel) {
        questions.remove(questionModel)
        resource.emit(Resource.Success(data = questions))
    }

    suspend fun updateQuestionState(questionModel: QuestionModel, shouldPaused: Boolean): Resource {
        return tryAndReturnResource {
            val question = client.put("question/" + questionModel.id) {
                contentType(ContentType.Application.Json)
                setBody(questionModel.apply { isPaused = shouldPaused })
            }.getBody<QuestionModel>()
            saveQuestion(question)
            Resource.Success(question)
        }
    }

    suspend fun createNewQuestion(questionName: String, imageFile: File, phoneNumberFile: File): Resource {
        return try {
            val response = window.fetch(
                "$API_URL/question",
                RequestInit("POST", body = FormData().apply {
                    append("questionName", questionName)
                    append("imageFile", imageFile)
                    append("phoneNumberFile", phoneNumberFile)
                }
                )).await()
            if (response.ok) {
                val question = response.text().await()
                Resource.Success(decodeFromString(QuestionModel.serializer(), question))
            } else {
                val error = response.text().await()
                Resource.Error(decodeFromString(ErrorModel.serializer(), error))
            }
        } catch (throwable: Throwable) {
            Resource.Error(throwable.toErrorModel())
        }
    }

    suspend fun uploadFile(questionId: Long, imageFile: File): Resource {
        return try {
            val response = window.fetch(
                "$API_URL/images",
                RequestInit("POST", body = FormData().apply {
                    append("questionId", "$questionId")
                    append("imageFile", imageFile)
                }
                )).await()
            if (response.ok) {
                Resource.Success(fileUploadedSuccessfully)
            } else {
                val error = response.text().await()
                Resource.Error(decodeFromString(ErrorModel.serializer(), error))
            }
        } catch (throwable: Throwable) {
            Resource.Error(throwable.toErrorModel())
        }
    }
}

private fun JsonElement.removeQuotation(): String {
    return toString().replace("\"", "")
}