package network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

const val API_URL = "http://localhost:8080"//"https://microtasktool.spatialcollective.com"

val client = HttpClient {
    install(ContentNegotiation) { json() }
    defaultRequest {
        url(API_URL)
    }
}