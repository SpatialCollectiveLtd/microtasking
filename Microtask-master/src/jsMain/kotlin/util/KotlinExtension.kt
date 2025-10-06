package util

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import network.model.ErrorModel
import repository.Resource

fun Any?.isNull() = this == null

fun Any?.isNotNull() = this != null

inline fun <T> T?.isNotNull(firstAction: T.(T) -> Unit, elseAction: (T?) -> Unit = {}) {
    if (this != null) firstAction(this) else elseAction(this)
}

@Suppress("UNCHECKED_CAST")
fun <T> String?.orDefault(default: T): T {
    return if (this.isNullOrEmpty()) {
        default
    } else {
        this as T
    }
}

suspend inline fun tryAndReturnResource(codes: () -> Resource): Resource {
    return try {
        codes()
    } catch (responseException: ServerResponseException) {
        val errorModel = responseException.response.body<ErrorModel>().apply {
            if (message.isEmpty()) message = responseException.message
        }
        Resource.Error(errorModel)
    } catch (throwable: Throwable) {
        Resource.Error(throwable.toErrorModel())
    }
}

fun Throwable.toErrorModel(): ErrorModel {
    return ErrorModel(message = this.message.toString(), error = this.cause?.message.toString())
}

suspend inline fun <reified T> HttpResponse.getBody(): T {
    if (status.value in 200..299) {
        return body()
    } else {
        throw ServerResponseException(this, "")
    }
}

fun <T> MutableList<T>.clearAddAll(newList: List<T>) {
    clear()
    addAll(newList)
}