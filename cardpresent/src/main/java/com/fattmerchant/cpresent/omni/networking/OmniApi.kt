package com.fattmerchant.cpresent.omni.networking

import com.fattmerchant.cpresent.omni.entity.models.*
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.receive
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess

class OmniApi {

    val gson = GsonBuilder()
        .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            }
        }
    }

    fun <T> toJson(obj: T): String {
        return gson.toJson(obj)
    }

    fun <T> fromJson(json: String): T {
        val type = object : TypeToken<T>() {}.type

        val gson = GsonBuilder()
            .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .serializeNulls()
            .create()

        return gson.fromJson<T>(json, type)
    }

    var token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJtZXJjaGFudCI6ImE2MWQ3OGNjLWNkZTktNDRhYy04YTE4LTMwYzM5YmUwNTg3OSIsImdvZFVzZXIiOnRydWUsInN1YiI6IjMwYzZlZWI2LTY0YjYtNDdmNi1iY2Y2LTc4N2E5YzU4Nzk4YiIsImlzcyI6Imh0dHA6Ly9hcGlkZXYwMS5mYXR0bGFicy5jb20vYXV0aGVudGljYXRlIiwiaWF0IjoxNTczNzQ0MTUzLCJleHAiOjE1NzM4MzA1NTMsIm5iZiI6MTU3Mzc0NDE1MywianRpIjoiNzFXbWM3VzM1ZFZ4OG5YNCJ9.xl9LbSXJLXVcrxfVdKSqU1icuPUi3RXeaX4pwlbZ3og"

    companion object {
        val baseUrl = "https://apidev01.fattlabs.com/"
    }

    /**
     * Uses the [token] field and returns the [Self] object associated with that token
     *
     * @param error
     * @return [Self], the owner of the [token]
     */
    suspend fun getSelf(error: (Error) -> Unit): Self? = get("self", error)

    /**
     * Gets the merchant which the [token] corresponds to
     *
     * @param error
     * @return
     */
    suspend fun getMerchant(error: (Error) -> Unit): Merchant? = getSelf(error)?.merchant

    /**
     * Creates a new invoice in Omni
     *
     * @param invoice
     * @return the created invoice
     */
    suspend fun createInvoice(invoice: Invoice, error: (Error) -> Unit): Invoice? =
        post("invoice", toJson(invoice), error)

    /**
     * Updates an new invoice in Omni
     *
     * @param invoice
     * @return the updated invoice
     */
    suspend fun updateInvoice(id: String, invoice: Invoice): Invoice? =
        put("invoice/${invoice.id}", toJson(invoice))

    /**
     * Creates a new customer in Omni
     *
     * @param customer
     * @return the created customer
     */
    suspend fun createCustomer(customer: Customer, error: (Error) -> Unit): Customer? =
        post("customer", toJson(customer), error)

    /**
     * Creates a transaction in Omni
     *
     * @param transaction
     * @return the created transaction
     */
    suspend fun createTransaction(transaction: Transaction, error: (Error) -> Unit): Transaction? =
        post("transaction", toJson(transaction), error)

    /**
     * Gets a list of transactions from Omni
     *
     * @return the list of transactions
     */
    suspend fun getTransactions(error: (Error) -> Unit): List<Transaction>? =
        get<PaginatedData<Transaction>>("transaction", error)?.data

    /**
     * Gets a list of transactions from Omni
     *
     * @return the list of transactions
     */
    suspend fun getInvoices(error: (Error) -> Unit): List<Invoice>? =
        get<PaginatedData<Invoice>>("invoice", error)?.data

    /**
     * Creates a payment method in Omni
     *
     * @param paymentMethod
     * @return the created payment method
     */
    suspend fun createPaymentMethod(paymentMethod: PaymentMethod, error: (Error) -> Unit): PaymentMethod? =
        post("payment-method", toJson(paymentMethod), error)

    private suspend inline fun <reified T> post(urlString: String, body: String, error: (Error) -> Unit): T? =
        this.request<T>(
            method = HttpMethod.Post,
            urlString = urlString,
            body = body,
            error = error
        )

    private suspend inline fun <reified T> put(urlString: String, body: String): T? = this.request<T>(
        method = HttpMethod.Put,
        urlString = urlString,
        body = body
    )

    suspend inline fun <reified T> get(urlString: String, error: (Error) -> Unit): T? = this.request<T>(
        method = HttpMethod.Get,
        urlString = urlString,
        error = error
    )

    suspend inline fun <reified T> request(method: HttpMethod, urlString: String, body: String? = null): T? =
        request(method, urlString, body) {}

    suspend inline fun <reified T> request(
        method: HttpMethod,
        urlString: String,
        body: String? = null,
        error: ((Error) -> Unit)
    ): T? {
        val url = baseUrl + urlString

        try {

            // Make the request and wait for the response
            val response = httpClient.request<HttpResponse>(url) {
                headers.append("Authorization", "Bearer $token")
                this.method = method
                body?.let {
                    this.body = TextContent(body, ContentType.Application.Json)
                }
            }

            // Attempt to get the object we're expecting
            if (response.status.isSuccess()) {
                return response.receive<T>()
            }

            val responseText = response.readText()

            // Triage the error
            when (response.status.value) {
                in 300..399 -> {
                    // Redirect
                    error(Error("Redirect"))
                }

                in 400..499 -> {
                    // Client error
                    if (isTokenExpired(responseText)) {
                        error(Error("token_expired"))
                    }

                    error(Error("Client error"))
                }

                in 500..599 -> {
                    // Server error
                    error(Error("Server error"))
                }
            }
            return null

//            Working code below
//            return httpClient.request<T>(url) {
//                headers.append("Authorization", "Bearer $token")
//                this.method = method
//                body?.let {
//                    this.body = TextContent(body, ContentType.Application.Json)
//                }
//            }

        } catch (e: NoTransformationFoundException) {
            // We were expecting an object of type T, but couldn't transform the response body to T
            print(e)
            throw e
        } catch (e: Exception) {
            // This happens when there was an error executing the request
            // This is a good place to look at that exception and do something with it before throwing it back
            print(e)
            throw e
        }
    }

    fun isTokenExpired(response: String): Boolean = response.contains("token_expired")

}
