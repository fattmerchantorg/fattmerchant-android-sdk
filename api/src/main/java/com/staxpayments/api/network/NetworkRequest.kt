package com.staxpayments.api.network

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.observer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.modules.SerializersModule

class NetworkClient {
    private val client = HttpClient(Android) {

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("Logger Ktor =>", message)
                    //firebase log
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
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }
        install(UserAgent) {
            agent = "ktor-client"
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(HttpSend)
    }

    suspend fun <T> get(url: String, params: Map<String, Any>? = null,  responseType: Class<T>) : T {
        val response = client.get(url) {
            params?.forEach { (key, value) ->
                parameter(key, value.toString())
            }
            header("Accept", "application/json")
        }
        val text = response.bodyAsText()
        return Json.nonstrict.parse(responseType.serializer(), text)
    }

    private val jsonDecodeBuilder = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val jsonDynamicLookupBuilder = Json {
        encodeDefaults = false
        serializersModule = SerializersModule {
            contextual(Any::class, DynamicLookupSerializer)
        }
    }

    fun jsonDynamicLookupBuilder() = jsonDynamicLookupBuilder

    suspend fun <T, R> pot(url: String, request: R?, responseType: Class<T>) : T {
        val response = client.request<T> {
            logCaller(
                ApiRequestLogger.Default(networkRequest.httpMethod, networkRequest.path)
            )

            method = mapHttpMethod(networkRequest.httpMethod)
            url {
                protocol = URLProtocol.HTTPS
                host = networkRequest.host
                path(networkRequest.path)
                networkRequest.queryParameters.addParams(parameters)
            }

            networkRequest.headers.addHeaders(headers)

            if (networkRequest.requestBody != EmptyRequestBody) {
                body = if (!networkRequest.encodeDefaults) {
                    jsonDynamicLookupBuilder().encodeToJsonElement(networkRequest.requestBody)
                } else networkRequest.requestBody
            }
        }

        val text = response.readText()
        return Json.nonstrict.parse(responseType.serializer(), text)
    }


    suspend fun <T, R> post(url: String, request: R?, responseType: Class<T>) : T {
        val response = client.post(url) {
            request?.let {
                contentType(ContentType.Application.Json)
                body = Json.stringify(requestType.serializer(), it)
            }
        }

        val text = response.readText()
        return Json.nonstrict.parse(responseType.serializer(), text)
    }

    suspend fun <T, R> put(url: String, request: R?, responseType: Class<T>) : T {
        val response = client.put(url) {
            request?.let {
                contentType(ContentType.Application.Json)
                body = Json.stringify(requestType.serializer(), it)
            }
        }
        val text = response.bodyAsText()
        return Json.nonstrict.parse(responseType.serializer(), text)
    }

    suspend fun <T> delete(url: String, responseType: Class<T>) : T {
        val response = client.delete(url)
        val text = response.bodyAsText()
        return Json.nonstrict.parse(responseType.serializer(), text)
    }
}


data class RequestData(val name: String, val age: Int)
data class ResponseData(val message: String)

suspend fun main() {

    try {
        val networkClient = NetworkClient()
        val request = RequestData("John Doe", 30)
        val response = networkClient.post("https://api.example.com/post", request, ResponseData::class.java)
        println(response.message)
    }catch (e: Exception){
        println(e.message)
    }

}