package repository

import io.ktor.client.request.*
import network.client
import network.model.ImageCountModel
import util.getBody
import util.tryAndReturnResource

class ImageRepository {

    suspend fun getLinksByQuestionId(questionId: Long): Resource {
        return tryAndReturnResource {
            val links = client.get("/images/$questionId").getBody<MutableList<ImageCountModel>>()
            Resource.Success(data = links)
        }
    }

    suspend fun deleteLinks(questionId: Long, createdDate: String): Resource {
        return tryAndReturnResource {
            val deletedItems = client.delete("/images") {
                parameter("questionId", questionId)
                parameter("createdAt", createdDate)
            }.getBody<Int>()
            Resource.Success(data = "$deletedItems Links deleted successfully")
        }
    }
}