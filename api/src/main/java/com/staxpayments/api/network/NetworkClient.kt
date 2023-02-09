package com.staxpayments.api.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NetworkClient(private val baseUrl: String) {

    companion object {
        private var instance: NetworkClient? = null
        fun initialize(baseUrl: String): NetworkClient {
            if (instance == null) {
                instance = NetworkClient(baseUrl)
            }
            return instance!!
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val format = Json { explicitNulls = false }

    private val client = HttpClient {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("Logger Ktor =>", message)
                    // firebase log
                }
            }
            level = LogLevel.ALL
        }

        install(ResponseObserver) {
            onResponse { response ->
                Log.d("HTTP status:", "${response.status.value}")
            }
        }

        install(DefaultRequest) {

            headers {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                append(HttpHeaders.Authorization, "")
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(HttpSend)
    }

    suspend fun <R> get(path: String, request: Any? = null, responseType: DeserializationStrategy<R>): R {
        val requestJson = when (request) {
            is String -> request
            is Map<*, *> -> Json.encodeToString(request)
            else -> {
                format.encodeToString(request)
            }
        }

        val response = client.get("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(requestJson)
        }
        return Json.decodeFromString(responseType, response.bodyAsText())
    }

    suspend fun <R> post(path: String, request: Any? = null, responseType: DeserializationStrategy<R>): R {
        val requestJson = when (request) {
            is String -> request
            is Map<*, *> -> Json.encodeToString(request)
            else -> {
                format.encodeToString(request)
            }
        }

        val response = client.post("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(requestJson)
        }
        return Json.decodeFromString(responseType, response.bodyAsText())
    }

    suspend fun <R> put(path: String, request: Any? = null, responseType: DeserializationStrategy<R>): R {
        val requestJson = when (request) {
            is String -> request
            is Map<*, *> -> Json.encodeToString(request)
            else -> {
                format.encodeToString(request)
            }
        }

        val response = client.post("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(requestJson)
        }
        return Json.decodeFromString(responseType, response.bodyAsText())
    }

    suspend fun <R> delete(path: String, responseType: DeserializationStrategy<R>): R {
        val response = client.delete("$baseUrl$path")
        return Json.decodeFromString(responseType, response.bodyAsText())
    }
}
