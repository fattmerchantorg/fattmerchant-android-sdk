package com.staxpayments.api.network

import android.util.Log
import com.google.gson.Gson
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
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

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

    private val gson = Gson()

    suspend fun <T> get(path: String, params: Map<String, Any>? = null, responseType: Class<T>): T {
        val response = client.get("$baseUrl$path") {
            params?.forEach { (key, value) ->
                parameter(key, value.toString())
            }
        }
        return gson.fromJson(response.bodyAsText(), responseType)
    }

    suspend fun <T, R> post(path: String, request: R, responseType: Class<T>): T {
        val response = client.post("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            setBody(Gson().toJson(request))
        }
        return gson.fromJson(response.bodyAsText(), responseType)
    }

    suspend fun <T, R> put(path: String, request: R?, responseType: Class<T>): T {
        val response = client.put("$baseUrl$path") {
            request?.let {
                contentType(ContentType.Application.Json)
                setBody(Gson().toJson(request))
            }
        }
        return gson.fromJson(response.bodyAsText(), responseType)
    }

    suspend fun <T> delete(path: String, responseType: Class<T>): T {
        val response = client.delete("$baseUrl$path")
        return gson.fromJson(response.bodyAsText(), responseType)
    }
}
