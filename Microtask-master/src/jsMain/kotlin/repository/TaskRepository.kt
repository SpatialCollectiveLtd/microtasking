package repository

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import network.API_URL
import network.client
import network.model.*
import org.w3c.dom.get
import org.w3c.fetch.RequestInit
import org.w3c.files.File
import org.w3c.xhr.FormData
import ui.string.fileUploadedSuccessfully
import util.getBody
import util.orDefault
import util.toErrorModel
import util.tryAndReturnResource

private const val ACTIVE_TASK_ID = "activeTaskId"
private const val ACTIVE_QUESTION_ID = "activeQuestionId"
private const val ACTIVE_QUESTION_NAME = "activeQuestionName"
private const val ACTIVE_WORKER_UNIQUE_ID = "activeWorkerUniqueId"
private const val ACTIVE_PROGRESS = "activeProgress"
private const val ACTIVE_TOTAL_LINK = "activeTotalLink"

class TaskRepository {

    private val storage = localStorage

    suspend fun getUserTask(questionId: Long, currentPage: Int): ImagePaginationModel {
        return client.get("/image/$questionId?page=$currentPage").getBody()
    }

    fun getTasksByPhoneNumberAsFlow(phoneNumber: String) = flow {
        val result = tryAndReturnResource {
            val tasks = client.get("/tasks/$phoneNumber").getBody<List<TaskModel>>()
            Resource.Success(data = tasks)
        }
        emit(result)
    }

    suspend fun getTasksByPhoneNumber(phoneNumber: String): Resource {
        return tryAndReturnResource {
            val tasks = client.get("/tasks/$phoneNumber").getBody<List<TaskModel>>()
            Resource.Success(data = tasks)
        }
    }

    fun setActiveTask(myTask: TaskModel) {
        storage.setItem(ACTIVE_TASK_ID, myTask.id.toString())
        storage.setItem(ACTIVE_QUESTION_ID, myTask.questionId.toString())
        storage.setItem(ACTIVE_QUESTION_NAME, myTask.questionName)
        storage.setItem(ACTIVE_WORKER_UNIQUE_ID, myTask.workerUniqueId)
        storage.setItem(ACTIVE_PROGRESS, myTask.progress.toString())
        storage.setItem(ACTIVE_TOTAL_LINK, myTask.totalLink.toString())
    }

    fun getActiveTask(): TaskModel {
        return TaskModel().apply {
            id = storage[ACTIVE_TASK_ID].orDefault(0)
            questionId = storage[ACTIVE_QUESTION_ID].orDefault(0)
            questionName = storage[ACTIVE_QUESTION_NAME].orEmpty()
            workerUniqueId = storage[ACTIVE_WORKER_UNIQUE_ID].orEmpty()
            progress = storage[ACTIVE_PROGRESS].orDefault(0)
            totalLink = storage[ACTIVE_TOTAL_LINK].orDefault(0)
        }
    }

    suspend fun sendAnswer(answerModel: AnswerModel): Resource {
        return tryAndReturnResource {
            val answer = client.post("/answer") {
                contentType(ContentType.Application.Json)
                setBody(answerModel)
            }.getBody<Int>()
            Resource.Success(data = answer)
        }
    }


    suspend fun getWorkerByQuestionId(questionId: Long): Resource {
        return tryAndReturnResource {
            val workers =
                client.get("/workers") { parameter("questionId", questionId) }.getBody<MutableList<TaskModel>>()
            Resource.Success(data = workers)
        }
    }

    suspend fun deleteWorker(workerModel: TaskModel): Resource {
        return tryAndReturnResource {
            client.delete("/worker") {
                parameter("workerId", workerModel.id)
            }.getBody<Unit>()
            Resource.Success(data = "Worker with unique Id ${workerModel.workerUniqueId}, deleted successfully")
        }
    }

    suspend fun uploadFile(questionId: Long, phoneNumberFile: File): Resource {
        return try {
            val response = window.fetch(
                "$API_URL/tasks",
                RequestInit("POST", body = FormData().apply {
                    append("questionId", "$questionId")
                    append("phoneNumberFile", phoneNumberFile)
                }
                )).await()
            if (response.ok) {
                Resource.Success(fileUploadedSuccessfully)
            } else {
                val error = response.text().await()
                Resource.Error(Json.decodeFromString(ErrorModel.serializer(), error))
            }
        } catch (throwable: Throwable) {
            Resource.Error(throwable.toErrorModel())
        }
    }
}