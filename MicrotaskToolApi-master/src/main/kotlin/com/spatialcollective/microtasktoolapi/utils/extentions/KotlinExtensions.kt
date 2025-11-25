package com.spatialcollective.microtasktoolapi.utils.extentions

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.spatialcollective.microtasktoolapi.model.entity.TaskEntity
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.multipart.MultipartFile
import java.net.URL
import java.util.*


fun Any?.isNotNull(): Boolean {
    return this != null
}

fun Any?.isNull() = this == null

inline fun Boolean.isTrue(action: () -> Unit): Boolean {
    if (this) {
        action()
    }
    return this
}

inline fun Boolean.isNotTrue(action: () -> Unit) {
    if (!this) {
        action()
    }
}


fun ServletWebRequest.getPath(): String {
    return request.requestURI.toString()
}

fun MultipartFile.toLinkValues(questionId: Long): List<String> {
    val createdDate = Date()
    return csvReader().readAll(inputStream).map {
        "(null, \"${it.first()}${it.last()}\", \"${createdDate.stringFormat()}\", ${questionId})"
    }
}

fun MultipartFile.toPhoneValue(questionId: Long): List<String> {
    val taskEntity = TaskEntity()
    return csvReader().readAll(inputStream)
        .map { "(null, '${taskEntity.createdAt.stringFormat()}', '${taskEntity.updatedAt.stringFormat()}', '${it.last()}',0,null,'${it.first()}',${questionId})" }
}

fun String.getName(): String {
    return if (this.isNotEmpty()) {
        val urlPath = URL(this).path
        val lastIndex = urlPath.lastIndexOf('/')
        if (lastIndex != -1) {
            urlPath.substring(lastIndex + 1)
        } else urlPath
    } else {
        ""
    }
}