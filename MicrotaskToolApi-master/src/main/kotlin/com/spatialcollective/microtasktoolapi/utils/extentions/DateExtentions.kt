package com.spatialcollective.microtasktoolapi.utils.extentions

import java.text.SimpleDateFormat
import java.util.*


private fun formatter(pattern: String = "yyyy-MM-dd HH:mm:ss") = SimpleDateFormat(pattern, Locale.getDefault())

fun Date.stringFormat(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return formatter(pattern).format(this)
}

fun String.toDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): Date = formatter(pattern).parse(this)


fun Calendar.getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}