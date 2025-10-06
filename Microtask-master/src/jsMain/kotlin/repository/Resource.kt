package repository

import androidx.compose.runtime.Composable
import network.model.ErrorModel
import network.model.ToastModel
import network.model.ToastType

sealed class Resource {

    object None : Resource()

    object Loading : Resource()

    data class Success<T>(val data: T) : Resource()

    data class Error<T>(val error: T) : Resource()

}

@Suppress("UNCHECKED_CAST")
suspend fun <T> Resource.whenSucceed(code: suspend (Resource.Success<T>) -> Unit) {
    if (this.isSucceed()) {
        code(this as Resource.Success<T>)
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <T> Resource.whenError(code: suspend (Resource.Error<T>) -> Unit) {
    if (this.isError()) {
        code(this as Resource.Error<T>)
    }
}

@Composable
fun Resource.whenError(code: @Composable (Resource.Error<*>) -> Unit) {
    if (this.isError()) {
        code(this as Resource.Error<*>)
    }
}

fun Resource.isError() = this is Resource.Error<*>

fun Resource.isSucceed() = this is Resource.Success<*>

fun Resource.isLoading() = this is Resource.Loading

@Suppress("UNCHECKED_CAST")
fun <T> Resource.getData(): T {
    return (this as Resource.Success<*>).data as T
}

fun Resource.getError(): Any {
    return (this.unsafeCast<Resource.Error<Any>>()).error
}


fun Resource.getErrorMessage() = when (val error = getError()) {
    is String -> error
    is ErrorModel -> "${error.status} ${error.message}"
    else -> ""
}

fun Resource.toToastModel(): ToastModel? {
    return when (this) {
        Resource.Loading -> null
        Resource.None -> null
        is Resource.Success<*> -> ToastModel("Succeed! ", getData())
        is Resource.Error<*> -> ToastModel("Error! ", getErrorMessage(), ToastType.ERROR)
    }
}
